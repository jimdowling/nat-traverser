package se.sics.kompics;

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
import se.sics.gvod.hp.msgs.TConnectionMsg;
import se.sics.gvod.net.*;
import se.sics.gvod.net.events.*;
import se.sics.gvod.net.events.PortBindResponse.Status;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.UUID;
import se.sics.gvod.timer.java.JavaTimer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Simple ping pong test for UDT.
 *
 * @author Steffen Grohsschmiedt
 */
public class UdtPingTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(UdtPingTest.class);
    private boolean testStatus = true;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public UdtPingTest(String testName) {
        super(testName);
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(UdtPingTest.class);
    }

    public static void setTestObj(UdtPingTest testObj) {
        TestStClientComponent.testObj = testObj;
    }

    public static class TestStClientComponent extends ComponentDefinition {

        private Component client;
        private Component server;
        private Component timer;
        private static UdtPingTest testObj = null;
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
                ip = InetAddress.getByName("localhost");

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
            subscribe(handlePortBindResponse, server.getPositive(NatNetworkControl.class));
            subscribe(handlePing, server.getPositive(VodNetwork.class));
            subscribe(handlePong, client.getPositive(VodNetwork.class));
            subscribe(handleCloseConnectionResponse, client.getPositive(NatNetworkControl.class));
            subscribe(handlePortDeletionResponse, server.getPositive(NatNetworkControl.class));

            trigger(new NettyInit(132, true, BaseMsgFrameDecoder.class),
                    client.getControl());
            trigger(new NettyInit(132, true, BaseMsgFrameDecoder.class),
                    server.getControl());
        }

        public Handler<Start> handleStart = new Handler<Start>() {

            public void handle(Start event) {
                logger.info("Starting");
                ScheduleTimeout st = new ScheduleTimeout(30 * 1000);
                MsgTimeout mt = new MsgTimeout(st);
                st.setTimeoutEvent(mt);
                PortBindRequest request =
                        new PortBindRequest(serverAddr.getPeerAddress(),
                                Transport.UDT);
                request.setResponse(new PortBindResponse(request) {
                });
                trigger(request, server.getPositive(NatNetworkControl.class));
                trigger(st, timer.getPositive(Timer.class));
            }
        };
        public Handler<PortBindResponse> handlePortBindResponse = new Handler<PortBindResponse>() {

            @Override
            public void handle(PortBindResponse event) {
                logger.info("Port bind response");

                if (event.getStatus() == Status.FAIL) {
                    testObj.failAndRelease();
                    return;
                }

                trigger(new TConnectionMsg.Ping(clientAddr, serverAddr, Transport.UDT,
                        UUID.nextUUID()),
                        client.getPositive(VodNetwork.class));
            }
        };
        public Handler<TConnectionMsg.Ping> handlePing = new Handler<TConnectionMsg.Ping>() {

            @Override
            public void handle(TConnectionMsg.Ping event) {
                logger.info("Received ping");
                trigger(new TConnectionMsg.Pong(serverAddr, clientAddr, Transport.UDT,
                        event.getTimeoutId()),
                        server.getPositive(VodNetwork.class));
            }
        };
        public Handler<TConnectionMsg.Pong> handlePong = new Handler<TConnectionMsg.Pong>() {
            @Override
            public void handle(TConnectionMsg.Pong event) {
                logger.info("Received pong");
                CloseConnectionRequest request = new CloseConnectionRequest(0, serverAddr.getPeerAddress(), Transport.UDT);
                request.setResponse(new CloseConnectionResponse(request));
                trigger(request, client.getPositive(NatNetworkControl.class));
            }
        };
        public Handler<CloseConnectionResponse> handleCloseConnectionResponse = new Handler<CloseConnectionResponse>() {
            @Override
            public void handle(CloseConnectionResponse event) {
                logger.info("Received CloseConnectionResponse");
                Set set = new HashSet<Integer>();
                set.add(serverAddr.getPort());
                PortDeleteRequest request = new PortDeleteRequest(0, set, Transport.UDT);
                request.setResponse(new PortDeleteResponse(request, 0) {
                });
                trigger(request, server.getPositive(NatNetworkControl.class));
            }
        };
        public Handler<PortDeleteResponse> handlePortDeletionResponse = new Handler<PortDeleteResponse>() {
            @Override
            public void handle(PortDeleteResponse event) {
                logger.info("Received PortDeleteResponse");
                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                testObj.pass();
            }
        };
        public Handler<MsgTimeout> handleMsgTimeout = new Handler<MsgTimeout>() {
            @Override
            public void handle(MsgTimeout event) {
                logger.info("Msg timeout");
                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                testObj.failAndRelease();
            }
        };
    }

    private static final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    private void allTests() {
        runInstance();
        assertTrue(testStatus);
    }

    private void runInstance() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        Kompics.createAndStart(TestStClientComponent.class, 1);

        try {
            UdtPingTest.semaphore.acquire(EVENT_COUNT);
            logger.info("Finished test.");
        } catch (InterruptedException e) {
            assert (false);
        } finally {
            Kompics.shutdown();
        }
    }

    @org.junit.Ignore
    public void testApp() {
        setTestObj(this);
        allTests();
    }

    public void pass() {
        UdtPingTest.semaphore.release();
    }

    public void failAndRelease() {
        testStatus = false;
        UdtPingTest.semaphore.release();
    }
}
