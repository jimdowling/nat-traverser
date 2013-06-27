package se.sics.gvod.stun.clientmain;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.NatNetworkControl;

import se.sics.kompics.Component;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfImpl;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.stun.client.StunClient;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.stun.client.StunPort;
import se.sics.gvod.stun.client.events.GetNatTypeRequest;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.gvod.stun.client.events.GetNatTypeResponseRuleExpirationTime;
import se.sics.gvod.stun.client.events.StunClientInit;
import se.sics.gvod.stun.msgs.ReportMsg;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.kompics.ComponentDefinition;

/**
 * The
 * <code>Root</code> class
 *
 */
public final class StunClientMain extends ComponentDefinition {

    private static Logger logger = Logger.getLogger(StunClientMain.class);
    public static final int UPNP_DISCOVERY_TIMEOUT = 3 * 1000;
    public static final int STUN_UPNP_TIMEOUT = 6 * 1000;
    public static final int ROOT_DEVICE_TIMEOUT = 1 * 1000;
    public static final int MSG_RETRY_TIMEOUT = 1 * 1000;
    public static final int RULE_EXPIRATION_TIMEOUT = 60 * 1000;
    public static final int RULE_EXPIRATION_INCREMENT = 60 * 1000;
    public static final int MINIMUM_RTT = 50;
    private static int CLIENT_PORT = 4444;
    private static final int CLIENT_ID = 0;
    private static int SEED;
    private static String SERVER;
    private static boolean ENABLE_UPNP = false;
    private static boolean NAT_BINDING_TIMEOUT_MEASURE = false;
    private Component stunClient;
    private Component timer;
    private Component network;
    private Component resolveIp;
    private Address server;
    private Self self;
    private int retries = 3;
    private static Integer pickIp;
//------------------------------------------------------------------------    

    public static class MsgTimeout extends Timeout {

        public MsgTimeout(ScheduleTimeout request) {
            super(request);
        }
    }

//------------------------------------------------------------------------    
    public static void main(String[] args) {
        // This initializes the Kompics runtime, and creates an instance of Root
        if (args.length < 3) {
            System.out.println("Usage: <prog> server enable-upnp 0|1|2 "
                    + "(publicIp|privIp1|privIp2) [enable-natBindingTimeoutMeasure] [clientPort] ");
            System.exit(0);
        }

        SEED = (int) System.currentTimeMillis();
        SERVER = args[0];
        if (args.length > 1) {
            ENABLE_UPNP = Boolean.parseBoolean(args[1]);
        }
        if (args.length > 2) {
            pickIp = Integer.parseInt(args[2]);
        }
        if (args.length > 3) {
            NAT_BINDING_TIMEOUT_MEASURE = Boolean.parseBoolean(args[3]);
        }
        if (args.length > 4) {
            CLIENT_PORT = Integer.parseInt(args[4]);
        }
        try {
            VodConfig.init(new String[0]);
        } catch (IOException ex) {
            logger.warn(ex.toString());
        }

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(StunClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        Kompics.createAndStart(StunClientMain.class, 2);
    }

    public StunClientMain() {
        timer = create(JavaTimer.class);
        network = create(NettyNetwork.class);
        stunClient = create(StunClient.class);
        resolveIp = create(ResolveIp.class);

        connect(stunClient.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(stunClient.getNegative(NatNetworkControl.class), network.getPositive(NatNetworkControl.class));
        connect(stunClient.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));

        subscribe(handleStart, control);
        subscribe(handleGetNatTypeResponse, stunClient.getPositive(StunPort.class));
        subscribe(handleNatTypeResponseRuleTimeout, stunClient.getPositive(StunPort.class));
        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
        subscribe(handleFault, network.getControl());
        subscribe(handleReportMsgResponse, network.getPositive(VodNetwork.class));
        subscribe(handleReportMsgRequestTimeout, timer.getPositive(Timer.class));

    }
//------------------------------------------------------------------------    
    private Handler<Fault> handleFault = new Handler<Fault>() {
        @Override
        public void handle(Fault event) {
            logger.warn("Fault: " + event.getFault().getMessage());
            Kompics.shutdown();
            System.exit(-1);
        }
    };
//------------------------------------------------------------------------    
    private Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            // ignore planetlab apis
            if (pickIp == 0) {
                trigger(new GetIpRequest(false, EnumSet.of(
                        GetIpRequest.NetworkInterfacesMask.IGNORE_LOOPBACK,
                        GetIpRequest.NetworkInterfacesMask.IGNORE_TEN_DOT_PRIVATE,
                        GetIpRequest.NetworkInterfacesMask.IGNORE_PRIVATE)),
                        resolveIp.getPositive(ResolveIpPort.class));
            } else {
                trigger(new GetIpRequest(false, EnumSet.of(
                        GetIpRequest.NetworkInterfacesMask.IGNORE_LOOPBACK,
                        GetIpRequest.NetworkInterfacesMask.IGNORE_PRIVATE)),
                        resolveIp.getPositive(ResolveIpPort.class));
            }
        }
    };
//------------------------------------------------------------------------    
    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
        @Override
        public void handle(GetIpResponse event) {

            try {
                server = new Address(InetAddress.getByName(SERVER), VodConfig.DEFAULT_STUN_PORT,
                        VodConfig.DEFAULT_STUN_ID);
            } catch (UnknownHostException e) {
                System.err.println("Could not resolve hostname: " + SERVER);
                throw new IllegalArgumentException(e.toString());
            }

            InetAddress addr;

            if (pickIp > 0) {
                addr = event.getTenDotIpAddress(pickIp);
                if (addr == null) {
                    System.err.println("No 10.* IP address found. Exiting.");
                    System.exit(0);
                }
            } else {
                addr = event.getBoundIp();
            }

//            addr = event.getIpAddress();
            logger.info("my ip is " + addr);
            Address stunClientAddress = new Address(addr, CLIENT_PORT, CLIENT_ID);
            trigger(new NettyInit(stunClientAddress, true, SEED), network.getControl());

            VodAddress sca = ToVodAddr.stunClient(stunClientAddress);

            StunClientConfiguration sc = StunClientConfiguration.build().
                    setRuleExpirationIncrement(RULE_EXPIRATION_INCREMENT).
                    setUpnpEnable(ENABLE_UPNP).
                    setUpnpTimeout(STUN_UPNP_TIMEOUT).
                    setRto(MSG_RETRY_TIMEOUT).
                    setRuleExpirationMinWait(RULE_EXPIRATION_TIMEOUT).
                    setMinimumRtt(MINIMUM_RTT).
                    setRandTolerance(10).
                    setRtoRetries(20).
                    setRtoScale(1.0);

            self = new SelfImpl(sca);
            trigger(new StunClientInit(self, SEED,
                    sc), stunClient.getControl());

            ScheduleTimeout st = new ScheduleTimeout(60 * 1000);
            MsgTimeout msgTimeout = new MsgTimeout(st);
            st.setTimeoutEvent(msgTimeout);

            Set<Address> servers = new HashSet<Address>();
            servers.add(server);
            trigger(new GetNatTypeRequest(servers, NAT_BINDING_TIMEOUT_MEASURE), stunClient.getPositive(StunPort.class));
            trigger(st, timer.getPositive(Timer.class));
        }
    };
    private Handler<GetNatTypeResponse> handleGetNatTypeResponse = new Handler<GetNatTypeResponse>() {
        @Override
        public void handle(GetNatTypeResponse event) {
            if (event.getStatus() != GetNatTypeResponse.Status.SUCCEED) {
                logger.info("UNABLE TO DETERMINE THE NAT TYPE, Status " + event.getStatus());
            } else {
                logger.info("Nat type is: " + event.getNat());
            }

            self.setNat(event.getNat());

            String report = "REPORT:\t" + event.getStatus() + " - " + self.getAddress() + "\n"
                    + event.getNat();
            logger.info(report);
            sendReport(report);

            if (event.getNat() == null || event.getStatus() != GetNatTypeResponse.Status.NO_UPNP) {
                Kompics.shutdown();
                System.exit(0);
            }
        }
    };

    private void sendReport(String report) {
        logger.info("Send Nat Type report to server: " + server);

        ScheduleTimeout srt = new ScheduleTimeout(5000);
        ReportMsg.RequestTimeout rt = new ReportMsg.RequestTimeout(srt);
        srt.setTimeoutEvent(rt);
        ReportMsg.Request r = new ReportMsg.Request(self.getAddress(),
                ToVodAddr.stunServer(server), rt.getTimeoutId(), report);
        rt.setRequestMsg(r);
        trigger(r, network.getPositive(VodNetwork.class));
        trigger(srt, timer.getPositive(Timer.class));
    }
    public Handler<ReportMsg.Response> handleReportMsgResponse =
            new Handler<ReportMsg.Response>() {
        @Override
        public void handle(ReportMsg.Response msg) {
            logger.info("Successfully sent STUN report to server: " + server);
            trigger(new CancelTimeout(msg.getTimeoutId()), timer.getPositive(Timer.class));

            if (self.getNat().isOpen()) {
                Kompics.shutdown();
                System.exit(0);
            }
        }
    };
    public Handler<ReportMsg.RequestTimeout> handleReportMsgRequestTimeout =
            new Handler<ReportMsg.RequestTimeout>() {
        @Override
        public void handle(ReportMsg.RequestTimeout msg) {
            retries--;
            if (retries > 0) {
                sendReport(msg.getRequestMsg().getReport());
            } else {
                logger.warn("Could not send STUN report to server: " + server);
                Kompics.shutdown();
                System.exit(0);
            }
        }
    };
    public Handler<GetNatTypeResponseRuleExpirationTime> handleNatTypeResponseRuleTimeout = new Handler<GetNatTypeResponseRuleExpirationTime>() {
        public void handle(GetNatTypeResponseRuleExpirationTime event) {
            logger.info("Rule life time value is " + event.getRuleLifeTime());
            sendReport(self.getAddress() + " - Rule life time value is " + event.getRuleLifeTime());
            Kompics.shutdown();
            System.exit(0);
        }
    };
}
