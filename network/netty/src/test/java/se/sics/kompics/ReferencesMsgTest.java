package se.sics.kompics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.common.msgs.ReferencesMsg;
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
public class ReferencesMsgTest
        extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(ReferencesMsgTest.class);
    private boolean testStatus = true;


    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ReferencesMsgTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ReferencesMsgTest.class);
    }

    public static void setTestObj(ReferencesMsgTest testObj) {
        TestStClientComponent.testObj = testObj;
    }

    public static class TestStClientComponent extends ComponentDefinition {

        private Component client;
        private Component server;
        private Component timer;
        private static ReferencesMsgTest testObj = null;
        private VodAddress clientAddr;
        private VodAddress serverAddr;
        private UtilityVod utility = new UtilityVod(10,200,15);

        public TestStClientComponent() {
            timer = create(JavaTimer.class);
            client = create(NettyNetwork.class);
            server = create(NettyNetwork.class);

            InetAddress ip = null;
            int clientPort = 57412;
            int serverPort = 3698;


            try {
                ip = InetAddress.getLocalHost();

            } catch (UnknownHostException ex) {
                logger.error("UnknownHostException");
                testObj.fail();
            }
            Address cAddr = new Address(ip, clientPort, 0);
            Address sAddr = new Address(ip, serverPort, 1);

            clientAddr = new VodAddress(cAddr, VodConfig.SYSTEM_OVERLAY_ID);
            serverAddr = new VodAddress(sAddr, VodConfig.SYSTEM_OVERLAY_ID);

            subscribe(handleStart, control);
            subscribe(handleMsgTimeout, timer.getPositive(Timer.class));
            subscribe(handleConnectRequest, server.getPositive(VodNetwork.class));
            subscribe(handleConnectResponse, client.getPositive(VodNetwork.class));

            trigger(new NettyInit(clientAddr.getPeerAddress(), false, (int) 132, BaseMsgFrameDecoder.class),
                    client.getControl());
            trigger(new NettyInit(serverAddr.getPeerAddress(), false, (int) 136, BaseMsgFrameDecoder.class),
                    server.getControl());

        }
        public Handler<Start> handleStart = new Handler<Start>() {

            public void handle(Start event) {
                VodDescriptor nodeDesc = new VodDescriptor(clientAddr, 
                        utility, 0, BaseCommandLineConfig.DEFAULT_MTU);
                System.out.println("Starting");
                ScheduleTimeout st = new ScheduleTimeout(2000);
                ReferencesMsg.RequestTimeout mt = new ReferencesMsg.RequestTimeout(st, serverAddr);
                st.setTimeoutEvent(mt);
                trigger(new ReferencesMsg.Request(clientAddr, serverAddr, mt.getTimeoutId(), 
                        0, utility, null),
                        client.getPositive(VodNetwork.class));
                trigger(st, timer.getPositive(Timer.class));
            }
        };
        public Handler<ReferencesMsg.Request> handleConnectRequest = new Handler<ReferencesMsg.Request>() {

            @Override
            public void handle(ReferencesMsg.Request event) {
                System.out.println("RefRequest");
                trigger(new ReferencesMsg.Response(event.getVodDestination(),
                        event.getVodSource(), event.getTimeoutId(), 0,
                        utility, null),
                        server.getPositive(VodNetwork.class));
            }
        };
        public Handler<ReferencesMsg.Response> handleConnectResponse = new Handler<ReferencesMsg.Response>() {

            @Override
            public void handle(ReferencesMsg.Response event) {

                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                System.out.println("RefResponse");
                testObj.pass();
            }
        };
        public Handler<ReferencesMsg.RequestTimeout> handleMsgTimeout = new Handler<ReferencesMsg.RequestTimeout>() {

            public void handle(ReferencesMsg.RequestTimeout event) {
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
        Kompics.createAndStart(TestStClientComponent.class, 1);
        try {
            ReferencesMsgTest.semaphore.acquire(EVENT_COUNT);
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
        ReferencesMsgTest.semaphore.release();
    }

    public void fail(boolean release) {
        testStatus = false;
        ReferencesMsgTest.semaphore.release();
    }
}
