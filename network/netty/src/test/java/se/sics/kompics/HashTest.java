package se.sics.kompics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.msgs.DataMsg;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.Utility;
import se.sics.gvod.common.msgs.DisconnectMsg;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;

/**
 * Unit test for simple App.
 */
public class HashTest
        extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(HashTest.class);
    private boolean testStatus = true;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public HashTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(HashTest.class);
    }

    public static void setTestObj(HashTest testObj) {
        TestStClientComponent.testObj = testObj;
    }

    public static class TestStClientComponent extends ComponentDefinition {

        private Component client;
        private Component server;
        private Component timer;
        private static HashTest testObj = null;
        private VodAddress clientAddr;
        private VodAddress serverAddr;
        private Utility utility = new UtilityVod(10,200,15);

        public TestStClientComponent() {
            timer = create(JavaTimer.class);
            client = create(NettyNetwork.class);
            server = create(NettyNetwork.class);

            InetAddress ip = null;
            int clientPort = 56987;
            int serverPort = 9874;


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
            subscribe(handleHashRequest, server.getPositive(VodNetwork.class));
            subscribe(handleDataResponse, client.getPositive(VodNetwork.class));

            trigger(new NettyInit(clientAddr.getPeerAddress(), false, (int) 132),
                    client.getControl());
            trigger(new NettyInit(serverAddr.getPeerAddress(), false, (int) 134),
                    server.getControl());

        }
        public Handler<Start> handleStart = new Handler<Start>() {

            public void handle(Start event) {
                System.out.println("Starting");
                ScheduleTimeout st = new ScheduleTimeout(2000);
                DataMsg.HashTimeout mt = new DataMsg.HashTimeout(st, 10, serverAddr, 0);
                st.setTimeoutEvent(mt);
                trigger(new DataMsg.HashRequest(clientAddr, serverAddr,
                        mt.getTimeoutId(), 100),
                        client.getPositive(VodNetwork.class));
                trigger(st, timer.getPositive(Timer.class));
            }
        };
        public Handler<DataMsg.HashRequest> handleHashRequest = new Handler<DataMsg.HashRequest>() {

            @Override
            public void handle(DataMsg.HashRequest event) {
                System.out.println("Data Request");
                trigger(new DataMsg.HashResponse(event.getVodDestination(),
                        event.getVodSource(), event.getTimeoutId(), 100, new byte[1400], 0,
                        1),
                        server.getPositive(VodNetwork.class));
            }
        };
        public Handler<DataMsg.HashResponse> handleDataResponse = new Handler<DataMsg.HashResponse>() {

            @Override
            public void handle(DataMsg.HashResponse event) {


                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                System.out.println("Data Response");
                testObj.pass();
            }
        };
        public Handler<DisconnectMsg.RequestTimeout> handleMsgTimeout = new Handler<DisconnectMsg.RequestTimeout>() {

            public void handle(DisconnectMsg.RequestTimeout event) {
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
            HashTest.semaphore.acquire(EVENT_COUNT);
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
        HashTest.semaphore.release();
    }

    public void fail(boolean release) {
        testStatus = false;
        HashTest.semaphore.release();
    }
}
