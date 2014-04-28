/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.parentmaker;

import se.sics.gvod.config.ParentMakerConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import se.sics.gvod.timer.TimeoutId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.NatFactory;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.common.RTTStore;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfNoUtility;
import se.sics.gvod.common.msgs.NatReportMsg;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.hp.msgs.HpRegisterMsg;
import se.sics.gvod.hp.msgs.HpRegisterMsg.RegisterStatus;
import se.sics.gvod.hp.msgs.HpUnregisterMsg;
import se.sics.gvod.hp.msgs.ParentKeepAliveMsg;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.parentmaker.ParentMaker.KeepBindingOpenTimeout;
import se.sics.gvod.parentmaker.evts.PrpPortsResponse;
import se.sics.kompics.Event;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;

public class ParentMakerTest extends VodRetryComponentTestCase {

    private static Logger logger = LoggerFactory.getLogger(ParentMakerTest.class);
    ParentMaker parentMaker = null;
    TimeoutId timeoutId;
    Self mySelf;

    public ParentMakerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            VodConfig.init(new String[]{});
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ParentMakerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        parentMaker = new ParentMaker(this);

        Nat eiPpPd = new NatFactory(1).getEiPpPd();
        setNat(eiPpPd);
        parentMaker.handleInit.handle(new ParentMakerInit(this,
                ParentMakerConfiguration.build().
                setParentUpdatePeriod(30 * 1000)
                .setRto(5 * 1000)
                .setRtoScale(1.5)
                .setRtoRetries(4)
                .setKeepParentRttRange(200),
                new ConcurrentSkipListSet<Integer>()
        ));

        mySelf = new SelfNoUtility(privAddrs.get(2));
        mySelf.setNat(eiPpPd);
        parentMaker.handleInit.handle(new ParentMakerInit(mySelf,
                ParentMakerConfiguration.build().
                setParentUpdatePeriod(30 * 1000)
                .setRto(5 * 1000)
                .setRtoScale(1.5)
                .setRtoRetries(4)
                .setKeepParentRttRange(200),
                new ConcurrentSkipListSet<Integer>()
        ));
        LinkedList<Event> events = pollEvent(1);
        assertSequence(events, ParentMakerCycle.class);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
        parentMaker.stop(null);
    }

    @Test
    public void testCycle() {
        RTTStore.addSample(mySelf.getId(), pubAddrs.get(0), 200);
        RTTStore.addSample(mySelf.getId(), pubAddrs.get(1), 50);

        ScheduleTimeout spt = new ScheduleTimeout(5000);
        spt.setTimeoutEvent(new ParentMakerCycle(spt));
        timeoutId = spt.getTimeoutEvent().getTimeoutId();
        parentMaker.handleCycle.handle(new ParentMakerCycle(spt));

        LinkedList<Event> events = popEvents();
        List<PrpPortsResponse> pars = new ArrayList<PrpPortsResponse>();
        Random random = new Random(System.currentTimeMillis());
        for (int j=1; j<3; j++) {
            PortAllocRequest par = (PortAllocRequest) events.get(j);
            pars.add((PrpPortsResponse) par.getResponse());
        }
        for (int i = 0; i < 2; i++) {
            events.clear();
            PrpPortsResponse ppr = pars.get(i);
            Set<Integer> ports = new HashSet<Integer>();
            ports.add(random.nextInt(65535));
            ports.add(random.nextInt(65535));
            ppr.setAllocatedPorts(ports);
            parentMaker.handlePrpPortsResponse.handle(ppr);
            events = popEvents();
            HpRegisterMsg.Request r = (HpRegisterMsg.Request) events.get(0);
            parentMaker.handleHpRegisterMsgResponse.handle(
                    new HpRegisterMsg.Response(r.getVodDestination(), r.getVodSource(),
                            HpRegisterMsg.RegisterStatus.ACCEPT, r.getTimeoutId(),
                            ports));

            assert (parentMaker.self.getParents().size() == 1);
            assert (parentMaker.connections.size() == 1);
            events = pollEvent(1);
            assertSequence(events, KeepBindingOpenTimeout.class);

            SchedulePeriodicTimeout spt2 = new SchedulePeriodicTimeout(0, 5000);
            parentMaker.handleKeepBindingOpenTimeout.handle(
                    new KeepBindingOpenTimeout(spt2, r.getVodDestination()));
            events = popEvents();
            assertSequence(events, NatReportMsg.class, ParentKeepAliveMsg.Ping.class);

            parentMaker.handleUnregisterParentRequest.handle(
                    new HpUnregisterMsg.Request(r.getVodDestination(), r.getVodSource(), 0,
                            RegisterStatus.BETTER_PARENT));

            popEvents();
            assert(parentMaker.portsAssignedToParent.isEmpty());
            assert(parentMaker.portsInUse.isEmpty());
        }

        assert (parentMaker.connections.isEmpty());
    }

}
