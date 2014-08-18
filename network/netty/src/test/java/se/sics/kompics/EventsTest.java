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
import se.sics.gvod.common.Utility;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.filters.MsgDestFilterNodeId;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.timer.TimeoutId;

/**
 * Unit test for simple App.
 */
public class EventsTest
        extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(EventsTest.class);
    private boolean testStatus = true;
        public  static VodAddress p1Addr;
        public static VodAddress p2Addr;
        public static int count = 0;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public EventsTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(EventsTest.class);
    }

    public static void setTestObj(EventsTest testObj) {
        TestStClientComponent.testObj = testObj;
        PingPongComponent.testObj = testObj;
    }

    public static class PingPongComponent extends ComponentDefinition {

        Positive<VodNetwork> lower = positive(VodNetwork.class);
        Negative<VodNetwork> upper = negative(VodNetwork.class);
        Negative<Evt> evt = negative(Evt.class);
        private static EventsTest testObj = null;

        public PingPongComponent() {
//            subscribe(handlePing, evt);
            subscribe(handlePong, evt);
            subscribe(handleUpper, upper);
//            subscribe(handleMsg, evt);
        }
//        public Handler<Ping> handlePing = new Handler<Ping>() {
//
//            @Override
//            public void handle(Ping event) {
//                System.out.println("Ping");
//                isFinished();
//            }
//        };

        private void isFinished() {
            count++;
            if (count == 2) {
                testObj.pass();
            }
        }
        public Handler<Pong> handlePong = new Handler<Pong>() {

            @Override
            public void handle(Pong event) {
                System.out.println("Pong");
                System.out.println("Msg " + event.getDestination().getId());
                event.rewriteDestination(p1Addr.getPeerAddress());
                trigger(event, lower);
                Pong p = new Pong(event.getVodDestination(), p2Addr);
                trigger(p, lower);
            }
        };
        
        public Handler<RewriteableMsg> handleUpper = new Handler<RewriteableMsg>() {
            @Override
            public void handle(RewriteableMsg event) {
                System.out.println("Msg " + event.getDestination().getId());
                isFinished();
            }
        };
        
//        public Handler<RewriteableMsg> handleMsg = new Handler<RewriteableMsg>() {
//            @Override
//            public void handle(RewriteableMsg event) {
//                System.out.println("Msg " + event.getDestination().getId());
//                event.rewriteDestination(p1Addr.getPeerAddress());
//                trigger(event, lower);
//                event.rewriteDestination(p2Addr.getPeerAddress());
//                trigger(event, lower);
//            }
//        };
    }

    public static class TestStClientComponent extends ComponentDefinition {

        Positive<Evt> evt = positive(Evt.class);
        private Component pingPong;
        private Component pongPing1;
        private Component pongPing2;
        private static EventsTest testObj = null;
        private VodAddress clientAddr;
        private VodAddress serverAddr;
        private Utility utility = new UtilityVod(10, 200, 15);
        private TimeoutId timeoutId;

        public TestStClientComponent() {
            pingPong = create(PingPongComponent.class, Init.NONE);
            pongPing1 = create(PingPongComponent.class, Init.NONE);
            pongPing2 = create(PingPongComponent.class, Init.NONE);

            InetAddress ip = null;
            int clientPort = 59658;
            int serverPort = 23651;

            try {
                ip = InetAddress.getLocalHost();
            } catch (UnknownHostException ex) {
                logger.error("UnknownHostException");
                testObj.fail();
            }
            Address cAddr = new Address(ip, clientPort, 0);
            Address sAddr = new Address(ip, serverPort, 1);
            Address pAddr1 = new Address(ip, serverPort, 2);
            Address pAddr2 = new Address(ip, serverPort, 3);

            clientAddr = new VodAddress(cAddr, VodConfig.SYSTEM_OVERLAY_ID);
            serverAddr = new VodAddress(sAddr, VodConfig.SYSTEM_OVERLAY_ID);
            p1Addr = new VodAddress(pAddr1, VodConfig.SYSTEM_OVERLAY_ID);
            p2Addr = new VodAddress(pAddr2, VodConfig.SYSTEM_OVERLAY_ID);

            subscribe(handleStart, control);
            

            connect(pingPong.getNegative(VodNetwork.class), 
                    pongPing1.getPositive(VodNetwork.class)
                    ,new MsgDestFilterNodeId(pAddr1.getId())
                    );
            connect(pingPong.getNegative(VodNetwork.class), 
                    pongPing2.getPositive(VodNetwork.class)
                    ,new MsgDestFilterNodeId(pAddr2.getId())
                    );
            
        }
        public Handler<Start> handleStart = new Handler<Start>() {

            public void handle(Start event) {
                trigger(new Pong(clientAddr, serverAddr),
                        pingPong.getPositive(Evt.class));
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
            EventsTest.semaphore.acquire(EVENT_COUNT);
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
        EventsTest.semaphore.release();
    }

    public void fail(boolean release) {
        testStatus = false;
        EventsTest.semaphore.release();
    }
}
