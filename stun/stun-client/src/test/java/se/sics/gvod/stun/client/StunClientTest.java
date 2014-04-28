package se.sics.gvod.stun.client;

import java.io.IOException;
import se.sics.gvod.config.StunServerConfiguration;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfNoParents;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.nat.common.PortInit;
import se.sics.gvod.nat.common.PortReservoirComp;
import se.sics.gvod.nat.emu.DistributedNatGatewayEmulator;
import se.sics.gvod.nat.emu.IpIntPair;
import se.sics.gvod.nat.emu.events.DistributedNatGatewayEmulatorInit;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.stun.client.events.GetNatTypeRequest;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.gvod.stun.client.events.GetNatTypeResponseRuleExpirationTime;
import se.sics.gvod.stun.client.events.StunClientInit;
import se.sics.gvod.stun.client.simulator.NetworkSimulator;
import se.sics.gvod.stun.client.simulator.NetworkSimulatorInit;
import se.sics.gvod.stun.server.StunServer;
import se.sics.gvod.stun.server.events.StunServerInit;
import se.sics.kompics.ChannelFilter;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;

/**
 * Unit test for simple App.
 */
public class StunClientTest {

    private static final Logger logger = LoggerFactory.getLogger(StunClientTest.class);
    private static Semaphore semaphore = new Semaphore(0);
    int testNumber = 0;
    boolean res = true;

    public static class StunClientComponentTester extends ComponentDefinition {

        private static final Logger logger = LoggerFactory.getLogger(StunClientComponentTester.class);
        private Component stunClientComp;
        private List<Component> serverS1Components = new ArrayList<Component>();
        private List<Component> serverS2Components = new ArrayList<Component>();
        private Component natComp;
        private Component timer, networkSimulator;
        private Component portReservoir_For_A, portReservoir_For_B;
        public static Nat nat;
        public static int ruleLifeTime;
        public static StunClientTest testObj = null;
        private Address stunClientAddress;
        int clientPort;
        static int clientID = 0;
        InetAddress natIP = null;
        int natID;
        private List<Address> serverS1Addresses = new ArrayList<Address>();
        private List<Address> serverS2Addresses = new ArrayList<Address>();
        int serverS1Port;
        int serverS1ChangePort = 3479;
        int serverS2Port;
        int serverS2ChangePort = 3479;
        public static int numberOfPairServers = 2;
        public static int numberOfSingleServers = 1;

        public StunClientComponentTester() {
            stunClientComp = create(StunClient.class);
            timer = create(JavaTimer.class);
            natComp = create(DistributedNatGatewayEmulator.class);
            networkSimulator = create(NetworkSimulator.class);
            portReservoir_For_A = create(PortReservoirComp.class);
            portReservoir_For_B = create(PortReservoirComp.class);

            subscribe(handleStart, control);
            subscribe(handleMsgTimeout, timer.getPositive(Timer.class));
            subscribe(handleGetNatTypeResponse, stunClientComp.getPositive(StunPort.class));
            subscribe(handleNatTypeResponseRuleTimeout, stunClientComp.getPositive(StunPort.class));
        }
        public Handler<Start> handleStart = new Handler<Start>() {
            @Override
            public void handle(Start event) {

                try {
                    InetAddress clientIp = InetAddress.getByName("192.168.0.1");
                    clientPort = 8081;
                    stunClientAddress = new Address(clientIp, clientPort, clientID++);

                    natID = 100;
                    natIP = InetAddress.getByName("192.168.0.2");;

                    int i = 1;

                    for (; i <= numberOfPairServers; i++) {
                        String server1Ip = "192.168." + i + ".1";
                        InetAddress server1Addr = InetAddress.getByName(server1Ip);
                        int serverS1Port = 3478;
                        int serverS1Id = Integer.valueOf(i + 100 + "1");
                        serverS1Addresses.add(new Address(server1Addr, serverS1Port, serverS1Id));
                        serverS1Components.add(create(StunServer.class));

                        String server2Ip = "192.168." + i + ".2";
                        InetAddress server2Addr = InetAddress.getByName(server2Ip);
                        int serverS2Port = 3478;
                        int serverS2Id = Integer.valueOf(i + 100 + "2");
                        serverS2Addresses.add(new Address(server2Addr, serverS2Port, serverS2Id));
                        serverS2Components.add(create(StunServer.class));
                    }

                    for (; i <= numberOfPairServers + numberOfSingleServers; i++) {
                        String server1Ip = "192.168." + i + ".1";
                        InetAddress server1Addr = InetAddress.getByName(server1Ip);
                        int serverS1Port = 3478;
                        serverS1Addresses.add(new Address(server1Addr, serverS1Port, 1));
                        serverS1Components.add(create(StunServer.class));
                    }

                } catch (UnknownHostException ex) {
                    testObj.fail("Could not acquire ip addresses");
                }

                connect(natComp.getNegative(VodNetwork.class), networkSimulator.getPositive(VodNetwork.class), new MessageDestinationFilterBasedOnIp(natIP));

                connect(stunClientComp.getNegative(VodNetwork.class), natComp.getPositive(VodNetwork.class), new MessageDestinationFilterBasedOnIPandID(new IpIntPair(stunClientAddress.getIp(),
                        stunClientAddress.getId())));
                connect(natComp.getNegative(Timer.class), timer.getPositive(Timer.class));


                for (Address serverS1Address : serverS1Addresses) {
                    int index = serverS1Addresses.indexOf(serverS1Address);

                    connect(serverS1Components.get(index).getNegative(VodNetwork.class), networkSimulator.getPositive(VodNetwork.class), new MessageDestinationFilterBasedOnIp(serverS1Address.getIp()));
                    connect(serverS1Components.get(index).getNegative(Timer.class), timer.getPositive(Timer.class));

                    if (index < serverS2Addresses.size()) {
                        Address serverS2Address = serverS2Addresses.get(index);
                        connect(serverS2Components.get(index).getNegative(VodNetwork.class), networkSimulator.getPositive(VodNetwork.class), new MessageDestinationFilterBasedOnIp(serverS2Address.getIp()));
                        connect(serverS2Components.get(index).getNegative(Timer.class), timer.getPositive(Timer.class));
                    }
                }

                connect(stunClientComp.getNegative(Timer.class), timer.getPositive(Timer.class));

                connect(stunClientComp.getNegative(NatNetworkControl.class),
                        natComp.getPositive(NatNetworkControl.class));
                connect(natComp.getNegative(NatNetworkControl.class),
                        portReservoir_For_A.getPositive(NatNetworkControl.class));

                trigger(new NetworkSimulatorInit(), networkSimulator.getControl());



                for (Address serverS1Address : serverS1Addresses) {

                    int index = serverS1Addresses.indexOf(serverS1Address);
                    List<VodAddress> altAddrs1 = new ArrayList<VodAddress>();

                    if (index < serverS2Addresses.size()) {
                        Address serverS2Address = serverS2Addresses.get(index);
                        VodAddress s2 = ToVodAddr.stunServer(serverS2Address);
                        altAddrs1.add(s2);

                        List<VodAddress> altAddrs2 = new ArrayList<VodAddress>();
                        VodAddress s1 = ToVodAddr.stunServer(serverS1Address);
                        altAddrs2.add(s1);
                        trigger(new StunServerInit(new SelfNoParents(s2), 
                                altAddrs2, StunServerConfiguration.build()), 
                                serverS2Components.get(index).getControl());

                    }
                    VodAddress s1 = ToVodAddr.stunServer(serverS1Address);
                    trigger(new StunServerInit(new SelfNoParents(s1),
                            altAddrs1, StunServerConfiguration.build()),
                            serverS1Components.get(index).getControl());
                }


                boolean upnpEnabled = (nat.getType() == Nat.Type.UPNP);

                ruleLifeTime = 2 * 1000;
                VodAddress sca = ToVodAddr.stunClient(stunClientAddress);
                Self self = new SelfNoParents(sca);


                trigger(new StunClientInit(self, 0 /* seed */,
                        StunClientConfiguration.build().
                        setRandTolerance(1).
                        setRuleExpirationMinWait(ruleLifeTime).
                        setRuleExpirationIncrement(ruleLifeTime).
                        setUpnpEnable(upnpEnabled).
                        setUpnpTimeout(500).
                        setMinimumRtt(500).
                        setRto(500).
                        setRtoRetries(0)), stunClientComp.getControl());
                ruleLifeTime = ruleLifeTime + (ruleLifeTime / 2);


                trigger(new DistributedNatGatewayEmulatorInit(50000,
                        nat.getMappingPolicy(),
                        nat.getAllocationPolicy(),
                        nat.getAlternativePortAllocationPolicy(), /* alternaive policy if there is clash using PP */
                        nat.getFilteringPolicy(),
                        nat.getType(), /* types are NAT OPEN and UPNP */
                        3 * 1000 /* gc */,
                        natIP /* ip of NAT Gateway*/,
                        65533,
                        false, /*mapping overrides*/
                        ruleLifeTime,
                        55,
                        upnpEnabled/* upnp */), natComp.getControl());

                trigger(new PortInit(11), portReservoir_For_A.getControl());
                trigger(new PortInit(2992), portReservoir_For_B.getControl());

                Set<Address> serverAddresses = new LinkedHashSet<Address>();
                serverAddresses.addAll(serverS1Addresses);

                trigger(new GetNatTypeRequest(serverAddresses, true),
                        stunClientComp.getPositive(StunPort.class));
                ScheduleTimeout st = new ScheduleTimeout(ruleLifeTime * 20);
                MsgTimeout mt = new MsgTimeout(st);
                st.setTimeoutEvent(mt);
                trigger(st, timer.getPositive(Timer.class));
            }
        };
        public Handler<GetNatTypeResponseRuleExpirationTime> handleNatTypeResponseRuleTimeout =
                new Handler<GetNatTypeResponseRuleExpirationTime>() {
            @Override
            public void handle(GetNatTypeResponseRuleExpirationTime event) {
                logger.info("Rule life time value is " + event.getRuleLifeTime());
                testObj.pass();
            }
        };
        public Handler<GetNatTypeResponse> handleGetNatTypeResponse =
                new Handler<GetNatTypeResponse>() {
            @Override
            public void handle(GetNatTypeResponse event) {

                Nat determinedNat = event.getNat();

                if (determinedNat != null) {
                    logger.info("Recvd " + determinedNat);
                    logger.info("Actual " + nat);
                    if (nat.getType() == Nat.Type.UPNP) {
                        if (determinedNat.getType() != Nat.Type.UPNP) {
                            testObj.fail("Upnp is not discovered correctly!!");
                        } else {
                            logger.info("Correct type. UPNP public ip is: " + determinedNat.getPublicUPNPAddress());
                            testObj.pass();
                        }
                    } else {
                        if (nat.getMappingPolicy() == determinedNat.getMappingPolicy()
                                && nat.getAllocationPolicy() == determinedNat.getAllocationPolicy()
                                && nat.getFilteringPolicy() == determinedNat.getFilteringPolicy()) {
                            logger.info("Found correct NAT type!");
                            // Only finish test when rule expiration time has been calculated
                        } else {
                            testObj.fail("Incorrect Nat type: " + nat.toString() + " was discovered as " + determinedNat.toString());
                        }
                    }
                } else {
                    if (event.getStatus() == GetNatTypeResponse.Status.ALL_HOSTS_TIMED_OUT) {
                        logger.info("All hosts timed out!!");
                        testObj.pass();
                    } else if (event.getStatus() == GetNatTypeResponse.Status.FAIL) {
                        testObj.fail("General failure getting nat type " + event.getStatus());
                    } else {
                        testObj.fail("Some problem with getting nat type. Response is: "
                                + event.getStatus());
                    }
                }

                logger.info("Correct nat type");
            }
        };
        public Handler<MsgTimeout> handleMsgTimeout = new Handler<MsgTimeout>() {
            @Override
            public void handle(MsgTimeout event) {
                testObj.fail("Stun client did not answer in the expected time limit.");
            }
        };

        private class MsgTimeout extends Timeout {

            public MsgTimeout(ScheduleTimeout request) {
                super(request);
            }
        }

        // no need to filter the packets based on port. a peer may have many opened ports
        final class MessageDestinationFilterBasedOnIPandID extends ChannelFilter<DirectMsg, IpIntPair> {

            public MessageDestinationFilterBasedOnIPandID(IpIntPair IpIntPair) {
                super(DirectMsg.class, IpIntPair, true);
            }

            @Override
            public IpIntPair getValue(DirectMsg event) {
                IpIntPair IpIntPair = new IpIntPair(event.getDestination().getIp(),
                        event.getDestination().getId());
                return IpIntPair;
            }
        }

        final class MessageDestinationFilterBasedOnIp extends ChannelFilter<DirectMsg, InetAddress> {

            public MessageDestinationFilterBasedOnIp(InetAddress ip) {
                super(DirectMsg.class, ip, true);
            }

            @Override
            public InetAddress getValue(DirectMsg event) {
                return event.getDestination().getIp();
            }
        }
    }

    @Before
    public void init() {
        StunClientComponentTester.testObj = this;
        try {
            VodConfig.init(new String[0]);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(StunClientTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    @Ignore
    public void testUpnp() {
//        TestStunClientComponent.nat = new Nat(Nat.Type.UPNP, , Nat.MappingPolicy.OPEN, Nat.AllocationPolicy.OPEN, Nat.FilteringPolicy.OPEN, Nat.AlternativePortAllocationPolicy.OPEN);
        logger.info("Testing upnp");
        runInstance();
    }

    @Test
    public void testAllNatCombinationsWithMultipleServers() {
        List<Nat> allNatCombinations = Nat.getAllNatTypes();
        logger.info("Testing " + allNatCombinations.size() + " NAT types");

        StunClientComponentTester.numberOfPairServers = 1;
        StunClientComponentTester.numberOfSingleServers = 0;

        for (Nat nat : allNatCombinations) {

            StunClientComponentTester.nat = nat;

            logger.info("\n\n\n\n\n\n\n\n\n\n\n\n" + ++testNumber + ":  " + nat.toString()
                    + "\n********************************************************************");


            runInstance();
        }
    }

    private void runInstance() {
        Kompics.createAndStart(StunClientComponentTester.class, 1);
        try {
            StunClientTest.semaphore.acquire(1);
            if (!res) {
                Assert.fail();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            StunClientTest.semaphore.release();
            Assert.fail(e.getMessage());
        } finally {
            Kompics.shutdown();
        }
    }

    public void pass() {
        StunClientTest.semaphore.release();
    }

    public void fail(String message) {
        logger.error("test #" + testNumber + " fails.");
        res = false;
        StunClientTest.semaphore.release();
    }
}
