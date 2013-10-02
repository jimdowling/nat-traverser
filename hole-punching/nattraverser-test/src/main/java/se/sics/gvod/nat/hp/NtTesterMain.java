package se.sics.gvod.nat.hp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import se.sics.gvod.common.msgs.NatReportMsg;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.hp.msgs.TConnectionMsg;
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
import se.sics.gvod.filters.MsgDestFilterNodeId;
import se.sics.gvod.nat.emu.DistributedNatGatewayEmulator;
import se.sics.gvod.nat.emu.events.DistributedNatGatewayEmulatorInit;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.kompics.Fault;
import se.sics.kompics.nat.utils.getip.IpAddrStatus;

/**
 * The
 * <code>Root</code> class
 *
 */
public final class NtTesterMain extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(NtTesterMain.class);
    private static final int OVERLAY_ID = 100;
    private static Address localAddress;
    private final Component timer;
    private final Component network;
    private final Component resolveIp;
    private final Component natTraverser;
    private Component natGateway;
    private final Component croupier;
    final NatTraverserConfiguration ntConfig;
    final RendezvousServerConfiguration rendezvousServerConfig;
    Self self;
    static Set<Address> servers = new HashSet<Address>();
    private static boolean upnpEnabled;
    private static int myId;
    private static int serverId;
    private static String server;
    private static Nat natType;
    private static InetAddress localIp = null;
    private static boolean openServer = false;
    private static Integer pickIp;
    private static Integer numFail = 0, numSuccess = 0;
    private Set<VodAddress> alreadyConnected = new HashSet<VodAddress>();
    private Map<Integer, TimeoutId> pangTimeouts = new HashMap<Integer, TimeoutId>();
    private Map<Integer, Long> startTimers = new HashMap<Integer, Long>();

    public static void exit() {
        logger.info("Usage: <prog> upnp id bindIp bootstrapNodeId@bootstrapNodeIp [openServer] [natGateway]");
        logger.info("       bindIp: 0=publicIp, 1=privateIp1, 2=privateIp2");
        logger.info("e.g.  <prog> true 111 0 1@cloud4.sics.se false");
        logger.info("To run bootstrap server:  <prog> false 1 0 1@cloud4.sics.se true");
        logger.info("To run a nat-emulated node:  <prog> false 1 0 1@cloud4.sics.se false m(EI)_a(PP)_f(PD)");
        System.exit(0);
    }

    public static void main(String[] args) {

        System.setProperty("java.net.preferIPv4Stack", "true");

        if (args.length < 3) {

            if (args.length == 1 && (args[0].compareToIgnoreCase("-help") == 0
                    || args[0].compareToIgnoreCase("-h") == 0)) {
            }
            upnpEnabled = false;
            Random r = new Random(System.currentTimeMillis());
            myId = r.nextInt();
            pickIp = 0;
            server = "cloud4.sics.se";
            serverId = 4;
        } else {
            upnpEnabled = Boolean.parseBoolean(args[0]);
            myId = Integer.parseInt(args[1]);
            pickIp = Integer.parseInt(args[2]);
            String serverStr = args[3];
            int idx = serverStr.lastIndexOf("@");
            if (idx == -1) {
                logger.info("bootstrapNodeId@bootstrapNodeIp format incorrect.");
                exit();
            }
            server = serverStr.substring(idx+1);
            serverId = Integer.parseInt(serverStr.substring(0, idx));
        }
        try {
            InetAddress serverIp = InetAddress.getByName(server);
            Address s = new Address(serverIp, VodConfig.DEFAULT_STUN_PORT, serverId);
            servers.add(s);
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(NtTesterMain.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if (args.length > 4) {
            openServer = Boolean.parseBoolean(args[4]);
        }
        if (args.length > 5 && args[5].compareTo(" ") != 0) {
            natType = Nat.parseToNat(args[5]);
            if (natType != null) {
                openServer = true;
            } else {
                System.err.println("Invalid nat type: " + args[5]);
                System.err.println("Example Nat format:  m(EI)_a(PP)_f(PD)");
            }
        }

        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            VodConfig.init(args);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(NtTesterMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        Kompics.createAndStart(NtTesterMain.class, 3);
    }

    public static class PingTimeout extends Timeout {

        private final VodAddress dest;

        public PingTimeout(ScheduleTimeout st, VodAddress dest) {
            super(st);
            this.dest = dest;
        }

        public VodAddress getDest() {
            return dest;
        }
    }

    public static class PangTimeout extends Timeout {

        private final int id;
        private final VodAddress dest;

        public PangTimeout(ScheduleTimeout st, int id, VodAddress dest) {
            super(st);
            this.id = id;
            this.dest = dest;
        }

        public VodAddress getDest() {
            return dest;
        }

        public int getId() {
            return id;
        }
    }

    public static class NtPortBindResponse extends PortBindResponse {

        public NtPortBindResponse(PortBindRequest request) {
            super(request);
        }
    }

    public NtTesterMain() throws IOException {

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
        connect(croupier.getNegative(VodNetwork.class), natTraverser.getPositive(VodNetwork.class));

        if (natType == null) {
            connect(natTraverser.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(natTraverser.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class)
                    ,new MsgDestFilterNodeId(myId)
                    );
            connect(natTraverser.getNegative(NatNetworkControl.class), network.getPositive(NatNetworkControl.class));
        } else {
            natGateway = create(DistributedNatGatewayEmulator.class);
            connect(natGateway.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(natGateway.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));
            connect(natGateway.getNegative(NatNetworkControl.class), network.getPositive(NatNetworkControl.class));

            connect(natTraverser.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(natTraverser.getNegative(VodNetwork.class), natGateway.getPositive(VodNetwork.class)
                    ,new MsgDestFilterNodeId(myId)                    
                    );
            connect(natTraverser.getNegative(NatNetworkControl.class), natGateway.getPositive(NatNetworkControl.class));
        }

        connect(resolveIp.getNegative(Timer.class), timer.getPositive(Timer.class));

        subscribe(handleStart, control);
        subscribe(handleGetNatTypeResponse, natTraverser.getPositive(NatTraverserPort.class));
        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
        subscribe(handlePing, natTraverser.getPositive(VodNetwork.class));
        subscribe(handlePong, natTraverser.getPositive(VodNetwork.class));
        subscribe(handlePang, natTraverser.getPositive(VodNetwork.class));
        subscribe(handleNtPortBindResponse, network.getPositive(NatNetworkControl.class));
        subscribe(handleFault, natTraverser.getControl());
        subscribe(handlePingTimeout, timer.getPositive(Timer.class));
        subscribe(handlePangTimeout, timer.getPositive(Timer.class));
        subscribe(handleCroupierSample, croupier.getPositive(PeerSamplePort.class));

    }
    private Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            if (pickIp != 0) {
                trigger(new GetIpRequest(false, EnumSet.of(
                        GetIpRequest.NetworkInterfacesMask.IGNORE_LOOPBACK)),
                        resolveIp.getPositive(ResolveIpPort.class));
            } else {
                trigger(new GetIpRequest(false, EnumSet.of(
                        GetIpRequest.NetworkInterfacesMask.IGNORE_LOOPBACK //                        , GetIpRequest.NetworkInterfacesMask.IGNORE_TEN_DOT_PRIVATE 
                        //,GetIpRequest.NetworkInterfacesMask.IGNORE_PRIVATE
                        )),
                        resolveIp.getPositive(ResolveIpPort.class));

            }
        }
    };
    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
        @Override
        public void handle(GetIpResponse event) {

            logger.info("Available IPs are:");
            for (IpAddrStatus a : event.getAddrs()) {
                logger.info(a.getAddr().toString());
            }

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
                logger.info("Using net i/f with ip address: " + localIp);

                localAddress = new Address(localIp, VodConfig.getPort(), myId);
                trigger(new NettyInit(VodConfig.getSeed(), true,
                        BaseMsgFrameDecoder.class), network.getControl());

                PortBindRequest pb1 = new PortBindRequest(localAddress, Transport.UDP);
                PortBindResponse pbr1 = new NtPortBindResponse(pb1);
                trigger(pb1, network.getPositive(NatNetworkControl.class));

            } else {
                logger.error("Couldnt find a network interface that is up");
                System.exit(-1);
            }
        }
    };
    private Handler<NtPortBindResponse> handleNtPortBindResponse =
            new Handler<NtPortBindResponse>() {
        @Override
        public void handle(NtPortBindResponse event) {

            if (event.getStatus() != NtPortBindResponse.Status.SUCCESS) {
                logger.error("Couldn't bind to port: " + event.getPort() + " - "
                        + event.getStatus());
                Kompics.shutdown();
                System.exit(-1);
            }
            self = new SelfImpl(null, localIp, event.getPort(),
                    localAddress.getId(), OVERLAY_ID);

            if (natType != null) {
                trigger(new DistributedNatGatewayEmulatorInit(natType, localIp,
                        2000, 65535), natGateway.getControl());
                self.setNat(natType);
            }


            trigger(new NatTraverserInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID),
                    servers, VodConfig.getSeed(),
                    NatTraverserConfiguration.build(),
                    HpClientConfiguration.build(),
                    RendezvousServerConfiguration.build(),
                    StunClientConfiguration.build().setUpnpEnable(upnpEnabled),
                    StunServerConfiguration.build()
                    .setRto(500)
                    .setRtoRetries(8)
                    .setRtoScale(1.2),
                    ParentMakerConfiguration.build(),
                    openServer),
                    natTraverser.getControl());

            trigger(new CroupierInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID),
                    CroupierConfiguration.build()),
                    croupier.getControl());
            startTimers.put(self.getId(), System.currentTimeMillis());
        }
    };
    private Handler<GetNatTypeResponse> handleGetNatTypeResponse =
            new Handler<GetNatTypeResponse>() {
        @Override
        public void handle(GetNatTypeResponse event) {

            logger.info("Nat type is: " + event.getNat());
            report(self.getPort(), self.getAddress(),
                    event.getStatus() == GetNatTypeResponse.Status.SUCCEED,
                    System.currentTimeMillis() - startTimers.get(self.getId()),
                    "Stun completed");
            List<VodDescriptor> svd = new ArrayList<VodDescriptor>();
            Address s = servers.iterator().next();
            VodAddress s1 = ToVodAddr.systemAddr(s);
            svd.add(new VodDescriptor(s1, new UtilityVod(0), 0, VodConfig.DEFAULT_MTU));
            trigger(new CroupierJoin(svd), croupier.getPositive(CroupierPort.class));
        }
    };
    public Handler<TConnectionMsg.Ping> handlePing =
            new Handler<TConnectionMsg.Ping>() {
        @Override
        public void handle(TConnectionMsg.Ping ping) {

            logger.info("ping recvd from "
                    + ping.getSource() + " at " + ping.getDestination() + " - "
                    + ping.getTimeoutId());
//            report("ping recvd from " + ping.getVodSource() + " at " + ping.getVodDestination()
//                    + "Success: " + numSuccess + "/" + numFail);
            report(ping.getDestination().getPort(), ping.getVodSource(),
                    true,
                    0,
                    "Incoming Nat Traversed");
            TConnectionMsg.Pong pong =
                    new TConnectionMsg.Pong(self.getAddress(),
                    ping.getVodSource(), ping.getTimeoutId());
            trigger(pong, natTraverser.getPositive(VodNetwork.class));

            ScheduleTimeout st = new ScheduleTimeout(10 * 1000);
            PangTimeout pt = new PangTimeout(st, ping.getTimeoutId().getId(),
                    ping.getVodSource());
            st.setTimeoutEvent(pt);
            trigger(st, timer.getPositive(Timer.class));
            pangTimeouts.put(ping.getTimeoutId().getId(), pt.getTimeoutId());
        }
    };
    public Handler<TConnectionMsg.Pong> handlePong =
            new Handler<TConnectionMsg.Pong>() {
        @Override
        public void handle(TConnectionMsg.Pong pong) {

            long timeTaken = System.currentTimeMillis() - (startTimers.get(pong.getSource().getId()));
            logger.info("pong recvd from " + pong.getSource() + " - " + pong.getTimeoutId()
                    + " time taken: " + timeTaken);
            numSuccess++;
            report(pong.getDestination().getPort(),
                    pong.getVodSource(), true,
                    timeTaken,
                    "Outgoing Nat Traversed");

            logger.info("Total Success/Failure ratio is: {}/{}", numSuccess, numFail);
            trigger(new CancelTimeout(pong.getTimeoutId()), timer.getPositive(Timer.class));

            TConnectionMsg.Pang pang =
                    new TConnectionMsg.Pang(self.getAddress(),
                    pong.getVodSource(), pong.getTimeoutId());
            trigger(pang, natTraverser.getPositive(VodNetwork.class));
        }
    };
    public Handler<TConnectionMsg.Pang> handlePang =
            new Handler<TConnectionMsg.Pang>() {
        @Override
        public void handle(TConnectionMsg.Pang pang) {
            TimeoutId pt = pangTimeouts.remove(pang.getMsgTimeoutId().getId());
            assert (pt != null);
            trigger(new CancelTimeout(pt), timer.getPositive(Timer.class));
            logger.info("pang recvd from " + pang.getSource() + " - " + pang.getMsgTimeoutId());
            numSuccess++;
            logger.info("Total Success/Failure ratio is: {}/{}", numSuccess, numFail);
        }
    };
    public Handler<Fault> handleFault =
            new Handler<Fault>() {
        @Override
        public void handle(Fault ex) {

            logger.debug(ex.getFault().toString());
            report(0, self.getAddress(), false, 0, ex.toString());
            System.exit(-1);
        }
    };
    Handler<PingTimeout> handlePingTimeout = new Handler<PingTimeout>() {
        @Override
        public void handle(PingTimeout msg) {
            logger.info("FAILURE: pong not recvd for TimeoutId: " + msg.getTimeoutId());
            numFail++;
            logger.info("Total Success/Failure ratio is: {}/{}", numSuccess, numFail);
            report(0, msg.getDest(), false, 0, "Nat Traversal Ping Timeout");
        }
    };
    Handler<PangTimeout> handlePangTimeout = new Handler<PangTimeout>() {
        @Override
        public void handle(PangTimeout timeout) {
            Integer pt = null;
            for (Integer t : pangTimeouts.keySet()) {
                if (pangTimeouts.get(t).equals(timeout)) {
                    pt = t;
                }
            }
            pangTimeouts.remove(pt);
            logger.info("FAILURE: pang not recvd for PingtimeoutId {} with PangTimeoutId: " + pt,
                    timeout.getId());
            numFail++;
            logger.info("Total Success/Failure ratio is: {}/{}", numSuccess, numFail);
            report(0, timeout.getDest(), false, 0, "Nat Traversal Pang Timeout");
        }
    };
    Handler<CroupierSample> handleCroupierSample = new Handler<CroupierSample>() {
        @Override
        public void handle(CroupierSample msg) {
            for (VodDescriptor sample : msg.getNodes()) {
                VodAddress dest = sample.getVodAddress();
                if (alreadyConnected.contains(dest) == false) {
                    // send a msg to the new VodAddress if it is private, has parents
                    // and I haven't tried to send a message to it before.
                    if (self.getId() != dest.getId()
                            && dest.isOpen() == false && dest.getParents().size() >= 1
                            && dest.isHpPossible(self.getAddress())) {
                        ScheduleTimeout st = new ScheduleTimeout(10 * 1000);
                        PingTimeout hp = new PingTimeout(st, dest);
                        st.setTimeoutEvent(hp);
                        trigger(new TConnectionMsg.Ping(self.getAddress(),
                                dest, hp.getTimeoutId()),
                                natTraverser.getPositive(VodNetwork.class));
                        trigger(st, timer.getPositive(Timer.class));
                        alreadyConnected.add(dest);
                        logger.info("sending ping with TimeoutId {} to {} ",
                                hp.getTimeoutId(), dest);
                        startTimers.put(dest.getId(), System.currentTimeMillis());
                    }
                }
            }
        }
    };

    private void report(int portUsed, VodAddress target, boolean success, long timeTaken,
            String str) {
        NatReportMsg.NatReport nr =
                new NatReportMsg.NatReport(portUsed, target, success, timeTaken, str);
        List<NatReportMsg.NatReport> nrs = new ArrayList<NatReportMsg.NatReport>();
        nrs.add(nr);
        VodAddress dest = ToVodAddr.bootstrap(VodConfig.getBootstrapServer());
        NatReportMsg msg = new NatReportMsg(self.getAddress(), dest, nrs);
        trigger(msg, network.getPositive(VodNetwork.class));
        logger.info("Reporting nat type msg to " + dest);
    }
}
