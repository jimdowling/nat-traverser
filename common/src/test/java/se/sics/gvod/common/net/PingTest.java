package se.sics.gvod.common.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.gvod.address.Address;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.kompics.Init;

/**
 * Unit test for simple App.
 */
public class PingTest
        extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(PingTest.class);
    private boolean testStatus = true;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PingTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(PingTest.class);
    }

    public static void setTestObj(PingTest testObj) {
        TestStClientComponent.testObj = testObj;
    }

    public static class TestStClientComponent extends ComponentDefinition {

        private Component ping;
        private Component timer;
        private static PingTest testObj = null;
        private InetAddress ip;

        public TestStClientComponent() {
            timer = create(JavaTimer.class, Init.NONE);
            ping = create(Ping.class, Init.NONE);

            ip = null;


            try {
                ip = InetAddress.getByName("193.10.67.135");

            } catch (UnknownHostException ex) {
                logger.error("UnknownHostException");
                testObj.fail();
            }

            connect(ping.getNegative(Timer.class), timer.getPositive(Timer.class));

            subscribe(handleStart, control);
            subscribe(handleFault, ping.getControl());
            subscribe(handlePingResponse, ping.getPositive(PingPort.class));

        }
        public Handler<Start> handleStart = new Handler<Start>() {

            public void handle(Start event) {
                System.out.println("Sending ping to " + ip.getHostAddress());

                trigger(new PingRequest(new Address(ip, 6666, 1), 5*1000),
                        ping.getPositive(PingPort.class));
            }
        };
        public Handler<PingResponse> handlePingResponse = new Handler<PingResponse>() {

            @Override
            public void handle(PingResponse event) {
                System.out.println("Ping result " + event.getAddr().getIp().getHostAddress() + " (" + event.isAlive()
                        + ") Time taken: " + event.getTimeTaken());
                testObj.pass();
            }
        };
        public Handler<Fault> handleFault = new Handler<Fault>() {

            @Override
            public void handle(Fault event) {
                testObj.fail();
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
        Kompics.createAndStart(TestStClientComponent.class, 4);
        try {
            PingTest.semaphore.acquire(EVENT_COUNT);
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
        PingTest.semaphore.release();
    }

    public void fail(boolean release) {
        testStatus = false;
        PingTest.semaphore.release();
    }
}
