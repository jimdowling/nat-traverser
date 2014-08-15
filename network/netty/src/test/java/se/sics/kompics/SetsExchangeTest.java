package se.sics.kompics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.Utility;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.gradient.msgs.SetsExchangeMsg;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.events.*;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;

/**
 * Unit test for simple App.
 */
public class SetsExchangeTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(SetsExchangeTest.class);
    private boolean testStatus = true;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SetsExchangeTest(String testName) {
        super(testName);
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(SetsExchangeTest.class);
    }

    public static void setTestObj(SetsExchangeTest testObj) {
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
        private static SetsExchangeTest testObj = null;
        private VodAddress clientAddr;
        private VodAddress serverAddr;
        private Utility utility = new UtilityVod(10, 200, 15);
        private VodDescriptor nodeDesc;
        private List<VodDescriptor> nodes;

        private class ClientPortDeleteResponse extends PortDeleteResponse {
            public ClientPortDeleteResponse(PortDeleteRequest request, Object key) {
                super(request, key);
            }
        }

        private class ServerPortDeleteResponse extends PortDeleteResponse {
            public ServerPortDeleteResponse(PortDeleteRequest request, Object key) {
                super(request, key);
            }
        }

        public TestStClientComponent() {
            timer = create(JavaTimer.class, Init.NONE);
            client = create(NettyNetwork.class, 
                    new NettyInit(132, true, BaseMsgFrameDecoder.class));
            server = create(NettyNetwork.class,
                    new NettyInit(132, true, BaseMsgFrameDecoder.class));

            InetAddress ip = null;
            int clientPort = 54644;
            int serverPort = 54645;

            try {
                ip = InetAddress.getByName("127.0.0.1");

            } catch (UnknownHostException ex) {
                logger.error("UnknownHostException");
                testObj.fail(true);
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
            subscribe(handleSetsExchangeRequest, server.getPositive(VodNetwork.class));
            subscribe(handleSetsExchangeResponse, client.getPositive(VodNetwork.class));
            subscribe(handleSetsExchangeResponse, server.getPositive(VodNetwork.class));
            subscribe(handleTestPortBindResponse, client.getPositive(NatNetworkControl.class));
            subscribe(handleTestPortBindResponse, server.getPositive(NatNetworkControl.class));
            subscribe(handleClientPortDeletionResponse, client.getPositive(NatNetworkControl.class));
            subscribe(handleServerPortDeletionResponse, server.getPositive(NatNetworkControl.class));
        }
        public Handler<Start> handleStart = new Handler<Start>() {
            public void handle(Start event) {
                System.out.println("Starting");
                PortBindRequest pb1 = new PortBindRequest(clientAddr.getPeerAddress(),
                        Transport.UDP);
                PortBindResponse pbr1 = new TestPortBindResponse(pb1);
                trigger(pb1, client.getPositive(NatNetworkControl.class));

                PortBindRequest pb2 = new PortBindRequest(serverAddr.getPeerAddress(), Transport.UDP);
                PortBindResponse pbr2 = new TestPortBindResponse(pb2);
                trigger(pb2, server.getPositive(NatNetworkControl.class));
            }
        };
        public Handler<SetsExchangeMsg.Request> handleSetsExchangeRequest = new Handler<SetsExchangeMsg.Request>() {
            @Override
            public void handle(SetsExchangeMsg.Request event) {
                logger.info("Received SetExchangeRequest");
                trigger(new SetsExchangeMsg.Response(serverAddr.getNodeAddress(), event, nodes, nodes),
                        server.getPositive(VodNetwork.class));
            }
        };
        
        public Handler<TestPortBindResponse> handleTestPortBindResponse 
                = new Handler<TestPortBindResponse>() {
            @Override
            public void handle(TestPortBindResponse event) {
                logger.info("Received PortBindResponse");
                if (event.getStatus() != TestPortBindResponse.Status.SUCCESS) {
                    testObj.fail(true);
                } else {
                    ScheduleTimeout st = new ScheduleTimeout(10 * 1000);
                    SetsExchangeMsg.RequestTimeout mt = new SetsExchangeMsg.RequestTimeout(st,
                            serverAddr);
                    st.setTimeoutEvent(mt);
                    trigger(new SetsExchangeMsg.Request(clientAddr, serverAddr, clientAddr.getId(),
                            serverAddr.getId(), mt.getTimeoutId()),
                            client.getPositive(VodNetwork.class));
                    trigger(st, timer.getPositive(Timer.class));
                }
            }
        };
        public Handler<SetsExchangeMsg.Response> handleSetsExchangeResponse = new Handler<SetsExchangeMsg.Response>() {
            @Override
            public void handle(SetsExchangeMsg.Response event) {
                logger.info("Received SetExchangeResponse");
                Set set = new HashSet<Integer>();
                set.add(clientAddr.getPort());
                PortDeleteRequest request = new PortDeleteRequest(0, set, Transport.UDP);
                request.setResponse(new ClientPortDeleteResponse(request, 0) {
                });
                trigger(request, client.getPositive(NatNetworkControl.class));
            }
        };
        public Handler<ClientPortDeleteResponse> handleClientPortDeletionResponse = new Handler<ClientPortDeleteResponse>() {
            @Override
            public void handle(ClientPortDeleteResponse event) {
                logger.info("Received PortDeleteResponse for client");
                Set set = new HashSet<Integer>();
                set.add(clientAddr.getPort());
                PortDeleteRequest request = new PortDeleteRequest(0, set, Transport.UDP);
                request.setResponse(new ServerPortDeleteResponse(request, 0) {
                });
                trigger(request, server.getPositive(NatNetworkControl.class));
            }
        };
        public Handler<ServerPortDeleteResponse> handleServerPortDeletionResponse = new Handler<ServerPortDeleteResponse>() {
            @Override
            public void handle(ServerPortDeleteResponse event) {
                logger.info("Received PortDeleteResponse for server");
                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                testObj.pass();
            }
        };
        public Handler<SetsExchangeMsg.RequestTimeout> handleMsgTimeout = new Handler<SetsExchangeMsg.RequestTimeout>() {
            public void handle(SetsExchangeMsg.RequestTimeout event) {
                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                System.out.println("Msg timeout");
                testObj.testStatus = false;
                testObj.fail(true);
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
            SetsExchangeTest.semaphore.acquire(EVENT_COUNT);
            System.out.println("Finished test.");
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
        SetsExchangeTest.semaphore.release();
    }

    public void fail(boolean release) {
        testStatus = false;
        SetsExchangeTest.semaphore.release();
    }
}
