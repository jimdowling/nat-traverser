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
import se.sics.gvod.timer.java.JavaTimer;

/**
 * Unit test for simple App.
 */
public class MsgRetryTimeoutTest extends TestCase
{

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MsgRetryTimeoutTest(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(MsgRetryTimeoutTest.class);
    }

    public static void setTestObj(MsgRetryTimeoutTest testObj)
    {
        TestRetryClientComponent.testObj = testObj;
    }

    public static class RetryInit extends Init
    {
        public RetryInit()
        {
        }

    }


    public static class ClassicTimeout extends Timeout
    {

        public ClassicTimeout(ScheduleTimeout request)
        {
            super(request);
        }

    }

   

    public static class MainComponent extends ComponentDefinition
    {

        private Component javaTimer;
        private Component testRetryClient;
        private Component server;

        public MainComponent()
        {
            javaTimer = create(JavaTimer.class);

            server = create(TimeoutTestServer.class);

            testRetryClient = create(TestRetryClientComponent.class);
            connect(testRetryClient.getNegative(Timer.class),
                    javaTimer.getPositive(Timer.class));

            connect(testRetryClient.getNegative(VodNetwork.class), 
                    server.getPositive(VodNetwork.class));

            trigger(new RetryInit(), testRetryClient.getControl());
            trigger(new RetryInit(), server.getControl());
        }

    }

    public static class TestRetryClientComponent extends MsgRetryComponent
    {

        private static MsgRetryTimeoutTest testObj = null;
        private VodAddress src, dest;
        private long startTime = System.currentTimeMillis();
        private boolean isFirstResponse = true;

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
                testObj.fail();
            }
            this.delegator.doAutoSubscribe();
        }

        public Handler<RetryInit> handleInit = new Handler<RetryInit>()
        {

            @Override
            public void handle(RetryInit event)
            {

                // send a message with retry count set to 1
                // on the first reply from the server dont cancel the timer
                // on the second response cancel the timer
                // the client should not recv the timeout message

                int retries = 1;
                TMessage.RequestMsg req = new TMessage.RequestMsg(src, dest);
                ScheduleRetryTimeout st = new ScheduleRetryTimeout(1*500, retries, 1.5d);
                TMessage.RequestRetryTimeout timeoutMsg =
                        new TMessage.RequestRetryTimeout(st, req);
                retry(timeoutMsg);

                ScheduleTimeout st1 = new ScheduleTimeout(3*1000);
                ClassicTimeout ct = new ClassicTimeout(st1);
                st1.setTimeoutEvent(ct);
                trigger(st1, timer);
            }

        };

        private Handler<TMessage.ResponseMsg> handleTestResponseMessage
               = new Handler<TMessage.ResponseMsg>()
        {
            @Override
            public void handle(TMessage.ResponseMsg response)
            {
                if(isFirstResponse)
                {
                    System.out.println("Client: response recvd. not cancelling the timer. Time secs: "
                        +((double)(System.currentTimeMillis()-startTime))/1000d);
                    isFirstResponse=false;
                }
                else
                {
                    System.out.println("Client: response recvd. cancelling the timer. Time secs: "
                        +((double)(System.currentTimeMillis()-startTime))/1000d);
                    cancelRetry(response.getTimeoutId());
                }
            }
        };
        private Handler<TMessage.RequestRetryTimeout> handleTMessageTimeout
                = new Handler<TMessage.RequestRetryTimeout>()
        {
            @Override
            public void handle(TMessage.RequestRetryTimeout event)
            {
                testObj.fail();
            }

        };


        private Handler<ClassicTimeout> handleClassicTimeout
                = new Handler<ClassicTimeout>()
        {
            @Override
            public void handle(ClassicTimeout event)
            {
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
    public void testApp()
    {
        setTestObj(this);
        Kompics.createAndStart(MainComponent.class, 1);
        try
        {
            MsgRetryTimeoutTest.semaphore.acquire(EVENT_COUNT);
            System.out.println("Exiting unit test....");
        }
        catch (InterruptedException e)
        {
            assert (false);
        }
        finally {
            Kompics.shutdown();
        }
    }

    public void pass()
    {
        assertTrue(true);
        semaphore.release();
    }

    public void fail(boolean release)
    {
        assertTrue(false);
        if (release == true)
        {
            semaphore.release();
        }
    }

}
