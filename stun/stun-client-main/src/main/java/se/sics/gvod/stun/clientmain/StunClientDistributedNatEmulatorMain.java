package se.sics.gvod.stun.clientmain;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.NatNetworkControl;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.SelfNoParents;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.NettyInit;
import se.sics.kompics.nat.utils.ResultsLogger;
import se.sics.gvod.nat.emu.DistributedNatGatewayEmulator;
import se.sics.gvod.nat.emu.events.DistributedNatGatewayEmulatorInit;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.stun.client.StunClient;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.stun.client.StunPort;
import se.sics.gvod.stun.client.events.GetNatTypeRequest;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.gvod.stun.client.events.GetNatTypeResponseRuleExpirationTime;
import se.sics.gvod.stun.client.events.StunClientInit;
import se.sics.gvod.stun.upnp.UpnpPort;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;

/**
 * The <code>Root</code> class

 */
public final class StunClientDistributedNatEmulatorMain extends ComponentDefinition {

    public static final int UPNP_DISCOVERY_TIMEOUT = 2 * 1000;
    public static final int STUN_UPNP_TIMEOUT = 600 * 1000;
    public static final int ROOT_DEVICE_TIMEOUT = 1 * 1000;
    public static final int MSG_RETRY_TIMEOUT = 5 * 1000;
    public static final int RULE_EXPIRATION_TIMEOUT = 120 * 1000;
    public static final int RULE_EXPIRATION_INCREMENT = 60 * 1000;
    public static final int MINIMUM_RTT = 50;

    private static int CLIENT_PORT = 4444;
    private static final int CLIENT_ID = 10;
    private static int SEED;
    private static String SERVER;
    private static Logger logger;

    public static class MsgTimeout extends Timeout {

        public MsgTimeout(ScheduleTimeout request) {
            super(request);
        }
    }
    private Component stunClient;
    private Component timer;
    private Component nat;
    private Component network;
    private Component resolveIp;

    public static void main(String[] args) {
        // This initializes the Kompics runtime, and creates an instance of Root
        if (args.length < 1) {
            System.out.println("Usage: <prog> server [client-port]");
            System.exit(0);
        }

        SEED = (int) System.currentTimeMillis();
        SERVER = args[0];
        if (args.length > 1) {
            CLIENT_PORT = Integer.parseInt(args[2]);
        }
        int peerId = 0;
        logger = ResultsLogger.getLogger(peerId);

        Kompics.createAndStart(StunClientDistributedNatEmulatorMain.class, 1);
    }

    public StunClientDistributedNatEmulatorMain() {

        timer = create(JavaTimer.class);
        network = create(NettyNetwork.class);
        stunClient = create(StunClient.class);
        resolveIp = create(ResolveIp.class);
        nat = create(DistributedNatGatewayEmulator.class);

        connect(stunClient.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(stunClient.getNegative(NatNetworkControl.class),
                nat.getPositive(NatNetworkControl.class));
        connect(stunClient.getNegative(VodNetwork.class), nat.getPositive(VodNetwork.class));
        connect(stunClient.getNegative(UpnpPort.class), nat.getPositive(UpnpPort.class));

                
        connect(nat.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(nat.getNegative(NatNetworkControl.class),
                network.getPositive(NatNetworkControl.class));
        connect(nat.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));


        subscribe(handleStart, control);
        subscribe(handleGetNatTypeResponse, stunClient.getPositive(StunPort.class));
        subscribe(handleNatTypeResponseRuleTimeout, stunClient.getPositive(StunPort.class));
        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
        subscribe(handleFault, network.getControl());

//        trigger(new UpnpInit(UPNP_DISCOVERY_TIMEOUT, ROOT_DEVICE_TIMEOUT), upnp.getControl());

    }
    private Handler<Fault> handleFault = new Handler<Fault>() {

        @Override
        public void handle(Fault event) {
            logger.log(Level.SEVERE, "Fault: {0}", event.getFault().getMessage());
            Kompics.shutdown();
            System.exit(-1);
        }
    };
    private Handler<Start> handleStart = new Handler<Start>() {

        @Override
        public void handle(Start event) {

            // ignore planetlab apis
            trigger(new GetIpRequest(false),
                    resolveIp.getPositive(ResolveIpPort.class));

        }
    };
    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {

        @Override
        public void handle(GetIpResponse event) {


            Address server;
            try {
                server = new Address(InetAddress.getByName(SERVER), 
                        BaseCommandLineConfig.DEFAULT_STUN_PORT, 
                        BaseCommandLineConfig.DEFAULT_STUN_ID);
            } catch (UnknownHostException e) {
                System.err.println("Could not resolve hostname: " + SERVER);
                throw new IllegalArgumentException(e.toString());
            }


            InetAddress addr = null;
            addr = event.getBoundIp();
            logger.log(Level.INFO, "my ip is {0}\n", addr);
            Address stunClientAddress = new Address(addr, CLIENT_PORT, CLIENT_ID);

            trigger(new NettyInit(stunClientAddress, false, SEED), network.getControl());

            trigger(new DistributedNatGatewayEmulatorInit(
                    50000 /*start port range */,
                    Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                    Nat.AllocationPolicy.PORT_PRESERVATION,
                    Nat.AlternativePortAllocationPolicy.PORT_CONTIGUITY, /* alternaive policy if there is clash using PP */
                    Nat.FilteringPolicy.PORT_DEPENDENT,
                    Nat.Type.NAT, /* types are NAT OPEN and UPNP */
                    1*1000 /* rule cleanup timer */,
                    addr /* ip of NAT Gateway*/,
                    65533 /* max port */,
                    false /* clashing overrides */,
                    30000 /* rule life time */,
                    55 /*rand port seed*/,
                    true /* Upnp */
                    ), nat.getControl());
            

            VodAddress sca = ToVodAddr.stunClient(stunClientAddress);
            StunClientConfiguration sc = StunClientConfiguration.build().
                    setRuleExpirationIncrement(RULE_EXPIRATION_INCREMENT).
                    setUpnpEnable(false).
                    setRto(MSG_RETRY_TIMEOUT).
                    setRuleExpirationMinWait(RULE_EXPIRATION_TIMEOUT).
                    setMinimumRtt(MINIMUM_RTT).
                    setRandTolerance(10).
                    setRtoRetries(0);
            
            trigger(new StunClientInit(new SelfNoParents(sca), SEED, 
                    sc), stunClient.getControl());

            ScheduleTimeout st = new ScheduleTimeout(60 * 1000);
            MsgTimeout msgTimeout = new MsgTimeout(st);
            st.setTimeoutEvent(msgTimeout);

            Set<Address> servers = new HashSet<Address>();
            servers.add(server);
            trigger(new GetNatTypeRequest(servers, 1, true),
                    stunClient.getPositive(StunPort.class));
            trigger(st, timer.getPositive(Timer.class));

        }
    };
    private Handler<GetNatTypeResponse> handleGetNatTypeResponse =
            new Handler<GetNatTypeResponse>() {

        @Override
                public void handle(GetNatTypeResponse event) {

                    if (event.getStatus() != GetNatTypeResponse.Status.SUCCEED) {
                        System.out.println("UNABLE TO DETERMINE THE NAT TYPE, Status " + event.getStatus()); // 
                        logger.info("UNABLE TO DETERMINE THE NAT TYPE, Status " + event.getStatus() + "\n");
                    } else {
                        System.out.println("Nat type is: " + event.getNat());
                        logger.info("Nat type is: " + event.getNat() + "\n");
                    }

                    // only wait for rule expiration time, if it is natted.
                    if (event.getNat() == null || event.getNat().getType() != Nat.Type.NAT) {
                        Kompics.shutdown();
                        System.exit(0);
                    }
                }
            };
    public Handler<GetNatTypeResponseRuleExpirationTime> handleNatTypeResponseRuleTimeout =
            new Handler<GetNatTypeResponseRuleExpirationTime>() {

                public void handle(GetNatTypeResponseRuleExpirationTime event) {
                    System.out.println("Rule life time value is " + event.getRuleLifeTime());
                    logger.info("Rule life time value is " + event.getRuleLifeTime());
                    Kompics.shutdown();
                    System.exit(0);
                }
            };
}
