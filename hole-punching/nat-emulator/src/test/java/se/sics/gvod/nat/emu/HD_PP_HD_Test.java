package se.sics.gvod.nat.emu;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import se.sics.gvod.address.Address;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.nat.common.TestMsgId;
import se.sics.gvod.nat.emu.events.DistributedNatGatewayEmulatorInit;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodNetwork;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
/**
 * Unit test for simple App.
 */
public class HD_PP_HD_Test
        extends TestCase {

    static InetAddress src1;
    static InetAddress src2;
    static InetAddress dest1;
    static InetAddress dest2;
    static InetAddress dest3;
    static int src1Port = 1111;
    static int src2Port = 1111;
    static int dest1Port = 1111;
    static int dest2Port = 1111;
    static int dest3Port = 1111;
    static int src1Id = 1;
    static int src2Id = 2;
    static int dest1Id = 6;
    static int dest2Id = 7;
    static int dest3Id = 8;
    static InetAddress natIP;
    static int natId = 9;
    static int incomingMsgs = 0;
    static boolean testStatus = true; // pass
    private static final Logger logger =  LoggerFactory.getLogger(HD_PP_HD_Test.class);
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public HD_PP_HD_Test(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(HD_PP_HD_Test.class);
    }

    public static void setTestObj(HD_PP_HD_Test testObj) {
        TestEmulatorComponent.testObj = testObj;

    }

    public static class MsgTimeout extends Timeout {

        public MsgTimeout(ScheduleTimeout request) {
            super(request);
        }
    }

    public static class TestEmulatorComponent extends ComponentDefinition {

        private Component emulator;
        private Component timer;
        private static HD_PP_HD_Test testObj = null;

        public TestEmulatorComponent() {
            emulator = create(DistributedNatGatewayEmulator.class);
            timer = create(JavaTimer.class);

            subscribe(handleStart, control);
            subscribe(handleLowerMessage, emulator.getNegative(VodNetwork.class));
            subscribe(handleUpperMessage, emulator.getPositive(VodNetwork.class));
            subscribe(handleMsgTimeout, timer.getPositive(Timer.class));

            trigger(new DistributedNatGatewayEmulatorInit(50000, 
                    Nat.MappingPolicy.HOST_DEPENDENT,
                    Nat.AllocationPolicy.PORT_PRESERVATION,
                    Nat.AlternativePortAllocationPolicy.PORT_CONTIGUITY, /* alternaive policy if there is clash using PP */
                    Nat.FilteringPolicy.HOST_DEPENDENT,
                    Nat.Type.NAT, /* types are NAT OPEN and UPNP */
                    1000 /* rule cleanup timer */,
                    testObj.natIP /* ip of NAT Gateway*/,
                    65533 /* max port */,
                    false /* clashing overrides */,
                    30000 /* rule life time */,
                    55 /*rand port seed*/,
                    false /* Upnp not enabled */), emulator.getControl());

            connect(emulator.getNegative(Timer.class), timer.getPositive(Timer.class));
        }
        public Handler<Start> handleStart = new Handler<Start>() {

            public void handle(Start event) {
                ScheduleTimeout st1 = new ScheduleTimeout(1000);
                MsgTimeout msgTimeout1 = new MsgTimeout(st1);
                st1.setTimeoutEvent(msgTimeout1);

                ScheduleTimeout st2 = new ScheduleTimeout(1000);
                MsgTimeout msgTimeout2 = new MsgTimeout(st2);
                st2.setTimeoutEvent(msgTimeout2);

                ScheduleTimeout st3 = new ScheduleTimeout(1000);
                MsgTimeout msgTimeout3 = new MsgTimeout(st3);
                st3.setTimeoutEvent(msgTimeout3);

                ScheduleTimeout st4 = new ScheduleTimeout(1000);
                MsgTimeout msgTimeout4 = new MsgTimeout(st4);
                st4.setTimeoutEvent(msgTimeout4);

                logger.debug("sending messages....");
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(src1, src1Port, src1Id)), ToVodAddr.systemAddr(new Address(dest1, dest1Port, dest1Id)), msgTimeout1.getTimeoutId()), emulator.getPositive(VodNetwork.class));
                // this packet can get in
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(dest1, dest1Port, dest1Id)), ToVodAddr.systemAddr(new Address(natIP, src1Port, 100)), msgTimeout2.getTimeoutId()), emulator.getNegative(VodNetwork.class));
                // so does this message. same host but different port
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(dest1, dest1Port + 4, dest1Id)), ToVodAddr.systemAddr(new Address(natIP, src1Port, 100)), msgTimeout3.getTimeoutId()), emulator.getNegative(VodNetwork.class));
                // this packet cant get in
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(dest3, dest3Port, dest3Id)), ToVodAddr.systemAddr(new Address(natIP, src1Port, 100)), msgTimeout1.getTimeoutId()/*any TimeoutId*/), emulator.getNegative(VodNetwork.class));
                // this packet also cant get in
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(dest2, dest2Port, dest2Id)), ToVodAddr.systemAddr(new Address(natIP, src1Port, 100)), msgTimeout1.getTimeoutId()/*any TimeoutId*/), emulator.getNegative(VodNetwork.class));

                trigger(st1, timer.getPositive(Timer.class));
                trigger(st2, timer.getPositive(Timer.class));
                trigger(st3, timer.getPositive(Timer.class));

                incomingMsgs = 3; // one will be dropped so, we should not expect it

            }
        };
        private Handler<TestMsgId> handleUpperMessage = new Handler<TestMsgId>() {

            public void handle(TestMsgId event) {
                logger.debug("JTest Handle Upper Message");
                trigger(new CancelTimeout(event.getTimeoutId()), timer.getPositive(Timer.class));
                logger.debug("Jtest upper. message recvd from " + event.getSource().getIp());
                if (event.getSource().getIp().equals(dest1)) // not expecting any message from any other destination
                {
                    incomingMsgs--;
                    if (incomingMsgs == 0) {
                        testObj.pass();
                    }
                } else {
                    testObj.fail(true);
                }
            }
        };
        private Handler<TestMsgId> handleLowerMessage = new Handler<TestMsgId>() {

            public void handle(TestMsgId event) {
                logger.debug("JTest Handle lower message");
                trigger(new CancelTimeout(event.getTimeoutId()), timer.getPositive(Timer.class));

                if (event.getDestination().getIp().equals(dest1))
                {
                    if (event.getSource().getIp().equals(natIP)
                        && event.getSource().getPort() == src1Port) // coz of PP
                    {
                        incomingMsgs--;
                        if (incomingMsgs == 0)
                        {
                            testObj.pass();
                        }
                    }
                    else
                    {
                        testObj.fail(true);
                    }
                } 
            }
        };
        public Handler<MsgTimeout> handleMsgTimeout = new Handler<MsgTimeout>() {

            public void handle(MsgTimeout event) {
                logger.debug("timeout");
                testObj.fail(true);
            }
        };
    }
    private static final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    public void testApp() {
        setTestObj(this);

        Kompics.createAndStart(TestEmulatorComponent.class, 1);


        try {
            HD_PP_HD_Test.semaphore.acquire(EVENT_COUNT);
            logger.debug("Exiting unit test....");
            if (!testStatus) // test failed
            {
                assertTrue(false);
            }
        } catch (InterruptedException e) {
            assert (false);
        }
        finally {
            Kompics.shutdown();
        }
    }



    public void pass() {
        semaphore.release();
    }

    public void fail(boolean release) {
        if (release == true) {
            testStatus = false;
            semaphore.release();
        }

    }

    protected void setUp() {
        try {
 src1 = InetAddress.getLocalHost();
            src2 = InetAddress.getLocalHost();
            dest1 = InetAddress.getLocalHost();
            dest2 = InetAddress.getLocalHost();
            dest3 = InetAddress.getLocalHost();
            natIP = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            logger.error("Unkown Host Exception");
        }
    }
}
