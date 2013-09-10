/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.croupier;

import se.sics.gvod.config.CroupierConfiguration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.DescriptorBuffer;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.croupier.events.CroupierInit;
import se.sics.gvod.croupier.events.CroupierJoin;
import se.sics.gvod.croupier.events.CroupierJoinCompleted;
import se.sics.gvod.croupier.events.CroupierSample;
import se.sics.gvod.croupier.events.CroupierShuffleCycle;
import se.sics.gvod.croupier.msgs.ShuffleMsg;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.kompics.Event;

/**
 *
 */
public class CroupierTest extends VodRetryComponentTestCase {

    private static Logger logger = LoggerFactory.getLogger(CroupierTest.class);
    Croupier croupier = null;
    int seed;
    List<VodDescriptor> neighbours;
    int numEntries = Math.min(pubDescs.size(), VodConfig.CROUPIER_VIEW_SIZE);

    public CroupierTest() {
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
        croupier = new Croupier(this);
        seed = 300;
        neighbours = new ArrayList<VodDescriptor>();

        CroupierConfiguration config =
                CroupierConfiguration.build()
                .setPolicy(VodConfig.CroupierSelectionPolicy.TAIL.name())
                .setSeed(seed);
        croupier.handleInit.handle(new CroupierInit(this, config));
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
        croupier.stop(null);
    }

    @Test
    public void testJoinComplete() {
        croupier.handleJoin.handle(new CroupierJoin(neighbours));
        LinkedList<Event> events = pollEvent(2);
        assertSequence(events, CroupierShuffleCycle.class, CroupierJoinCompleted.class);
    }

    private void join() {
        // pubDesc and privDescs are the same 
        for (int i = 0; i < pubDescs.size(); i++) {
            neighbours.add(pubDescs.get(i));
            if (i % (numEntries - 1) == 0) {
                croupier.handleJoin.handle(new CroupierJoin(neighbours));
            }
        }
    }

    @Test
    public void testJoin() {

        join();
        assert (croupier.privateView.isEmpty());
        assert (croupier.publicView.size() == numEntries);
    }

    @Test
    public void testShuffleCycle() {
        join();

        // pubDesc and privDescs are the same 
        List<VodDescriptor> pubNeighbours = new ArrayList<VodDescriptor>();
        List<VodDescriptor> privNeighbours = new ArrayList<VodDescriptor>();
        DescriptorBuffer descriptorBuffer = new DescriptorBuffer(self, pubNeighbours, privNeighbours);
        for (int i = 0; i < pubDescs.size(); i++) {
            pubNeighbours.add(pubDescs.get(i));
            privNeighbours.add(privDescs.get(i));
            if (i % VodConfig.CROUPIER_SHUFFLE_LENGTH == VodConfig.CROUPIER_SHUFFLE_LENGTH - 1) {
                int destId = new Random(VodConfig.getSeed()).nextInt(pubAddrs.size());
                ShuffleMsg.Request req = new ShuffleMsg.Request(self,
                        pubAddrs.get(destId),
                        descriptorBuffer, selfDesc);
                croupier.handleShuffleRequest.handle(req);
                pubDescs.clear();
                privDescs.clear();
                LinkedList<Event> e = pollEvent(2);

                // check response
                ShuffleMsg.Response sr = (ShuffleMsg.Response) e.get(0);
                assert (sr.getVodDestination().equals(pubAddrs.get(destId)));
                assert (sr.getBuffer().getFrom().equals(self));
                assert (sr.getBuffer().getFrom().equals(self));
                // check sample produced
                CroupierSample cs = (CroupierSample) e.get(1);
                assert (cs.getNodes().size() > 0);
            }
        }



    }

    @Test
    public void testCycle() {
        ScheduleTimeout st = new ScheduleTimeout(100);
        CroupierShuffleCycle csc = new CroupierShuffleCycle(st);
        croupier.handleCycle.handle(csc);
        assert (croupier.publicView.isEmpty());
        assert (croupier.privateView.isEmpty());

        join();
        popEvents();
        croupier.handleCycle.handle(csc);
        LinkedList<Event> e = pollEvent(1);
        ShuffleMsg.Request r = (ShuffleMsg.Request) e.getFirst();
        assert(r.getBuffer().getPublicDescriptors().size() > 0);
        assert(r.getBuffer().getPrivateDescriptors().isEmpty());
    }

    // TODO - shuffleRequest.Timeout
}
