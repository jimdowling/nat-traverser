/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.parentmaker;

import se.sics.gvod.config.ParentMakerConfiguration;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import se.sics.gvod.timer.TimeoutId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.common.RTTStore;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfNoUtility;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.hp.msgs.HpRegisterMsg;
import se.sics.gvod.hp.msgs.HpRegisterMsg.RegisterStatus;
import se.sics.gvod.hp.msgs.HpUnregisterMsg;
import se.sics.gvod.hp.msgs.ParentKeepAliveMsg;
import se.sics.gvod.parentmaker.ParentMaker.KeepBindingOpenTimeout;
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
            VodConfig.init(new String[] {});
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ParentMakerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        parentMaker = new ParentMaker(this);

        parentMaker.handleInit.handle(new ParentMakerInit(this,  
                ParentMakerConfiguration.build(). 
                setParentUpdatePeriod(30*1000)
                .setRto(5*1000)
                .setRtoScale(1.5)
                .setRtoRetries(4)
                .setKeepParentRttRange(200)
                ));
        
        mySelf = new SelfNoUtility(privAddrs.get(2));
        parentMaker.handleInit.handle(new ParentMakerInit(mySelf, 
//                30*1000, 2, 5000, 1.5, 4, 200, 0
                ParentMakerConfiguration.build(). 
                setParentUpdatePeriod(30*1000)
                .setRto(5*1000)
                .setRtoScale(1.5)
                .setRtoRetries(4)
                .setKeepParentRttRange(200)                
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
        assertSequence(events, HpRegisterMsg.Request.class);
        HpRegisterMsg.Request r = (HpRegisterMsg.Request) events.get(0);
        Set<Integer> ports = new HashSet<Integer>();
        ports.add(12121);
        parentMaker.handleHpRegisterMsgResponse.handle(
                new HpRegisterMsg.Response(pubAddrs.get(0), privAddrs.get(0), 
                        HpRegisterMsg.RegisterStatus.ACCEPT, r.getTimeoutId(),
                        ports));
        
        assert(parentMaker.self.getParents().size() == 1);
        assert(parentMaker.connections.size() == 1);
        events = pollEvent(1);
        assertSequence(events, KeepBindingOpenTimeout.class);

        SchedulePeriodicTimeout spt2 = new SchedulePeriodicTimeout(0, 5000);
        parentMaker.handleKeepBindingOpenTimeout.handle(new KeepBindingOpenTimeout(spt2, pubAddrs.get(0)));
        events = pollEvent(1);
        assertSequence(events, ParentKeepAliveMsg.Ping.class);

        parentMaker.handleUnregisterParentRequest.handle(
                new HpUnregisterMsg.Request(pubAddrs.get(0), privAddrs.get(0), 0, 
                        RegisterStatus.BETTER_PARENT));
        
        assert(parentMaker.connections.isEmpty());
    }



}
