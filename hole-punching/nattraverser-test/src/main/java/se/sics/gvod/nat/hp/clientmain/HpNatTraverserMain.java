package se.sics.gvod.nat.hp.clientmain;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import se.sics.gvod.timer.TimeoutId;
import java.util.logging.Level;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.VodNetwork;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.gvod.timer.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfImpl;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.hp.msgs.TConnectionMessage;
import se.sics.gvod.config.HpClientConfiguration;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.nat.traversal.NatTraverser;
import se.sics.gvod.config.NatTraverserConfiguration;
import se.sics.gvod.nat.traversal.NatTraverserPort;
import se.sics.gvod.nat.traversal.events.NatTraverserInit;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.config.StunServerConfiguration;
import se.sics.gvod.croupier.Croupier;
import se.sics.gvod.croupier.CroupierPort;
import se.sics.gvod.croupier.PeerSamplePort;
import se.sics.gvod.croupier.events.CroupierInit;
import se.sics.gvod.croupier.events.CroupierJoin;
import se.sics.gvod.croupier.events.CroupierSample;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.UUID;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.kompics.Fault;

/**
 * The
 * <code>Root</code> class
 *
 */
public final class HpNatTraverserMain extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(HpNatTraverserMain.class);
    private static final int OVERLAY_ID = 100;
    private static Address localAddress;
    private final Component timer;
    private final Component network;
    private final Component resolveIp;
    private final Component natTraverser;
    private final Component croupier;
    final NatTraverserConfiguration ntConfig;
    final RendezvousServerConfiguration rendezvousServerConfig;
    Self self;
    static Set<Address> servers = new HashSet<Address>();
    private static boolean upnpEnabled;
    private static int myId;
    private static final int SERVER_ID = 1;
    private static String server;
    private static boolean openServer = false;
    private static Integer pickIp;
    private static Integer numFail = 0, numSuccess = 0;
    private Set<VodAddress> alreadyConnected = new HashSet<VodAddress>();

    public static void main(String[] args) {
        // This initializes the Kompics runtime, and creates an instance of Root
        if (args.length < 3) {
            logger.info("Usage: <prog> upnp id bindIp bootstrapNode [openServer] [destId destNatType]");
            logger.info("       bindIp: 0=publicIp, 1=privateIp1, 2=privateIp2");
            logger.info("e.g.  <prog> true 1 0 cloud7.sics.se false 2 NAT_EI_PP_PD");
            System.exit(0);
        }
        upnpEnabled = Boolean.parseBoolean(args[0]);
        myId = Integer.parseInt(args[1]);
        pickIp = Integer.parseInt(args[2]);
        server = args[3];
        try {
            Address s = new Address(InetAddress.getByName(server), VodConfig.DEFAULT_STUN_PORT,
                    SERVER_ID);
            servers.add(s);
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(HpNatTraverserMain.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if (args.length > 4) {
            openServer = Boolean.parseBoolean(args[4]);
        }

        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            VodConfig.init(args);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(HpNatTraverserMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        Kompics.createAndStart(HpNatTraverserMain.class, 3);
    }

    public static class HolePunchTimeout extends Timeout {

        public HolePunchTimeout(ScheduleTimeout st) {
            super(st);
        }
    }

    public HpNatTraverserMain() throws IOException {

        ntConfig = NatTraverserConfiguration.build();
        rendezvousServerConfig =
                RendezvousServerConfiguration.build().
                setSessionExpirationTime(120 * 1000);
        timer = create(JavaTimer.class);
        network = create(NettyNetwork.class);
        resolveIp = create(ResolveIp.class);
        natTraverser = create(NatTraverser.class);
        croupier = create(Croupier.class);

        connect(croupier.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(croupier.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));

        connect(natTraverser.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(natTraverser.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));
        connect(natTraverser.getNegative(NatNetworkControl.class), network.getPositive(NatNetworkControl.class));
        connect(resolveIp.getNegative(Timer.class), timer.getPositive(Timer.class));


        subscribe(handleStart, control);
        subscribe(handleGetNatTypeResponse, natTraverser.getPositive(NatTraverserPort.class));
        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
        subscribe(handlePing, natTraverser.getPositive(VodNetwork.class));
        subscribe(handlePong, natTraverser.getPositive(VodNetwork.class));
        subscribe(handleFault, natTraverser.getControl());
        subscribe(handleNettyFault, network.getControl());
        subscribe(handleHolePunchTimeout, timer.getPositive(Timer.class));
        subscribe(handleCroupierSample, croupier.getPositive(PeerSamplePort.class));

    }
    private Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            trigger(new GetIpRequest(false, EnumSet.of(
                    GetIpRequest.NetworkInterfacesMask.IGNORE_LOOPBACK)),
                    resolveIp.getPositive(ResolveIpPort.class));
        }
    };
    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
        @Override
        public void handle(GetIpResponse event) {


            InetAddress localIp = null;
            if (pickIp > 0) {
                localIp = event.getTenDotIpAddress(pickIp);
                if (localIp == null) {
                    System.err.println("No 10.* IP address found. Exiting.");
                    System.exit(0);
                }
            } else {
                localIp = event.getBoundIp();
            }

            if (localIp != null) {
                logger.info("Found net i/f with ip address: " + localIp);

                localAddress = new Address(localIp, VodConfig.getPort(), myId);
                trigger(new NettyInit(localAddress, true,
                        VodConfig.getSeed(), BaseMsgFrameDecoder.class), network.getControl());

                self = new SelfImpl(null, localAddress.getIp(), localAddress.getPort(),
                        localAddress.getId(), OVERLAY_ID);

                trigger(new NatTraverserInit(self, servers, VodConfig.getSeed(),
                        NatTraverserConfiguration.build(),
                        HpClientConfiguration.build(),
                        RendezvousServerConfiguration.build(),
                        StunClientConfiguration.build().setUpnpEnable(upnpEnabled),
                        StunServerConfiguration.build(),
                        ParentMakerConfiguration.build(),
                        openServer),
                        natTraverser.getControl());
                trigger(new CroupierInit(self,
                        CroupierConfiguration.build()),
                        croupier.getControl());

            } else {
                logger.error("Couldnt find a network interface that is up");
                System.exit(-1);
            }
        }
    };
    private Handler<GetNatTypeResponse> handleGetNatTypeResponse =
            new Handler<GetNatTypeResponse>() {
        @Override
        public void handle(GetNatTypeResponse event) {

            logger.info("Nat type is: " + event.getNat());

            List<VodDescriptor> svd = new ArrayList<VodDescriptor>();
            Address s = servers.iterator().next();
            VodAddress s1 = ToVodAddr.systemAddr(s);
            svd.add(new VodDescriptor(s1, new UtilityVod(0), 0, VodConfig.DEFAULT_MTU));
            trigger(new CroupierJoin(svd), croupier.getPositive(CroupierPort.class));
        }
    };
    public Handler<TConnectionMessage.Ping> handlePing =
            new Handler<TConnectionMessage.Ping>() {
        @Override
        public void handle(TConnectionMessage.Ping ping) {

            logger.info("Received ping from "
                    + ping.getSource().getId());
            TimeoutId id = UUID.nextUUID();
            TConnectionMessage.Pong pong =
                    new TConnectionMessage.Pong(self.getAddress(),
                    ping.getVodSource(),
                    "here's a pong", id);
            trigger(pong, network.getPositive(VodNetwork.class));
        }
    };
    public Handler<TConnectionMessage.Pong> handlePong =
            new Handler<TConnectionMessage.Pong>() {
        @Override
        public void handle(TConnectionMessage.Pong pong) {

            logger.info("pong recvd " + pong.getMessage() + " from " + pong.getSource());
            numSuccess++;
            logger.info("Total Success/Failure ratio is: {}/{}", numSuccess, numFail);
        }
    };
    public Handler<Fault> handleFault =
            new Handler<Fault>() {
        @Override
        public void handle(Fault ex) {

            logger.debug(ex.getFault().toString());
            System.exit(-1);
        }
    };
    Handler<HolePunchTimeout> handleHolePunchTimeout = new Handler<HolePunchTimeout>() {
        @Override
        public void handle(HolePunchTimeout msg) {
            logger.info("FAILURE: pong not recvd for TimeoutId: " + msg.getTimeoutId());
            numFail++;
            logger.info("Total Success/Failure ratio is: {}/{}", numSuccess, numFail);

        }
    };
    Handler<CroupierSample> handleCroupierSample = new Handler<CroupierSample>() {
        @Override
        public void handle(CroupierSample msg) {
            for (VodDescriptor vd : msg.getNodes()) {
                VodAddress va = vd.getVodAddress();
                if (alreadyConnected.contains(va) == false) {
                    // try and send a msg to the new VodAddress
                    if (va.hasParents()) {
                        ScheduleTimeout st = new ScheduleTimeout(10 * 1000);
                        HolePunchTimeout hp = new HolePunchTimeout(st);
                        st.setTimeoutEvent(hp);
                        trigger(new TConnectionMessage.Ping(self.getAddress(),
                                va, hp.getTimeoutId(), "Hi there"),
                                natTraverser.getPositive(VodNetwork.class));
                        trigger(st, timer.getPositive(Timer.class));
                        alreadyConnected.add(va);
                    }
                }
            }
        }
    };
    Handler<Fault> handleNettyFault = new Handler<Fault>() {
        @Override
        public void handle(Fault msg) {
            logger.error("Problem in Netty: {}", msg.getFault().getMessage());
            System.exit(-1);
        }
    };
}
