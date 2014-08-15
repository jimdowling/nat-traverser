package se.sics.kompics.nat.utils.getip;

import java.util.EnumSet;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import java.util.concurrent.Semaphore;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest.NetworkInterfacesMask;

/**
 * Unit test for simple App.
 */
public class GetIpTest
        extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GetIpTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(GetIpTest.class);
    }

    public static void setTestObj(GetIpTest testObj) {
        TestResolveIpComponent.testObj = testObj;
    }
    private boolean status = true;

    public static class TestResolveIpComponent extends ComponentDefinition {

        private Component resolveIp;
        private Component timer;
        private static GetIpTest testObj = null;
        private int requestedPort = 4356;
        private int count = 0;

        public TestResolveIpComponent() {
            resolveIp = create(ResolveIp.class, Init.NONE);
            timer = create(JavaTimer.class, Init.NONE);

            connect(resolveIp.getNegative(Timer.class), timer.getPositive(Timer.class));

            subscribe(handleStart, control);
            subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));

        }
        public Handler<Start> handleStart = new Handler<Start>() {

            public void handle(Start event) {

                trigger(new GetIpRequest(true,
                        EnumSet.of(NetworkInterfacesMask.IGNORE_LOCAL_ADDRESSES,
                        NetworkInterfacesMask.IGNORE_PRIVATE)),
                        resolveIp.getPositive(ResolveIpPort.class));

            }
        };
        public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {

            public void handle(GetIpResponse event) {

                count++;
                System.out.println("bound ip: " + event.getBoundIp());

                for (IpAddrStatus addr : event.getAddrs()) 
                {
                    if (addr.getNetworkInterface() != null) {
                        System.out.println("received network interface: " + addr.getNetworkInterface().getDisplayName());
                    }
                    System.out.println("received ip: " + addr.getAddr());
                    System.out.println("mtu: " + addr.getMtu());
                    System.out.println("up?  " + addr.isUp());
                    System.out.println("network prefix length: " + addr.getNetworkPrefixLength());
                }

                if (count == 2) {
                    testObj.pass();
                } else {
                    trigger(new GetIpRequest(true,
                            EnumSet.of(NetworkInterfacesMask.NO_MASK)),
                            resolveIp.getPositive(ResolveIpPort.class));
                }
            }
        };
    }
    private static final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    public void testApp() {

        setTestObj(this);

        Kompics.createAndStart(TestResolveIpComponent.class, 1);
        long startTime = System.currentTimeMillis();
        try {
            GetIpTest.semaphore.acquire(EVENT_COUNT);
        } catch (InterruptedException e) {
            assert (false);
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println("Time taken: " + (endTime - startTime) + "ms");
            System.out.println("Exiting unit test....");
            Kompics.shutdown();
        }

        if (status == false) {
            assertTrue(false);
        } else {
            assertTrue(true);
        }
    }

    public void pass() {
        GetIpTest.semaphore.release();
    }

    public void fail(boolean release) {
        GetIpTest.semaphore.release();
        status = false;
    }
}
