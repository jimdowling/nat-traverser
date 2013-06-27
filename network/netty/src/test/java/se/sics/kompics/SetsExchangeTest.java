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
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;

/**
 * Unit test for simple App.
 */
public class SetsExchangeTest
        extends TestCase {

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
                testObj.fail();
            }
            Address cAddr = new Address(ip, clientPort, 0);
            Address sAddr = new Address(ip, serverPort, 1);

            clientAddr = new VodAddress(cAddr, VodConfig.SYSTEM_OVERLAY_ID);
            serverAddr = new VodAddress(sAddr, VodConfig.SYSTEM_OVERLAY_ID);

            nodeDesc = new VodDescriptor(clientAddr, utility, 0,
                    BaseCommandLineConfig.DEFAULT_MTU);
            nodes = new ArrayList<VodDescriptor>();
            nodes.add(nodeDesc);

            subscribe(handleStart, control);
            subscribe(handleMsgTimeout, timer.getPositive(Timer.class));
            subscribe(handleSetsExchangeRequest, server.getPositive(VodNetwork.class));
            subscribe(handleSetsExchangeResponse, client.getPositive(VodNetwork.class));
            subscribe(handleSetsExchangeResponse, server.getPositive(VodNetwork.class));

            trigger(new NettyInit(clientAddr.getPeerAddress(), true, (int) 132, BaseMsgFrameDecoder.class),
                    client.getControl());
            trigger(new NettyInit(serverAddr.getPeerAddress(), true, (int) 132, BaseMsgFrameDecoder.class),
                    server.getControl());

        }
        public Handler<Start> handleStart = new Handler<Start>() {

            public void handle(Start event) {
                System.out.println("Starting");
                ScheduleTimeout st = new ScheduleTimeout(10 * 1000);
                SetsExchangeMsg.RequestTimeout mt =
                        new SetsExchangeMsg.RequestTimeout(st, serverAddr);
                st.setTimeoutEvent(mt);
                trigger(new SetsExchangeMsg.Request(clientAddr, serverAddr,
                        clientAddr.getId(), serverAddr.getId(),
                        mt.getTimeoutId()), client.getPositive(VodNetwork.class));
                trigger(st, timer.getPositive(Timer.class));
            }
        };
        public Handler<SetsExchangeMsg.Request> handleSetsExchangeRequest = new Handler<SetsExchangeMsg.Request>() {

            @Override
            public void handle(SetsExchangeMsg.Request event) {
                System.out.println("Data Request");
                trigger(new SetsExchangeMsg.Response(event.getVodDestination(),
                        event, nodes, nodes),
                        server.getPositive(VodNetwork.class));
            }
        };
        public Handler<SetsExchangeMsg.Response> handleSetsExchangeResponse = new Handler<SetsExchangeMsg.Response>() {

            @Override
            public void handle(SetsExchangeMsg.Response event) {


                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                System.out.println("Data Response");
                testObj.pass();
            }
        };
        public Handler<SetsExchangeMsg.RequestTimeout> handleMsgTimeout =
                new Handler<SetsExchangeMsg.RequestTimeout>() {

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
        int i = 0;

        runInstance();
        if (testStatus == true) {
            assertTrue(true);
        }
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
        SetsExchangeTest.semaphore.release();
    }

    public void fail(boolean release) {
        testStatus = false;
        SetsExchangeTest.semaphore.release();
    }
}
