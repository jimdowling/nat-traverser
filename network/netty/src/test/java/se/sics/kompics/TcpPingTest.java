package se.sics.kompics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.address.Address;
import se.sics.gvod.common.Utility;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.gradient.msgs.SetsExchangeMsg;
import se.sics.gvod.hp.msgs.TConnectionMsg;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.net.events.PortBindResponse.Status;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;

/**
 * Unit test for simple App.
 */
public class TcpPingTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(SetsExchangeTest.class);
    private boolean testStatus = true;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TcpPingTest(String testName) {
        super(testName);
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TcpPingTest.class);
    }

    public static void setTestObj(TcpPingTest testObj) {
        TestStClientComponent.testObj = testObj;
    }

    public static class TestPortBindResponse extends PortBindResponse {
        public TestPortBindResponse(PortBindRequest request) {
            super(request);
        }
    }

    public static class TestStClientComponent extends ComponentDefinition {

        private Component client;
        private Component server;
        private Component timer;
        private static TcpPingTest testObj = null;
        private VodAddress clientAddr;
        private VodAddress serverAddr;
        private Utility utility = new UtilityVod(10, 200, 15);
        private VodDescriptor nodeDesc;
        private List<VodDescriptor> nodes;

        public TestStClientComponent() {
            timer = create(JavaTimer.class);
            client = create(NettyNetwork.class);
            server = create(NettyNetwork.class);

            InetAddress ip = null;
            int clientPort = 54644;
            int serverPort = 54645;

            try {
                ip = InetAddress.getByName("127.0.0.1");

            } catch (UnknownHostException ex) {
                logger.error("UnknownHostException");
                fail();
            }
            Address cAddr = new Address(ip, clientPort, 0);
            Address sAddr = new Address(ip, serverPort, 1);

            clientAddr = new VodAddress(cAddr, VodConfig.SYSTEM_OVERLAY_ID);
            serverAddr = new VodAddress(sAddr, VodConfig.SYSTEM_OVERLAY_ID);

            nodeDesc = new VodDescriptor(clientAddr, utility, 0, BaseCommandLineConfig.DEFAULT_MTU);
            nodes = new ArrayList<VodDescriptor>();
            nodes.add(nodeDesc);

            subscribe(handleStart, control);
            subscribe(handleMsgTimeout, timer.getPositive(Timer.class));
            subscribe(handlePong, client.getPositive(VodNetwork.class));
//            subscribe(handleTestPortBindResponse, client.getPositive(NatNetworkControl.class));
            subscribe(handleTestPortBindResponse, server.getPositive(NatNetworkControl.class));
//            subscribe(handlePing, server.getPositive(VodNetwork.class));

            trigger(new NettyInit(132, true,
                    BaseMsgFrameDecoder.class), client.getControl());
            trigger(new NettyInit(132, true,
                    BaseMsgFrameDecoder.class), server.getControl());


        }
        public Handler<Start> handleStart = new Handler<Start>() {
            public void handle(Start event) {
                System.out.println("Starting");
                ScheduleTimeout st = new ScheduleTimeout(1000 * 1000);
                SetsExchangeMsg.RequestTimeout mt = new SetsExchangeMsg.RequestTimeout(st,
                        serverAddr);
                st.setTimeoutEvent(mt);
//                PortBindRequest request = new PortBindRequest(0, serverAddr.getPort(),
//                        Transport.TCP);
//                request.setResponse(new PortBindResponse(request) {
//                });
//                trigger(request, server.getPositive(NatNetworkControl.class));
                trigger(st, timer.getPositive(Timer.class));
                
            PortBindRequest pb2 = new PortBindRequest(serverAddr.getPeerAddress(), 
                    Transport.TCP);
            PortBindResponse pbr2 = new TestPortBindResponse(pb2);
            trigger(pb2, server.getPositive(NatNetworkControl.class));
            }
        };
        public Handler<TestPortBindResponse> handleTestPortBindResponse 
                = new Handler<TestPortBindResponse>() {
            @Override
            public void handle(TestPortBindResponse event) {
                if (event.getStatus() != TestPortBindResponse.Status.SUCCESS) {
                    testObj.failAndRelease();
                }
                trigger(new TConnectionMsg.Ping(clientAddr, serverAddr, Transport.TCP, null),
                        client.getPositive(VodNetwork.class));
            }
        };
//        public Handler<PortBindResponse> handlePortBindResponse = new Handler<PortBindResponse>() {
//            @Override
//            public void handle(PortBindResponse event) {
//                System.out.println("Port bind response");
//
//                if (event.getStatus() == Status.FAIL) {
//                    testObj.failAndRelease();
//                    return;
//                }
//
//                trigger(new TConnectionMsg.Ping(clientAddr, serverAddr, Transport.TCP, null),
//                        client.getPositive(VodNetwork.class));
//            }
//        };
        public Handler<TConnectionMsg.Ping> handlePing = new Handler<TConnectionMsg.Ping>() {
            @Override
            public void handle(TConnectionMsg.Ping event) {
                System.out.println("Ping");
                trigger(new TConnectionMsg.Pong(serverAddr, clientAddr, Transport.TCP, null),
                        server.getPositive(VodNetwork.class));
            }
        };
        public Handler<TConnectionMsg.Pong> handlePong = new Handler<TConnectionMsg.Pong>() {
            @Override
            public void handle(TConnectionMsg.Pong event) {
                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                System.out.println("Pong");
                testObj.pass();
            }
        };
        public Handler<SetsExchangeMsg.RequestTimeout> handleMsgTimeout = new Handler<SetsExchangeMsg.RequestTimeout>() {
            public void handle(SetsExchangeMsg.RequestTimeout event) {
                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                System.out.println("Msg timeout");
                testObj.testStatus = false;
                testObj.failAndRelease();
            }
        };
    }
    private static final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    private void allTests() {
        runInstance();
        if (testStatus == true) {
            assertTrue(true);
        }
    }

    private void runInstance() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        Kompics.createAndStart(TestStClientComponent.class, 1);

        try {
            TcpPingTest.semaphore.acquire(EVENT_COUNT);
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

    @org.junit.Ignore
    public void testApp() {
        setTestObj(this);
        allTests();
    }

    public void pass() {
        TcpPingTest.semaphore.release();
    }

    public void failAndRelease() {
        testStatus = false;
        TcpPingTest.semaphore.release();
    }
}
