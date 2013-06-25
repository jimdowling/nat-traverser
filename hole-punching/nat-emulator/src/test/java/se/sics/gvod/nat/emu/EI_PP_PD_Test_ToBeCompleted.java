package se.sics.gvod.nat.emu;

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
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.nat.common.TestMsgId;
import se.sics.gvod.nat.emu.events.DistributedNatGatewayEmulatorInit;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;

/**
 * Unit test for simple App.
 */
public class EI_PP_PD_Test_ToBeCompleted
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
    private static final Logger logger = LoggerFactory.getLogger(EI_PP_PD_Test_ToBeCompleted.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public EI_PP_PD_Test_ToBeCompleted(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(EI_PP_PD_Test_ToBeCompleted.class);
    }

    public static void setTestObj(EI_PP_PD_Test_ToBeCompleted testObj) {
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
        private static EI_PP_PD_Test_ToBeCompleted testObj = null;

        public TestEmulatorComponent() {
            emulator = create(DistributedNatGatewayEmulator.class);
            timer = create(JavaTimer.class);

            subscribe(handleStart, control);
            subscribe(handleLowerMessage, emulator.getNegative(VodNetwork.class));
            subscribe(handleUpperMessage, emulator.getPositive(VodNetwork.class));
            subscribe(handleMsgTimeout, timer.getPositive(Timer.class));

            trigger(new DistributedNatGatewayEmulatorInit(50000, 
                    Nat.MappingPolicy.ENDPOINT_INDEPENDENT,
                    Nat.AllocationPolicy.PORT_PRESERVATION,
                    Nat.AlternativePortAllocationPolicy.PORT_CONTIGUITY, /* alternaive policy if there is clash using PP */
                    Nat.FilteringPolicy.PORT_DEPENDENT,
                    Nat.Type.NAT, /* types are NAT OPEN and UPNP */
                    1000 /* rule cleanup timer */,
                    testObj.natIP /* ip of NAT Gateway*/,
                    65533 /* max port */,
                    false /* clashing overrides */,
                    5000 /* rule life time */,
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

                logger.debug("sending message....");
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(src1, src1Port, src1Id)), ToVodAddr.systemAddr(new Address(dest1, dest1Port, dest1Id)), msgTimeout1.getTimeoutId()), emulator.getPositive(VodNetwork.class));
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(src1, src1Port, src1Id)), ToVodAddr.systemAddr(new Address(dest2, dest2Port, dest2Id)), msgTimeout2.getTimeoutId()), emulator.getPositive(VodNetwork.class));
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(src1, src1Port+1, src1Id)), ToVodAddr.systemAddr(new Address(dest2, dest2Port, dest2Id)), msgTimeout2.getTimeoutId()), emulator.getPositive(VodNetwork.class));
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(src1, src1Port+1, src1Id)), ToVodAddr.systemAddr(new Address(dest2, dest1Port, dest2Id)), msgTimeout2.getTimeoutId()), emulator.getPositive(VodNetwork.class));

//                trigger(new TestMsgId(new Address(dest1, dest1Port, dest1Id), new Address(natIP, src1Port, natId), msgTimeout3.getTimeoutId()), emulator.getNegative(GVodNetwork.class));
//                trigger(new TestMsgId(new Address(dest2, dest2Port, dest2Id), new Address(natIP, src1Port, natId), msgTimeout4.getTimeoutId()), emulator.getNegative(GVodNetwork.class));
//                trigger(new TestMsgId(new Address(dest2, dest2Port+1, dest2Id), new Address(natIP, src1Port, natId), msgTimeout4.getTimeoutId()), emulator.getNegative(GVodNetwork.class));

                testObj.incomingMsgs = 3;
            }
        };
        private Handler<TestMsgId> handleUpperMessage = new Handler<TestMsgId>() {

            public void handle(TestMsgId event) {
                logger.debug("JTest Upper Message Handler msg recvd src: "+event.getSource()+" dest "+event.getDestination());
            }
        };
        private Handler<TestMsgId> handleLowerMessage = new Handler<TestMsgId>() {

            public void handle(TestMsgId event)
            {
                logger.debug("JTest Lower Message Handler");
                

                trigger(new TestMsgId(event.getVodDestination(), event.getVodSource(), null), emulator.getNegative(VodNetwork.class));


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
            EI_PP_PD_Test_ToBeCompleted.semaphore.acquire(EVENT_COUNT);
            logger.debug("Exiting unitl test....");
            if (!testStatus) // test failed
            {
                assertTrue(false);
            } else {
                assertTrue(true);
            }
        } catch (InterruptedException e) {
            assert (false);
        } finally {
            Kompics.shutdown();
        }
    }

    public void pass() {
        testStatus = true;
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
