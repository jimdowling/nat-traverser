package se.sics.gvod.nat.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import se.sics.kompics.Stop;
import se.sics.gvod.address.Address;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;

/**
 * Unit test for simple App.
 */
public class RetryClientServerFailureTest
        extends TestCase {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public RetryClientServerFailureTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(RetryClientServerFailureTest.class);
    }

    public static void setTestObj(RetryClientServerFailureTest testObj) {
        TestRetryClientComponent.testObj = testObj;
    }

    public static class RetryInit extends Init<TestRetryClientComponent> {

        public RetryInit() {
        }

    }

    public static class ClassicTimeout extends Timeout {

        public ClassicTimeout(ScheduleTimeout request) {
            super(request);
        }

    }

    public static class MainComponent extends ComponentDefinition {

        private Component javaTimer;
        private Component testRetryClient;
        private Component server;

        public MainComponent() {
            javaTimer = create(JavaTimer.class, Init.NONE);

            server = create(TimeoutTestServerFailure.class, Init.NONE);

            testRetryClient = create(TestRetryClientComponent.class, new RetryInit());
            connect(testRetryClient.getNegative(Timer.class),
                    javaTimer.getPositive(Timer.class));

            connect(testRetryClient.getNegative(VodNetwork.class), server.getPositive(VodNetwork.class));
        }

    }

    public static class TestRetryClientComponent extends MsgRetryComponent {

//        Negative<CleanupPort> cleanup = negative(CleanupPort.class);
        private static RetryClientServerFailureTest testObj = null;
        private VodAddress src, dest;
        private int expectedResponses = 0;
        private long startTime = System.currentTimeMillis();

        public TestRetryClientComponent(RetryInit init) {
            this(null, init);
        }

        public TestRetryClientComponent(RetryComponentDelegator delegator, RetryInit init) {
            super(delegator);
            try {
                Address s = new Address(InetAddress.getLocalHost(), 2222, 0);
                Address d = new Address(InetAddress.getLocalHost(), 2223, 1);
                src = ToVodAddr.systemAddr(s);
                dest = ToVodAddr.systemAddr(d);
            } catch (UnknownHostException ex) {
//                Logger.getLogger(RetryClientTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.delegator.doAutoSubscribe();
            doInit(init);
        }

        private void doInit(RetryInit event) {
        }

        public Handler<Start> handleStart = new Handler<Start>() {

            @Override
            public void handle(Start event) {

                // Try to send the message 3 times, and if timeout, then
                // do nothing - silent
                int retries = 0;
                TMessage.RequestMsg req = new TMessage.RequestMsg(src, dest);
                ScheduleRetryTimeout st = new ScheduleRetryTimeout(1000, retries, 1d);
                TMessage.RequestRetryTimeout timeoutMsg
                        = new TMessage.RequestRetryTimeout(st, req);
                retry(timeoutMsg);
            }
        };

        private Handler<TMessage.ResponseMsg> handleTestResponseMessage
                = new Handler<TMessage.ResponseMsg>() {
                    @Override
                    public void handle(TMessage.ResponseMsg response) {
//                System.out.println("Client: response recvd. not cancelling the timer. Time secs: "
//                        +((double)(System.currentTimeMillis()-startTime))/1000d);
                        testObj.fail();
                    }
                };
        private Handler<TMessage.RequestRetryTimeout> handleTMessageTimeout
                = new Handler<TMessage.RequestRetryTimeout>() {

                    @Override
                    public void handle(TMessage.RequestRetryTimeout event) {
                        if (cancelRetry(event.getTimeoutId()) == true) {
                            System.out.println("Client Timeout msg recvd time secs: "
                                    + ((double) (System.currentTimeMillis() - startTime)) / 1000d);

                            testObj.pass();
                        } else {
                            testObj.fail();
                        }
                    }

                };

        @Override
        public void stop(Stop event) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    @org.junit.Test
    public void testApp() {
        setTestObj(this);
        Kompics.createAndStart(MainComponent.class, 1);
        try {
            RetryClientServerFailureTest.semaphore.acquire(EVENT_COUNT);
            System.out.println("Exiting unit test....");
        } catch (InterruptedException e) {
            assert (false);
        } finally {
            Kompics.shutdown();
        }
    }

    public void pass() {
        assertTrue(true);
        semaphore.release();
    }

    public void fail(boolean release) {
        assertTrue(false);
        if (release == true) {
            semaphore.release();
        }
    }

}
