/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.croupier;

import se.sics.gvod.config.CroupierConfiguration;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.croupier.events.CroupierInit;
import se.sics.gvod.croupier.events.CroupierJoin;
import se.sics.gvod.croupier.events.CroupierJoinCompleted;
import se.sics.gvod.croupier.events.CroupierShuffleCycle;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;
import se.sics.gvod.timer.SchedulePeriodicTimeout;

/**
 *
 */
public class CroupierTest extends VodRetryComponentTestCase {
    
    private static Logger logger = LoggerFactory.getLogger(CroupierTest.class);
    
    Croupier croupier = null;
    long shufflePeriod;
    int shuffleLength;
    int shuffleTimeout;
    int seed;
    int viewSize;
    InetAddress address1;
    InetAddress address2;
    VodAddress vodAddress1;
    VodAddress vodAddress2;
    VodDescriptor desc1;
    VodDescriptor desc2;
    List<VodDescriptor> neighbours;

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
        shufflePeriod = 1000;
        shuffleLength = 2;
        shuffleTimeout = 500;
        seed = 300;
        viewSize = 2;
        neighbours = new ArrayList<VodDescriptor>();

        try {
            address1 = InetAddress.getByName("192.168.0.2");
            address2 = InetAddress.getByName("192.168.0.3");

            vodAddress1 = ToVodAddr.systemAddr(new Address(address1, 8082, 2));
            vodAddress2 = ToVodAddr.systemAddr(new Address(address2, 8083, 3));

            desc1 = new VodDescriptor(vodAddress1, new UtilityVod(0), 0, 0);
            desc2 = new VodDescriptor(vodAddress2, new UtilityVod(0), 0, 0);
            VodConfig.init(new String[]{});
        } catch (IOException ex) {
            logger.error(null, ex);
        }

        CroupierConfiguration config = 
                CroupierConfiguration.build()
                .setPolicy(VodConfig.CroupierSelectionPolicy.TAIL.name())
                .setShuffleLength(shuffleLength)
                .setShufflePeriod(shufflePeriod)
                .setShuffleTimeout(shuffleTimeout)
                .setViewSize(viewSize)
                .setSeed(seed)
                ;
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
    


    @Test
    public void testJoin() {
        neighbours.add(desc1);
        neighbours.add(desc2);
        croupier.handleJoin.handle(new CroupierJoin(neighbours));
        assert (croupier.privateView.isEmpty());
        assert (croupier.publicView.size() == 2);
    }

    
    @Test
    public void testShuffleCycle() {
        neighbours.add(desc1);
        neighbours.add(desc2);
        croupier.handleJoin.handle(new CroupierJoin(neighbours));
        croupier.handleCycle.handle(new CroupierShuffleCycle(new SchedulePeriodicTimeout(shufflePeriod, shufflePeriod)));
    }

//    @Test
//    public void testRebootstrap() {
//        eventList.clear();
//        croupier.handleJoin.handle(new CroupierJoin(neighbours));
//        croupier.rebootstrap();
//        
//        LinkedList<Event> events = pollEvent(3);
//        assertSequence(events, CroupierShuffleCycle.class, CroupierJoinCompleted.class, 
//                Rebootstrap.class);
//        
//        neighbours.add(desc1);
//        croupier.handleRebootstrapResponse.handle(new RebootstrapResponse(this.getId(),
//                neighbours));
//        assert (croupier.privateView.isEmpty());
//        assert (croupier.publicView.size() == 1);
//    }

    @Test
    public void testShuffleTimeout() {
        neighbours.add(desc1);
        neighbours.add(desc2);
        croupier.handleJoin.handle(new CroupierJoin(neighbours));
    }
}
