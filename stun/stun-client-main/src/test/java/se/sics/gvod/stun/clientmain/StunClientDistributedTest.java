package se.sics.gvod.stun.clientmain;

import se.sics.gvod.stun.client.StunPort;
import se.sics.gvod.stun.client.StunClient;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.nat.emu.DistributedNatGatewayEmulator;
import se.sics.gvod.nat.emu.NatGatewayConfiguration;
import se.sics.gvod.nat.emu.events.DistributedNatGatewayEmulatorInit;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.kompics.ChannelFilter;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.SelfNoParents;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.nat.emu.IpIntPair;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.stun.client.events.GetNatTypeRequest;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.gvod.stun.client.events.GetNatTypeResponseRuleExpirationTime;
import se.sics.gvod.stun.client.events.StunClientInit;
import se.sics.gvod.stun.upnp.UpnpPort;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.config.StunClientConfiguration;
import static se.sics.gvod.stun.clientmain.StunClientMain.MINIMUM_RTT;
import static se.sics.gvod.stun.clientmain.StunClientMain.MSG_RETRY_TIMEOUT;
import static se.sics.gvod.stun.clientmain.StunClientMain.RULE_EXPIRATION_INCREMENT;
import static se.sics.gvod.stun.clientmain.StunClientMain.RULE_EXPIRATION_TIMEOUT;
import static se.sics.gvod.stun.clientmain.StunClientMain.STUN_UPNP_TIMEOUT;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;

/**
 * Unit test for simple App.
 */
public class StunClientDistributedTest
        extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(StunClientDistributedTest.class);
    private boolean testStatus = true;
    public static final int UPNP_DISCOVERY_TIMEOUT = 2 * 1000;
    public static final int ROOT_DEVICE_TIMEOUT = 4 * 1000;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public StunClientDistributedTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(StunClientDistributedTest.class);
    }

    public static void setTestObj(StunClientDistributedTest testObj) {
        TestStClientComponent.testObj = testObj;
    }
    public static String[] natCombinations = {
        "UPNP",
        "NAT_EI_PC_EI", "NAT_EI_PC_HD",
        "NAT_EI_PC_PD", "NAT_EI_PP_EI_AltPC",
        "NAT_EI_PP_HD", "NAT_EI_PP_PD",
        "NAT_EI_RD_EI", "NAT_EI_RD_HD",
        "NAT_EI_RD_PD", "NAT_HD_PC_HD",
        "NAT_HD_PP_HD_AltPC", "NAT_HD_PP_HD_AltRD",
        "NAT_HD_RD_HD", "NAT_PD_PC_EI",
        "NAT_PD_PC_PD", "NAT_PD_PC_PD_AltPC",
        "NAT_PD_PC_PD_AltRD", "NAT_PD_RD_PD"
    };

    public static class MsgTimeout extends Timeout {

        public MsgTimeout(ScheduleTimeout request) {
            super(request);
        }
    }

    public static class TestStClientComponent extends ComponentDefinition {

        private Component stunClientComp;
        private Component natComp;
        private Component timer, net;
        private Component resolveIp;
//        private Component portReservoirComp;
//        private Component upnp;
        private static StunClientDistributedTest testObj = null;
        private Address privateAddress;
        private Address natAddress;
        private Address serverAddress;
        private int seed = 11;
        public static Nat.Type natType;
        public static Nat.AllocationPolicy allocationPolicy;
        public static Nat.AlternativePortAllocationPolicy alternativeAllocationPolicy;
        public static Nat.MappingPolicy mappingPolicy;
        public static Nat.FilteringPolicy filteringPolicy;
        public static int ruleLifeTime;
        NatGatewayConfiguration natGatewayConfiguration;

        public TestStClientComponent() {
            stunClientComp = create(StunClient.class);
            timer = create(JavaTimer.class);
            natComp = create(DistributedNatGatewayEmulator.class);
            net = create(NettyNetwork.class);
            resolveIp = create(ResolveIp.class);
//            upnp = create(UpnpComponent.class);


//            natGatewayConfiguration = new NatGatewayConfiguration
//                    (60*1000 /* rule expiration time*/,
//                    1 /*delta*/,
//                    0 /*seed*/,
//                    natType.ordinal() /*natType*/,
//                    mappingPolicy.ordinal(),
//                    allocationPolicy.ordinal(),
//                    filteringPolicy.ordinal(),
//                    alternativeAllocationPolicy.ordinal(),
//                    false /* upnp*/,
//                    9000 /*start port range */,
//                    10000 /*end port range*/,
//                    101 /*nat-id*/
//                    );
            subscribe(handleMsgTimeout, timer.getPositive(Timer.class));
            subscribe(handleGetNatTypeResponse, stunClientComp.getPositive(StunPort.class));
            subscribe(handleNatTypeResponseRuleTimeout, stunClientComp.getPositive(StunPort.class));
            subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));

            trigger(new GetIpRequest(false),
                    resolveIp.getPositive(ResolveIpPort.class));



        }
        public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
            @Override
            public void handle(GetIpResponse event) {

                InetAddress serverIp = null;
                InetAddress privateIp = null;
                int port = 3478;
                int serverS1Port = port;
                int clientPort = 8081;
                int clientID = 10;
                int natId = 100;
                InetAddress natIp = null;

                try {
//                privateIp = InetAddress.getByName("192.168.1.11");
//                    serverIp = InetAddress.getByName("cloud3.sics.se");
                    serverIp = InetAddress.getByName("b00t.info");
//                natIp = InetAddress.getByName("lucan.sics.se");
                    natIp = event.getBoundIp();
//                            InetAddress.getLocalHost();

                } catch (UnknownHostException ex) {
                    logger.error("UnknownHostException");
                    testObj.fail();
                }

                serverAddress = new Address(serverIp, serverS1Port, 1);
//            privateAddress = new Address(privateIp, clientPort, clientID);
                privateAddress = new Address(natIp, clientPort, clientID);
                natAddress = new Address(natIp, clientPort, natId);

                System.out.println("Private Addr: " + privateAddress + " Nat addr: " + natAddress);

                // no need to filter the packets based on port. a peer may have many opened ports
                final class MessageDestinationFilterBasedOnIPandID extends ChannelFilter<RewriteableMsg, IpIntPair> {

                    String message = "";

                    public MessageDestinationFilterBasedOnIPandID(IpIntPair kompicsIP, String msg) {
                        super(RewriteableMsg.class, kompicsIP, true);
                        message = msg;
                    }

                    @Override
                    public IpIntPair getValue(RewriteableMsg event) {
                        IpIntPair kompicsIP = new IpIntPair(event.getDestination().getIp(),
                                event.getDestination().getId());
                        //System.out.println(message+" FILTER is called. public point is " + kompicsIP + " event type is " + event.getClass().toString());
                        return kompicsIP;
                    }
                }

                connect(stunClientComp.getNegative(UpnpPort.class), natComp.getPositive(UpnpPort.class));
                connect(stunClientComp.getNegative(VodNetwork.class), natComp.getPositive(VodNetwork.class),
                        new MessageDestinationFilterBasedOnIPandID(new IpIntPair(privateAddress.getIp(), privateAddress.getId()), "clnt-nat"));
                connect(stunClientComp.getNegative(NatNetworkControl.class),
                        natComp.getPositive(NatNetworkControl.class));
                connect(stunClientComp.getNegative(Timer.class), timer.getPositive(Timer.class));

                connect(natComp.getNegative(VodNetwork.class), net.getPositive(VodNetwork.class));
                connect(natComp.getNegative(NatNetworkControl.class), net.getPositive(NatNetworkControl.class));
                connect(natComp.getNegative(Timer.class), timer.getPositive(Timer.class));

                trigger(new NettyInit(natAddress, false, seed), net.getControl());


                VodAddress sca = ToVodAddr.stunClient(privateAddress);

            StunClientConfiguration sc = StunClientConfiguration.build().
                    setRuleExpirationIncrement(RULE_EXPIRATION_INCREMENT).
                    setUpnpEnable(true).
                    setUpnpTimeout(STUN_UPNP_TIMEOUT).
                    setMsgTimeout(MSG_RETRY_TIMEOUT).
                    setRuleExpirationMinWait(RULE_EXPIRATION_TIMEOUT).
                    setMinimumRtt(MINIMUM_RTT).
                    setRandTolerance(10).
                    setRtoRetries(0);
            
            trigger(new StunClientInit(new SelfNoParents(sca),
                        0 /*seed*/,
                        sc), stunClientComp.getControl());


//            trigger(new UpnpInit(UPNP_DISCOVERY_TIMEOUT,
//                    ROOT_DEVICE_TIMEOUT), upnp.getControl());
//            trigger(new PortInit(0), portReservoirComp.getControl());

                ruleLifeTime = 40 * 1000;

//                trigger(new DistributedNatGatewayEmulatorInit(
//                        natGatewayConfiguration.getStartPortRange(),
//                        natGatewayConfiguration.getPortMappingPolicy(),
//                        natGatewayConfiguration.getPortAllocationPolicy(),
//                        natGatewayConfiguration.getAlternativePortAllocationPolicy(),
//                        natGatewayConfiguration.getFilteringPolicy(),
//                        natGatewayConfiguration.getNatType(), /* types are NAT OPEN and UPNP */
//                        5000 /* rule cleanup timer */,
//                        natIp ,
//                        natGatewayConfiguration.getEndPortRange() /* max port */,
//                        false /* clashing overrides */,
//                        natGatewayConfiguration.getRuleExpirationTime()/* rule life time */,
//                        (int) natGatewayConfiguration.getSeed() /*rand port seed*/,
//                        natId /*nat id Used in nat Address*/,
//                        true /* Upnp not enabled */), natComp.getControl());

                trigger(new DistributedNatGatewayEmulatorInit(
                        50000 /*start port range*/,
                        mappingPolicy,
                        allocationPolicy,
                        alternativeAllocationPolicy, /* alternaive policy if there is clash using PP */
                        filteringPolicy,
                        natType, /* types are NAT OPEN and UPNP */
                        300000 /* rule cleanup timer */,
                        natIp /* ip of NAT Gateway*/,
                        65533 /* max port */,
                        false /* clashing overrides */,
                        ruleLifeTime /* rule life time */,
                        55 /*rand port seed*/,
                        true /* upnp */), natComp.getControl());




                ScheduleTimeout st = new ScheduleTimeout(600 * 1000);
                MsgTimeout msgTimeout = new MsgTimeout(st);
                st.setTimeoutEvent(msgTimeout);

                Set<Address> servers = new HashSet<Address>();
                servers.add(serverAddress);
                trigger(new GetNatTypeRequest(servers, true),
                        stunClientComp.getPositive(StunPort.class));
                trigger(st, timer.getPositive(Timer.class));


            }
        };
        public Handler<GetNatTypeResponseRuleExpirationTime> handleNatTypeResponseRuleTimeout =
                new Handler<GetNatTypeResponseRuleExpirationTime>() {
            public void handle(GetNatTypeResponseRuleExpirationTime event) {
                System.out.println("Rule life time value is " + event.getRuleLifeTime());
                testObj.pass();
            }
        };
        public Handler<GetNatTypeResponse> handleGetNatTypeResponse =
                new Handler<GetNatTypeResponse>() {
            public void handle(GetNatTypeResponse event) {

//                        System.out.println("Nat type is " + event.getNatType().toString());
                Nat nat = event.getNat();
                System.out.println("Recvd " + nat);
                System.out.println("Actual T(" + TestStClientComponent.natType + "), M(" + TestStClientComponent.mappingPolicy + "), A("
                        + TestStClientComponent.allocationPolicy
                        + "), F(" + TestStClientComponent.filteringPolicy + "), AA("
                        + TestStClientComponent.alternativeAllocationPolicy
                        + ")");

                boolean pass = true;
                if (TestStClientComponent.natType == Nat.Type.UPNP) {
                    if (nat.getType() != Nat.Type.UPNP) {
                        pass = false;
                    } else {
                        System.out.println("Correct type. UPNP public ip is: " + nat.getPublicUPNPAddress());
                        testObj.pass();
                    }
                } else {

                    if (mappingPolicy == nat.getMappingPolicy()
                            && allocationPolicy == nat.getAllocationPolicy()
                            && filteringPolicy == nat.getFilteringPolicy()) {
                        if (allocationPolicy == Nat.AllocationPolicy.PORT_PRESERVATION) {
                            if (alternativeAllocationPolicy != nat.getAlternativePortAllocationPolicy()) {
                                pass = false;
                            }
                        }
                    } else {
                        pass = false;
                    }


                }

                if (pass) {
                    System.out.println("Correct nat type");
                } else {
                    System.out.println("Incorrect nat type");
                    testObj.fail(true);
                }

            }
        };
        public Handler<MsgTimeout> handleMsgTimeout = new Handler<MsgTimeout>() {
            public void handle(MsgTimeout event) {
                System.out.println("Msg timeout");
                testObj.fail(true);
            }
        };
    }
    private static final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    private void allTests() {
        System.out.println("Testing " + natCombinations.length + " NAT types");
        int i = 0;

        // for running few tests
//        String[] someTests = {"EI_PC_EI"};
//        natCombinations = someTests;

        for (String natType : natCombinations) {

            StringTokenizer stz = new StringTokenizer(natType, "_");

            String token = stz.nextToken();

            TestStClientComponent.natType = getNATType(token);
            if (TestStClientComponent.natType == Nat.Type.NAT) {
                TestStClientComponent.mappingPolicy = getMappingPolicy((String) stz.nextElement());
                TestStClientComponent.allocationPolicy = getAllocPolicy((String) stz.nextElement());
                TestStClientComponent.filteringPolicy = getFilteringPolicy((String) stz.nextElement());
                if (stz.hasMoreElements()) {
                    TestStClientComponent.alternativeAllocationPolicy = getAltAllocPolicy((String) stz.nextElement());
                } else {
                    TestStClientComponent.alternativeAllocationPolicy = getAltAllocPolicy("");
                }
            }


            System.out.println(i++ + ": T(" + TestStClientComponent.natType + "), M(" + TestStClientComponent.mappingPolicy + "), A("
                    + TestStClientComponent.allocationPolicy
                    + "), F(" + TestStClientComponent.filteringPolicy + "), AA("
                    + TestStClientComponent.alternativeAllocationPolicy
                    + ")\n********************************************************************\n");

            runInstance();
        }
        if (testStatus == true) {
            assertTrue(true);
        }
    }

    private void runInstance() {
        Kompics.createAndStart(TestStClientComponent.class, 4);
        try {
            StunClientDistributedTest.semaphore.acquire(EVENT_COUNT);
            System.out.println("Finished test.");
        } catch (InterruptedException e) {
            assert (false);
        } finally {
            Kompics.shutdown();
        }
        if (testStatus == false) {
            assertTrue(false);
        }

    }

    private Nat.MappingPolicy getMappingPolicy(String ap) {

        if (ap.compareTo("EI") == 0) {
            return Nat.MappingPolicy.ENDPOINT_INDEPENDENT;
        } else if (ap.compareTo("HD") == 0) {
            return Nat.MappingPolicy.HOST_DEPENDENT;
        } else if (ap.compareTo("PD") == 0) {
            return Nat.MappingPolicy.PORT_DEPENDENT;
        } else {
            return null;
        }
    }

    private Nat.AllocationPolicy getAllocPolicy(String ap) {
        if (ap.compareTo("PP") == 0) {
            return Nat.AllocationPolicy.PORT_PRESERVATION;
        } else if (ap.compareTo("PC") == 0) {
            return Nat.AllocationPolicy.PORT_CONTIGUITY;
        } else if (ap.compareTo("RD") == 0) {
            return Nat.AllocationPolicy.RANDOM;
        } else {
            return null;
        }
    }

    private Nat.FilteringPolicy getFilteringPolicy(String ap) {
        if (ap.compareTo("EI") == 0) {
            return Nat.FilteringPolicy.ENDPOINT_INDEPENDENT;
        } else if (ap.compareTo("HD") == 0) {
            return Nat.FilteringPolicy.HOST_DEPENDENT;
        } else if (ap.compareTo("PD") == 0) {
            return Nat.FilteringPolicy.PORT_DEPENDENT;
        } else {
            return null;
        }
    }

    private Nat.AlternativePortAllocationPolicy getAltAllocPolicy(String alt) {
        if (alt.compareTo("AltPC") == 0) {
            return Nat.AlternativePortAllocationPolicy.PORT_CONTIGUITY;
        } else if (alt.compareTo("AltRD") == 0) {
            return Nat.AlternativePortAllocationPolicy.RANDOM;
        } else {
            // default to continguity if none specified
            return Nat.AlternativePortAllocationPolicy.PORT_CONTIGUITY;
        }
    }

    private Nat.Type getNATType(String ap) {
        if (ap.compareTo("NAT") == 0) {
            return Nat.Type.NAT;
        } else if (ap.compareTo("UPNP") == 0) {
            return Nat.Type.UPNP;
        } else {
            return null;
        }
    }

    @org.junit.Ignore
    public void testApp() {
        setTestObj(this);

//        allTests();
    }

    public void pass() {
        StunClientDistributedTest.semaphore.release();
    }

    public void fail(boolean release) {
        testStatus = false;
        StunClientDistributedTest.semaphore.release();
    }
}
