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
public class PD_RD_PD_Test
        extends TestCase
{

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
    private static final Logger logger = LoggerFactory.getLogger(PD_RD_PD_Test.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PD_RD_PD_Test(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(PD_RD_PD_Test.class);
    }

    public static void setTestObj(PD_RD_PD_Test testObj)
    {
        TestEmulatorComponent.testObj = testObj;

    }

    public static class MsgTimeout extends Timeout
    {

        public MsgTimeout(ScheduleTimeout request)
        {
            super(request);
        }
    }

    public static class TestEmulatorComponent extends ComponentDefinition
    {

        private Component emulator;
        private Component timer;
        private static PD_RD_PD_Test testObj = null;
        int firstMappedPort = 0;
        int secondMappedPort = 0;
        int timeout = 5000;

        public TestEmulatorComponent()
        {
            emulator = create(DistributedNatGatewayEmulator.class);
            timer = create(JavaTimer.class);


            subscribe(handleStart, control);
            subscribe(handleLowerMessage, emulator.getNegative(VodNetwork.class));
            subscribe(handleUpperMessage, emulator.getPositive(VodNetwork.class));
            subscribe(handleMsgTimeout, timer.getPositive(Timer.class));

            trigger(new DistributedNatGatewayEmulatorInit(50000, 
                    Nat.MappingPolicy.PORT_DEPENDENT,
                    Nat.AllocationPolicy.RANDOM,
                    Nat.AlternativePortAllocationPolicy.PORT_CONTIGUITY, /* alternaive policy if there is clash using PP */
                    Nat.FilteringPolicy.PORT_DEPENDENT,
                    Nat.Type.NAT, /* types are NAT OPEN and UPNP */
                    3000 /* rule cleanup timer */,
                    testObj.natIP /* ip of NAT Gateway*/,
                    65533 /* max port */,
                    false /* clashing overrides */,
                    30000 /* rule life time */,
                    55 /*rand port seed*/,
                    false /* Upnp not enabled */), emulator.getControl());

            connect(emulator.getNegative(Timer.class), timer.getPositive(Timer.class));
        }
        public Handler<Start> handleStart = new Handler<Start>()
        {

            public void handle(Start event)
            {

                ScheduleTimeout st1 = new ScheduleTimeout(timeout);
                MsgTimeout msgTimeout1 = new MsgTimeout(st1);
                st1.setTimeoutEvent(msgTimeout1);

                ScheduleTimeout st2 = new ScheduleTimeout(timeout);
                MsgTimeout msgTimeout2 = new MsgTimeout(st2);
                st2.setTimeoutEvent(msgTimeout2);

                ScheduleTimeout st3 = new ScheduleTimeout(timeout);
                MsgTimeout msgTimeout3 = new MsgTimeout(st3);
                st3.setTimeoutEvent(msgTimeout3);

                ScheduleTimeout st4 = new ScheduleTimeout(timeout);
                MsgTimeout msgTimeout4 = new MsgTimeout(st4);
                st4.setTimeoutEvent(msgTimeout4);

                logger.debug("sending messages....");
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(src1, src1Port, src1Id)), ToVodAddr.systemAddr(new Address(dest1, dest1Port, dest1Id)), msgTimeout1.getTimeoutId()), emulator.getPositive(VodNetwork.class));
                // sending to dest1 again but to different port and then recving from it agin
                trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(src1, src1Port, src1Id)), ToVodAddr.systemAddr(new Address(dest1, dest1Port + 1/*different dest port*/, dest1Id)), msgTimeout2.getTimeoutId()), emulator.getPositive(VodNetwork.class));

                trigger(st1, timer.getPositive(Timer.class));
                trigger(st2, timer.getPositive(Timer.class));

                incomingMsgs = 2;

            }
        };
        private Handler<TestMsgId> handleUpperMessage = new Handler<TestMsgId>()
        {

            public void handle(TestMsgId event)
            {
                logger.debug("JTest Handle Upper Message");
                trigger(new CancelTimeout(event.getTimeoutId()), timer.getPositive(Timer.class));
                logger.debug("timer killed " + event.getTimeoutId());
                logger.debug("Jtest upper. message recvd from " + event.getSource().getIp() + " port " + event.getSource().getPort());
                if (incomingMsgs > 0
                        && (event.getSource().getIp().equals(dest1) && (event.getSource().getPort() == dest1Port))
                        || (event.getSource().getIp().equals(dest1) && event.getSource().getPort() == (dest1Port + 1))) // not expecting any message from any other destination
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
        };
        private Handler<TestMsgId> handleLowerMessage = new Handler<TestMsgId>()
        {

            public void handle(TestMsgId event)
            {
                logger.debug("JTest Handle lower message");

                trigger(new CancelTimeout(event.getTimeoutId()), timer.getPositive(Timer.class));
                logger.debug("timer killed " + event.getTimeoutId());
                if (event.getDestination().getIp().equals(dest1)
                        && event.getDestination().getId() == dest1Id
                        && event.getDestination().getPort() == dest1Port)
                {
                    firstMappedPort = event.getSource().getPort();
                    incomingMsgs--;
                }
                else if (event.getDestination().getIp().equals(dest1)
                        && event.getDestination().getId() == dest1Id
                        && event.getDestination().getPort() == dest1Port + 1)
                {
                    secondMappedPort = event.getSource().getPort();
                    incomingMsgs--;

                    if (firstMappedPort != secondMappedPort)
                    {
                        logger.debug("sending msgs back");
                        ScheduleTimeout st1 = new ScheduleTimeout(timeout);
                        MsgTimeout msgTimeout1 = new MsgTimeout(st1);
                        st1.setTimeoutEvent(msgTimeout1);

                        ScheduleTimeout st2 = new ScheduleTimeout(timeout);
                        MsgTimeout msgTimeout2 = new MsgTimeout(st2);
                        st2.setTimeoutEvent(msgTimeout2);

                        ScheduleTimeout st3 = new ScheduleTimeout(timeout);
                        MsgTimeout msgTimeout3 = new MsgTimeout(st3);
                        st3.setTimeoutEvent(msgTimeout3);

                        ScheduleTimeout st4 = new ScheduleTimeout(timeout);
                        MsgTimeout msgTimeout4 = new MsgTimeout(st4);
                        st4.setTimeoutEvent(msgTimeout4);

                        logger.debug("sending messages back....");
                        // this msg should not get in
                        trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(dest1, dest1Port + 2/*not mapped*/, dest1Id)), ToVodAddr.systemAddr(new Address(natIP, firstMappedPort, 1)), null), emulator.getNegative(VodNetwork.class));
                        // this msg should not get in
                        trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(dest3, dest3Port/*not mapped*/, dest3Id)), ToVodAddr.systemAddr(new Address(natIP, secondMappedPort, 1)), null), emulator.getNegative(VodNetwork.class));
                        // this should not get in
                        trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(dest1, dest1Port, dest1Id)), ToVodAddr.systemAddr(new Address(natIP, secondMappedPort/*wrong mapped port. i am fucked if it gets through nat*/, 1)), null), emulator.getNegative(VodNetwork.class));
                        // but these can get in
                        trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(dest1, dest1Port, dest1Id)), ToVodAddr.systemAddr(new Address(natIP, firstMappedPort, 1)), msgTimeout1.getTimeoutId()), emulator.getNegative(VodNetwork.class));
                        trigger(new TestMsgId(ToVodAddr.systemAddr(new Address(dest1, dest1Port + 1, dest1Id)), ToVodAddr.systemAddr(new Address(natIP, secondMappedPort, 1)), msgTimeout2.getTimeoutId()), emulator.getNegative(VodNetwork.class));


                        trigger(st1, timer.getPositive(Timer.class));
                        trigger(st2, timer.getPositive(Timer.class));

                        incomingMsgs = 2;
                    }
                    else
                    {
                        testObj.fail(true);
                    }
                }
                else
                {
                    testObj.fail(true);
                }
            }
        };
        public Handler<MsgTimeout> handleMsgTimeout = new Handler<MsgTimeout>()
        {

            public void handle(MsgTimeout event)
            {
                logger.debug("timeout id " + event.getTimeoutId());
                testObj.fail(true);
            }
        };
    }
    private static final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    public void testApp()
    {
        setTestObj(this);

        Kompics.createAndStart(TestEmulatorComponent.class, 1);


        try
        {
            PD_RD_PD_Test.semaphore.acquire(EVENT_COUNT);
            logger.debug("Exiting unit test....");
            if (!testStatus) // test failed
            {
                assertTrue(false);
            }
        }
        catch (InterruptedException e)
        {
            assert (false);
        }
        finally
        {
            Kompics.shutdown();
        }
    }

    public void pass()
    {
        semaphore.release();
    }

    public void fail(boolean release)
    {
        if (release == true)
        {
            testStatus = false;
            semaphore.release();
        }

    }

    protected void setUp()
    {
        try
        {
            src1 = InetAddress.getLocalHost();
            src2 = InetAddress.getLocalHost();
            dest1 = InetAddress.getLocalHost();
            dest2 = InetAddress.getLocalHost();
            dest3 = InetAddress.getLocalHost();
            natIP = InetAddress.getLocalHost();
        }
        catch (UnknownHostException ex)
        {
            logger.error("Unkown Host Exception");
        }
    }
}
