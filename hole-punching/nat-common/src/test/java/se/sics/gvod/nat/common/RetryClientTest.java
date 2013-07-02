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
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Kompics;
/**
 * Unit test for simple App.
 */
public class RetryClientTest
        extends TestCase {
        private static Logger logger = LoggerFactory.getLogger(RetryClientTest.class.toString());
        
        public static int TIMEOUT=100;
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public RetryClientTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(RetryClientTest.class);
    }

    public static void setTestObj(RetryClientTest testObj) {
        TestRetryClientComponent.testObj = testObj;
    }

    public static class RetryInit extends Init {

        public RetryInit() {
        }

    }


    public static class ClassicTimeout extends Timeout {

        public ClassicTimeout(ScheduleTimeout request) {
            super(request);
        }
    }

    public static class SimpleTimeout extends RewriteableRetryTimeout {

        public SimpleTimeout(ScheduleRetryTimeout st, DirectMsg retryMsg) {
            super(st,retryMsg);
        }
    }


    public static class MainComponent extends ComponentDefinition {

        private Component javaTimer;
        private Component testRetryClient;
        private Component server;

        public MainComponent() {
            javaTimer = create(JavaTimer.class);

            server = create(Server.class);

            testRetryClient = create(TestRetryClientComponent.class);
            connect(testRetryClient.getNegative(Timer.class),
                    javaTimer.getPositive(Timer.class));

            connect(testRetryClient.getNegative(VodNetwork.class), server.getPositive(VodNetwork.class));

            trigger(new RetryInit(), testRetryClient.getControl());
        }
    }



    public static class TestRetryClientComponent extends MsgRetryComponent {

        private static RetryClientTest testObj = null;
        private VodAddress src, dest;

        public TestRetryClientComponent()
        {
            this(null);
        }
        public TestRetryClientComponent(RetryComponentDelegator delegator) {
            super(delegator);
            try
            {
                Address s = new Address(InetAddress.getLocalHost(), 2222, 0);
                Address d = new Address(InetAddress.getLocalHost(), 2223, 1);
                src = ToVodAddr.systemAddr(s);
                dest = ToVodAddr.systemAddr(d);
            }
            catch (UnknownHostException ex)
            {
//                Logger.getLogger(RetryClientTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.delegator.doAutoSubscribe();
        }
        public Handler<RetryInit> handleRetryInit = new Handler<RetryInit>() {

            @Override
            public void handle(RetryInit event) {

                // Try to send the message 3 times, and if timeout, then
                // do nothing - silent
                TestMsg req = new TestMsg(src, dest);
                retry(req, TIMEOUT, 3, 1.2);
            }
        };
        private Handler<TestMsg> handleTestMessage = new Handler<TestMsg>() {

            @Override
            public void handle(TestMsg event) {
               if  (cancelRetry(event.getTimeoutId()) == true)
               {
                TestMsgId req = new TestMsgId(src, dest);
                retry(req, TIMEOUT, 4, 1.5);
               }
               else {
                   testObj.fail();
               }
            }
        };
        private Handler<TestMsgId> handleTestIdMessage = new Handler<TestMsgId>() {

            @Override
            public void handle(TestMsgId event) {
                cancelRetry(event.getTimeoutId());

                // Retry TestMsgId twice, and if timeout, then SimpleTimeout handler is called
                TestMsgId req = new TestMsgId(src, dest);
                ScheduleRetryTimeout st =
                        new ScheduleRetryTimeout(TIMEOUT, 3, 1.5d);
                retry(new SimpleTimeout(st, req));              
            }
        };
        public Handler<SimpleTimeout> handleSimpleTimeout = new Handler<SimpleTimeout>() {

            @Override
            public void handle(SimpleTimeout event) {
                System.out.println("Simple timeout handler executed");
                RewriteableMsg r = event.getMsg();
                ScheduleTimeout st = new ScheduleTimeout(TIMEOUT);
                ClassicTimeout msgTimeout = new ClassicTimeout(st);
                st.setTimeoutEvent(msgTimeout);
                trigger(r, network);
                trigger(st, timer);
            }
        };
        public Handler<ClassicTimeout> handleClassicTimeout = new Handler<ClassicTimeout>() {

            @Override
            public void handle(ClassicTimeout event) {
                System.out.println("MsgTimeout received.");
                testObj.pass();
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
            RetryClientTest.semaphore.acquire(EVENT_COUNT);
            System.out.println("Exiting unit test....");
        } catch (InterruptedException e) {
            assert (false);
        }
        finally {
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
