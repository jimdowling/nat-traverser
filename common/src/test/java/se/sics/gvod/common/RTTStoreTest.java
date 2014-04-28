package se.sics.gvod.common;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.RTTStore.RTT;
import se.sics.gvod.common.net.RttStats;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author jim
 */
public class RTTStoreTest {

    private static int nodeId = 10;
    private static InetAddress address1 = null;
    private static InetAddress address2 = null;
    private static VodAddress vodAddress1 = null;
    private static VodAddress vodAddress2 = null;

    public RTTStoreTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        address1 = InetAddress.getByName("192.168.0.1");
        address2 = InetAddress.getByName("192.168.0.2");
        vodAddress1 = ToVodAddr.systemAddr(new Address(address1, 8081, 1));
        vodAddress2 = ToVodAddr.systemAddr(new Address(address2, 8082, 2));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        RTTStore.cleanup();
    }

    @Test
    public void testAddSample() {
        RTT rtt = RTTStore.getRtt(nodeId, vodAddress1);

        assert (rtt == null);

        rtt = RTTStore.addSample(nodeId, vodAddress1, 10);

//        assert (rtt.mostRecent() == 10);
        RTTStore.addSample(nodeId, vodAddress1, 20);
        RTTStore.addSample(nodeId, vodAddress2, 30);

        List<RTT> rtts = RTTStore.getOnAvgBest(nodeId, 2);
        Collections.sort(rtts, RTT.Order.ByRto);
        assert(rtts.get(0).getRTO() < rtts.get(1).getRTO() );
//        assert (rtt.mostRecent() == 30);
//        assert (rtt.avg() == 20);
//        assert (rtt.getRTO() == 20);
        RttStats stats = rtt.getRttStats();
    }

    @Test
    public void testDecay() {
        try {
            for (int i = 0; i < 450; i++) {
                RTTStore.addSample(nodeId, vodAddress1, 10);
            }

            for (int i = 0; i < 5; i++) {
                RTTStore.addSample(nodeId, vodAddress2, 20);
            }

            RTT rtt2 = RTTStore.getRtt(nodeId, vodAddress2);
            
            rtt2.decay();
            long rto = rtt2.getRTO();
            
            Thread.sleep(RTTStore.SAMPLE_VALIDITY_PERIOD + 500);
            rtt2.decay();
            assert (rtt2.getRTO() >= rto);
        } catch (InterruptedException ex) {
            Logger.getLogger(RTTStoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void testGetOnAvgBetterRtts() {
        List<RTT> allOnAvgBetterRtts = RTTStore.getAllOnAvgBetterRtts(nodeId, 100, 0);
        assert (allOnAvgBetterRtts.isEmpty());  
        
        RTTStore.addSample(nodeId, vodAddress1, 4);
        RTTStore.addSample(nodeId, vodAddress1, 6);
        RTTStore.addSample(nodeId, vodAddress1, 5);
        RTTStore.addSample(nodeId, vodAddress1, 3);
        RTTStore.addSample(nodeId, vodAddress1, 7);
        
        RTTStore.addSample(nodeId, vodAddress2, 10);
        RTTStore.addSample(nodeId, vodAddress2, 11);
        RTTStore.addSample(nodeId, vodAddress2, 9);
        RTTStore.addSample(nodeId, vodAddress2, 13);
        RTTStore.addSample(nodeId, vodAddress2, 8);
        
        List<RTT> onAVgBests = RTTStore.getOnAvgBest(nodeId, 1);
        
        assert (onAVgBests.size() == 1);
        assert (onAVgBests.get(0).getAddress().equals(vodAddress1));
        
        List<RTT> onAvgBetterRtts = RTTStore.getOnAvgBetterRtts(nodeId, 5, 20, 5);
        
//        assert (onAvgBetterRtts.size() == 2);
        
        List<RTT> onAvgBetterRtts2 = RTTStore.getOnAvgBetterRtts(nodeId, 5, 25, 3);
        
        assert (onAvgBetterRtts2.size() == 1);
        assert (onAvgBetterRtts2.get(0).getAddress().equals(vodAddress1));
    }
 
}
