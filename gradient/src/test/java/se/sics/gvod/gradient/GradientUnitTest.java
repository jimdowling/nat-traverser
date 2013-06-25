/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient;

import se.sics.gvod.config.GradientConfiguration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.croupier.events.CroupierSample;
import se.sics.gvod.gradient.events.GradientSample;
import se.sics.gvod.gradient.events.GradientSetsExchangeCycle;
import se.sics.gvod.gradient.msgs.GradientSetsExchangeMsg;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Event;

/**
 *
 * @author Owner
 */
public class GradientUnitTest extends VodRetryComponentTestCase {

    Gradient gradient = null;
    LinkedList<Event> events;
    List<VodDescriptor> nodes;
    List<VodDescriptor> updates;
    VodDescriptor n1, n2, n3, n4;
        
    public GradientUnitTest() {
        super();
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
        gradient = new Gradient(this);
        gradient.handleInit.handle(new GradientInit(this, GradientConfiguration.build()));
        events = pollEvent(1);
        assertSequence(events, GradientSetsExchangeCycle.class);

        VodAddress v1 = new VodAddress(pubAddrs.get(0).getPeerAddress(), 
                Gradient.SYSTEM_GRADIENT_OVERLAY_ID);
        VodAddress v2 = new VodAddress(pubAddrs.get(1).getPeerAddress(), 
                Gradient.SYSTEM_GRADIENT_OVERLAY_ID);
        VodAddress v3 = new VodAddress(pubAddrs.get(2).getPeerAddress(), 
                Gradient.SYSTEM_GRADIENT_OVERLAY_ID);
        VodAddress v4 = new VodAddress(pubAddrs.get(3).getPeerAddress(), 
                Gradient.SYSTEM_GRADIENT_OVERLAY_ID);
        
        n1 = new VodDescriptor(v1, new UtilityVod(10), 0, 1500);
        n2 = new VodDescriptor(v2, new UtilityVod(12), 0, 1500);
        n3 = new VodDescriptor(v3, new UtilityVod(20), 5, 1500);
        n4 = new VodDescriptor(v4, new UtilityVod(40), 0, 1500);
        
        nodes = new ArrayList<VodDescriptor>();
        updates = new ArrayList<VodDescriptor>();
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);
        updates.add(n4);
        n3 = new VodDescriptor(v3, new UtilityVod(50), 2, 1500);
        updates.add(n3);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testGradientView() {
        gradient.handleCroupierSample.handle(new CroupierSample(nodes));
        assert(gradient.similarSet.getAllSimilarPeers().size() == 3);
        gradient.handleCroupierSample.handle(new CroupierSample(nodes));
        assert(gradient.similarSet.getAllSimilarPeers().size() == 3);
        SchedulePeriodicTimeout st = new SchedulePeriodicTimeout(0, 1000);
        GradientSetsExchangeCycle gsec = new GradientSetsExchangeCycle(st);
        st.setTimeoutEvent(gsec);
        TimeoutId id = gsec.getTimeoutId();
        gradient.handleCycle.handle(gsec);
        events = pollEvent(1);
        GradientSetsExchangeMsg.Request req = (GradientSetsExchangeMsg.Request) events.getFirst();
        assertSequence(events, GradientSetsExchangeMsg.Request.class);
        gradient.handleSetsExchangeResponse.handle(
                new GradientSetsExchangeMsg.Response(n1.getVodAddress(), n2.getVodAddress(), 
                        n3.getVodAddress(), req.getTimeoutId(), updates));
        events = pollEvent(1);
        assertSequence(events, GradientSample.class);
        assert(gradient.similarSet.getAllSimilarPeers().size() == 5);
        
        List<VodDescriptor> nearest = gradient.similarSet.getSimilarPeers(1, new UtilityVod(10));
        assert(nearest.size() == 1);
        assert(((UtilityVod)nearest.get(0).getUtility()).getChunk() == 10);
        nearest = gradient.similarSet.getSimilarPeers(1, new UtilityVod(38));
        assert(nearest.size() == 1);
        assert(((UtilityVod)nearest.get(0).getUtility()).getChunk() == 40);
        
        VodDescriptor highest = gradient.similarSet.getHighestUtilityPeer();
        assert(((UtilityVod)highest.getUtility()).getChunk() == 50);
        ((UtilityVod)utility).setChunk(11);
        VodDescriptor lowest = gradient.similarSet.getWorstUtilityNode();
        assert(((UtilityVod)lowest.getUtility()).getChunk() == 10);
        
        // utility is '0' - prefer n1 with u=10
//        assert(gradient.similarSet.getAllSimilarPeers().equals(n1.getVodAddress()));
//        System.out.println(gradient.similarSet.getBestSimilarPeerAddress());
        this.updateUtility(new UtilityVod(41));
        System.out.println(gradient.similarSet.getBestSimilarPeerAddress());
        assert(gradient.similarSet.getBestSimilarPeerAddress().equals(n3.getVodAddress()));
        
    }
}
