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
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Kompics;

/**
 * Unit test for simple App.
 */
public class RetryClientTimeoutTest
        extends TestCase
{

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public RetryClientTimeoutTest(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(RetryClientTimeoutTest.class);
    }

    public static void setTestObj(RetryClientTimeoutTest testObj)
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

            connect(testRetryClient.getNegative(VodNetwork.class), server.getPositive(VodNetwork.class));

            trigger(new RetryInit(), testRetryClient.getControl());
        }

    }

    public static class TestRetryClientComponent extends MsgRetryComponent
    {

//        Negative<CleanupPort> cleanup = negative(CleanupPort.class);
        private static RetryClientTimeoutTest testObj = null;
        private VodAddress src, dest;
        private int expectedResponses = 0;
        private long startTime = System.currentTimeMillis();

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
        
        
        public Handler<RetryInit> handleRetryInit = new Handler<RetryInit>()
        {

            @Override
            public void handle(RetryInit event)
            {

                // Try to send the message 3 times, and if timeout, then
                // do nothing - silent
                int retries = 3;
                expectedResponses = retries + 1;
                TMessage.RequestMsg req = new TMessage.RequestMsg(src, dest);
                ScheduleRetryTimeout st = new ScheduleRetryTimeout(1000, retries, 1d);
                TMessage.RequestRetryTimeout timeoutMsg =
                        new TMessage.RequestRetryTimeout(st, req);
                retry(timeoutMsg);
            }

        };

        private Handler<TMessage.ResponseMsg> handleTestResponseMessage
               = new Handler<TMessage.ResponseMsg>()
        {
            @Override
            public void handle(TMessage.ResponseMsg response)
            {
                System.out.println("Client: response recvd. not cancelling the timer. Time secs: "
                        +((double)(System.currentTimeMillis()-startTime))/1000d);
                expectedResponses--;
            }
        };
        private Handler<TMessage.RequestRetryTimeout> handleTMessageTimeout
                = new Handler<TMessage.RequestRetryTimeout>()
        {

            @Override
            public void handle(TMessage.RequestRetryTimeout event)
            {
                if (cancelRetry(event.getTimeoutId()) == true)
                {
                    System.out.println("Client Timeout msg recvd time secs: "
                            +((double)(System.currentTimeMillis()-startTime))/1000d);

                    if(expectedResponses == 0)
                    {

                        testObj.pass();
                    }
                    else
                    {
                        System.out.println("TEST FAILED expected response is "+expectedResponses+" it should be 0");
                        testObj.fail();
                    }
                }
                else
                {
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
    public void testApp()
    {
        setTestObj(this);
        Kompics.createAndStart(MainComponent.class, 1);
        try
        {
            RetryClientTimeoutTest.semaphore.acquire(EVENT_COUNT);
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
