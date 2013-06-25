/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.emu;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.msgs.RelayMsgNetty;
import se.sics.gvod.croupier.msgs.ShuffleMsg;
import se.sics.gvod.nat.emu.events.DistributedNatGatewayEmulatorInit;
import se.sics.gvod.nat.emu.events.NatPortBindResponse;
import se.sics.gvod.nat.emu.events.RuleCleanupTimeout;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.kompics.Event;

/**
 *
 * @author Owner
 */
public class DistNatGwEmuUnitTest extends VodRetryComponentTestCase {

    DistributedNatGatewayEmulator emu = null;
    LinkedList<Event> events;

    public DistNatGwEmuUnitTest() {
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
        try {
            VodConfig.init(new String[]{""});
        } catch (IOException ex) {
            Logger.getLogger(DistNatGwEmuUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testNat() {


        for (int i=0; i<numAddrs; i++) 
        {
            VodAddress pr = privAddrs.get(i);
            VodAddress pub = pubAddrs.get(i);
            VodDescriptor des = pubDescs.get(i);
            emu = new DistributedNatGatewayEmulator(this);
            InetAddress natIp = null;
            natIp = ipGenerator.generateIP();
            emu.handleInit.handle(new DistributedNatGatewayEmulatorInit(
                    pr.getNat(), natIp, 50000, 65000));
            events = pollEvent(1);
            assertSequence(events, RuleCleanupTimeout.class);
            emu.handleUpperMessage.handle(new ShuffleMsg.Request(pr, pub, null, des));
            events = pollEvent(1);
            assertSequence(events, PortBindRequest.class);
            PortBindRequest req = (PortBindRequest) events.getFirst();
            NatPortBindResponse resp = (NatPortBindResponse) req.getResponse();
            resp.setStatus(PortBindResponse.Status.SUCCESS);
            emu.handleNatPortBindResponse.handle(resp);
            events = pollEvent(1);
            assertSequence(events, ShuffleMsg.Request.class);
            ShuffleMsg.Request r = (ShuffleMsg.Request) events.getFirst();


            emu.handleLowerMessage.handle(new ShuffleMsg.Response(r.getVodDestination(), 
                    r.getVodSource(), r.getClientId(),
                    r.getRemoteId(), r.getVodSource(), r.getTimeoutId(), 
                    RelayMsgNetty.Status.OK, null, null));
            events = pollEvent(1);
            assertSequence(events, ShuffleMsg.Response.class);
        }
    }

    @Test
    public void testGradientView() {
//        emu.handleCroupierSample.handle(new CroupierSample(nodes));
//        assert(emu.similarSet.getAllSimilarPeers().size() == 3);
//        emu.handleCroupierSample.handle(new CroupierSample(nodes));
//        assert(emu.similarSet.getAllSimilarPeers().size() == 3);
//        SchedulePeriodicTimeout st = new SchedulePeriodicTimeout(0, 1000);
//        GradientSetsExchangeCycle gsec = new GradientSetsExchangeCycle(st);
//        st.setTimeoutEvent(gsec);
//        TimeoutId id = gsec.getTimeoutId();
//        emu.handleSetsExchangeCycle.handle(gsec);
//        events = pollEvent(1);
//        assertSequence(events, GradientSetsExchangeMsg.Request.class);
//        
//        emu.handleSetsExchangeResponse.handle(
//                new GradientSetsExchangeMsg.Response(n1.getVodAddress(), n2.getVodAddress(), 
//                        n3.getVodAddress(), id, updates));
//        events = pollEvent(1);
//        assertSequence(events, CroupierSample.class);
//        assert(emu.similarSet.getAllSimilarPeers().size() == 4);
//        
//        List<VodDescriptor> nearest = emu.similarSet.getSimilarPeers(1, new Utility(10));
//        assert(nearest.size() == 1);
//        assert(nearest.get(0).getUtility().getChunk() == 10);
//        nearest = emu.similarSet.getSimilarPeers(1, new Utility(38));
//        assert(nearest.size() == 1);
//        assert(nearest.get(0).getUtility().getChunk() == 40);
//        
//        VodDescriptor highest = emu.similarSet.getHighestUtilityPeer();
//        assert(highest.getUtility().getChunk() == 50);
//        utility.setChunk(11);
//        VodDescriptor lowest = emu.similarSet.getWorstUtilityNode();
//        assert(lowest.getUtility().getChunk() == 10);
//        
//        // utility is '0' - prefer n1 with u=10
////        assert(gradient.similarSet.getAllSimilarPeers().equals(n1.getVodAddress()));
////        System.out.println(gradient.similarSet.getBestSimilarPeerAddress());
//        this.updateUtility(new Utility(41));
//        System.out.println(emu.similarSet.getBestSimilarPeerAddress());
//        assert(emu.similarSet.getBestSimilarPeerAddress().equals(n3.getVodAddress()));
    }
}
