package se.sics.gvod.stun.servermain;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.SelfNoParents;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.config.StunServerConfiguration;
import se.sics.gvod.stun.server.StunServer;
import se.sics.gvod.stun.server.events.StunServerInit;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.stun.msgs.ReportMsg;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;

/**
 * The
 * <code>Root</code> class
 *
 */
public final class StunServerMain extends ComponentDefinition {

    private static int STUN_SERVER_ID;
    private static final Logger logger = LoggerFactory.getLogger(StunServerMain.class);
    private static int SEED;
    private Component stunServer;
    private Component timer;
    private Component net;
    private Component resolveIp;
    int serverPort;
    int altServerPort;
    static List<String> partners = new ArrayList<String>();
    private Address serverAddr;
    private static Integer pickIp;

//------------------------------------------------------------------------    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: <prog> partner1 [0|1|2|..] (0=public, 1=private1, 2=private2)");
            System.exit(-1);
        }

        SEED = (int) System.currentTimeMillis();
        STUN_SERVER_ID = 1;
        partners.add(args[0]);
        pickIp = Integer.parseInt(args[1]);

        try {
            VodConfig.init(new String[0]);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(StunServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        // This initializes the Kompics runtime, and creates an instance of Root
        Kompics.createAndStart(StunServerMain.class);
    }

//------------------------------------------------------------------------    
    public StunServerMain() {
        timer = create(JavaTimer.class);
        net = create(NettyNetwork.class);
        stunServer = create(StunServer.class);
        resolveIp = create(ResolveIp.class);

        subscribe(handleStart, control);
        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
        subscribe(handleReportMsgRequest, net.getPositive(VodNetwork.class));

        serverPort = BaseCommandLineConfig.DEFAULT_STUN_PORT;
        altServerPort = BaseCommandLineConfig.DEFAULT_STUN_PORT_2;
    }
//------------------------------------------------------------------------    
    private Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            trigger(new GetIpRequest(false, EnumSet.of(
                    GetIpRequest.NetworkInterfacesMask.IGNORE_LOOPBACK,
                    GetIpRequest.NetworkInterfacesMask.IGNORE_PRIVATE)),
                    resolveIp.getPositive(ResolveIpPort.class));
        }
    };
//------------------------------------------------------------------------    
    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
        @Override
        public void handle(GetIpResponse event) {
            InetAddress addr = null;
            List<InetAddress> partnerIps = new ArrayList<InetAddress>();

            int mtu = 1500;

            try {
                if (pickIp > 0) {
                    addr = event.getTenDotIpAddress(pickIp);
                    if (addr == null) {
                        System.err.println("No 10.* IP address found. Exiting.");
                        System.exit(0);
                    }
                } else {
                    addr = event.getBoundIp();
                }

                for (String p : partners) {
                    InetAddress partnerIp = InetAddress.getByName(p);
                    partnerIps.add(partnerIp);
                    logger.info("Adding partner: " + partnerIp);
                }
            } catch (UnknownHostException ex) {
                logger.error("UnknownHostException for remote server: " + ex.toString());
                System.exit(-1);
            }

            logger.info("Binding to local ip: " + addr + " at ports: " + serverPort + ", " + altServerPort);
            serverAddr = new Address(addr, serverPort, STUN_SERVER_ID);
            Address serverAltAddr = new Address(addr, altServerPort, STUN_SERVER_ID);

            List<VodAddress> partnerAddrs = new ArrayList<VodAddress>();
            int i = 1;
            for (InetAddress ip : partnerIps) {
                Address a = new Address(ip, serverPort, i++);
                partnerAddrs.add(ToVodAddr.stunServer(a));
            }

            connect(stunServer.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(stunServer.getNegative(VodNetwork.class), net.getPositive(VodNetwork.class));

            StunServerConfiguration ssc = StunServerConfiguration.build().
                    setMsgTimeout(200).
                    setNumMsgRetries(8).
                    setMsgRetryScale(1.3);

            VodAddress gSa = ToVodAddr.stunServer(serverAddr);
            trigger(new StunServerInit(new SelfNoParents(gSa), partnerAddrs, ssc),
                    stunServer.getControl());
            trigger(new NettyInit(serverAddr, serverAltAddr, true, SEED, mtu), net.getControl());

        }
    };
    Handler<ReportMsg.Request> handleReportMsgRequest = new Handler<ReportMsg.Request>() {
        @Override
        public void handle(ReportMsg.Request msg) {
            logger.info("Receiving report.");
            logger.info(msg.getReport());
            trigger(new ReportMsg.Response(msg.getVodDestination(),
                    msg.getVodSource(), msg.getTimeoutId()), net.getPositive(VodNetwork.class));
        }
    };
}