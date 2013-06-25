/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient.snapshot;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.Self;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class GradientStats 
{
    private static Logger logger = LoggerFactory.getLogger(GradientStats.class);    
    private static volatile boolean collectData = false;
    private static AtomicInteger counter = new AtomicInteger(0);
    /**
     * A triple containing:
     * Map<OverlayId, (NodeId,Stats)>
     */
    protected static final ConcurrentHashMap<Integer,ConcurrentHashMap<Integer,Stats>> snapshotMap = 
            new ConcurrentHashMap<Integer,ConcurrentHashMap<Integer,Stats>>();
 
    protected GradientStats() {
        //hidden
    }
    public static Stats instance(Self self) {
        return getStat(self.getAddress());
    }

    /**
     * Given a node's address, give me its Stats object.
     * @param peer
     * @return 
     */
    public static Stats getStat(VodAddress peer) {
        int overlayId = peer.getOverlayId();
        int nodeId = peer.getId();
        ConcurrentHashMap<Integer,Stats> overlayStats;
        if (!snapshotMap.containsKey(overlayId)) {
            overlayStats = new ConcurrentHashMap<Integer,Stats>();
            snapshotMap.put(overlayId, overlayStats);
        } else {
            overlayStats = snapshotMap.get(overlayId);
        }
        Stats stats;
        if (!overlayStats.containsKey(nodeId)) {
            stats = new Stats();
            overlayStats.put(nodeId, stats);
        } else {
            stats = overlayStats.get(nodeId);
        }
        return stats;
    }
   
    public static boolean removeNode(int nodeId, int overlayId) {
        ConcurrentHashMap<Integer,Stats> overlayStats = snapshotMap.get(overlayId);
        if (overlayStats != null) {
            return (overlayStats.remove(nodeId) == null) ? false : true;
        } else {
            return false;
        }
    }
            
            
    
//-------------------------------------------------------------------
    public static void startCollectData() {
        logger.debug("\nStart collecting data ...\n");
        collectData = true;
    }

//-------------------------------------------------------------------
    public static void stopCollectData() {
        logger.debug("\nStop collecting data ...\n");
        for (int overlayId : snapshotMap.keySet()) {
            summaryReport(overlayId);
        }
        collectData = false;
    }

//-------------------------------------------------------------------
    public static void summaryReport(int overlayId) {
        StringBuilder sb = new StringBuilder("");
        sb.append("\ngradient current step: ").append(counter.incrementAndGet()).append("\t");
        sb.append("time: ").append(System.currentTimeMillis()).append("\n");
        sb.append(reportUtilityDifferences(overlayId));
        sb.append(reportNumRequestsResponses(overlayId));
        sb.append(reportNumCroupierSamples(overlayId));
        sb.append("\n###\n");

        logger.info(sb.toString());

    }    

    private static Set<Stats> getAllStats(int overlayId) {
        Set<Stats> nodes = new HashSet<Stats>();
        if (snapshotMap.get(overlayId) != null) {
            nodes.addAll(snapshotMap.get(overlayId).values());
        }
        return nodes;    
    }
    
    private static String reportUtilityDifferences(int overlayId) {
        StringBuilder sb = new StringBuilder();
        long sum = 0;
        for (Stats s : getAllStats(overlayId)) {
            sum += s.getSumNeighbourUtilities();
        }
        sb.append("Sum Utility Differences: ").append(sum).append("\n");
        return sb.toString();
    }
    
    private static String reportNumCroupierSamples(int overlayId) {
        StringBuilder sb = new StringBuilder();
        long sum = 0;
        for (Stats s : getAllStats(overlayId)) {
            sum += s.getNumCroupierSamples();
        }
        sb.append("Sum Croupier Samples: ").append(sum).append("\n");
        return sb.toString();
    }

    private static String reportNumRequestsResponses(int overlayId) {
        StringBuilder sb = new StringBuilder();
        long nrr = 0;
        long nres = 0;
        long nrs = 0;
        long nts = 0;
        for (Stats s : getAllStats(overlayId)) {
            nrr += s.getNumReqsRecvd();
            nres += s.getNumResps();
            nrs += s.getNumReqsSent();
            nts += s.getNumTimeouts();
        }
        sb.append("Reqs sent (").append(nrs).append("): Reqs recvd(").append(nrr)
                .append("): Resps recvd(").append(nres)
                .append("): Timeouts(").append(nts)
                .append(")\n");
        return sb.toString();
    }

    public static boolean isCollectData() {
        return collectData;
    }
    
}
