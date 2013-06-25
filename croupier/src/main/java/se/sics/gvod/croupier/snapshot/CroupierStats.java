/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.croupier.snapshot;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.RTTStore;
import se.sics.gvod.common.RTTStore.RTT;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfFactory;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.hp.msgs.HpRegisterMsg;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodAddress.NatType;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.network.model.common.NetworkModel;
import se.sics.gvod.network.model.king.KingLatencyMap;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class CroupierStats {
    
    private static Logger logger = LoggerFactory.getLogger(CroupierStats.class);
    // (overlayId -> (nodeId, stats))
    private static final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Stats>> snapshotMap =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Stats>>();
    private static final ConcurrentHashMap<VodAddress, Stats> nodeMap =
            new ConcurrentHashMap<VodAddress, Stats>();
    private static volatile boolean collectData = false;
    private static AtomicInteger counter = new AtomicInteger(0);
    private static final ConcurrentHashMap<Integer, Integer> shufflesSent =
            new ConcurrentHashMap<Integer, Integer>();
    private static final ConcurrentHashMap<Integer, Integer> pubshufflesRecvd =
            new ConcurrentHashMap<Integer, Integer>();
    private static final ConcurrentHashMap<Integer, Integer> privshufflesRecvd =
            new ConcurrentHashMap<Integer, Integer>();
    private static final ConcurrentHashMap<Integer, Integer> shufflesTimedout =
            new ConcurrentHashMap<Integer, Integer>();
    private static GraphUtil g = new GraphUtil();
    private final static NetworkModel networkModel = new KingLatencyMap(VodConfig.getSeed());
    private static int totalLatencyToPrivateNodes = 0;
    
    private static class DummyMessage extends RewriteableMsg {
        
        private static final long serialVersionUID = 5713578865207697848L;
        
        public DummyMessage(Address src, Address dest) {
            super(src, dest);
        }
        
        @Override
        public RewriteableMsg copy() {
            return new CroupierStats.DummyMessage(this.source, this.destination);
        }
    }
    
    private CroupierStats() {
        // hidden
    }
    
    public static Stats instance(Self self) {
        return addNode(self.getAddress());
    }
    
    public static Stats addNode(VodAddress peer) {
        int overlayId = peer.getOverlayId();
        int nodeId = peer.getId();
        
        ConcurrentHashMap<Integer, Stats> overlayStats;
        if (!snapshotMap.containsKey(overlayId)) {
            overlayStats = new ConcurrentHashMap<Integer, Stats>();
            snapshotMap.put(overlayId, overlayStats);
        } else {
            overlayStats = snapshotMap.get(overlayId);
        }
        Stats stats;
        if (!overlayStats.containsKey(nodeId)) {
            stats = new Stats(nodeId, overlayId);
            overlayStats.put(nodeId, stats);
            nodeMap.put(peer, stats);
            
        } else {
            stats = overlayStats.get(nodeId);
        }
        stats.setNatType(peer.getNatType());
        return stats;
    }
    
    public static boolean removeNode(int nodeId, int overlayId) {
        ConcurrentHashMap<Integer, Stats> overlayStats = snapshotMap.get(overlayId);
        if (overlayStats != null) {
            return (overlayStats.remove(nodeId) == null) ? false : true;
        } else {
            return false;
        }
    }
    
    private static Set<Stats> getNodes(int overlayId) {
        Set<Stats> nodes = new HashSet<Stats>();
        if (snapshotMap.get(overlayId) != null) {
            nodes.addAll(snapshotMap.get(overlayId).values());
        }
        return nodes;
    }
    
    private static int numNodes(int overlayId) {
        return getNodes(overlayId).size();
    }
    
    private static Set<Stats> getPrivateNodes(int overlayId) {
        Set<Stats> nodes = new HashSet<Stats>();
        if (snapshotMap.get(overlayId) != null) {
            for (Stats s : snapshotMap.get(overlayId).values()) {
                if (s.getNatType() == NatType.NAT) {
                    nodes.add(s);
                }
            }
        }
        return nodes;
    }
    
    private static int numPrivateNodes(int overlayId) {
        return getPrivateNodes(overlayId).size();
    }
    
    private static Set<Stats> getPublicNodes(int overlayId) {
        Set<Stats> nodes = new HashSet<Stats>();
        if (snapshotMap.get(overlayId) != null) {
            for (Stats s : snapshotMap.get(overlayId).values()) {
                if (s.getNatType() == NatType.OPEN) {
                    nodes.add(s);
                }
            }
        }
        return nodes;
    }
    
    private static int numPubNodes(int overlayId) {
        return getPublicNodes(overlayId).size();
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
            report(overlayId);
        }
        collectData = false;
    }
    
    public static boolean isCollectData() {
        return collectData;
    }

    /**
     * Returns all nodes with the same overlayId
     *
     * @param overlayId
     * @return
     */
    private static Set<Stats> getOverlayStats(int overlayId) {
        Set<Stats> nodes = new HashSet<Stats>();
        if (snapshotMap.get(overlayId) != null) {
            nodes.addAll(snapshotMap.get(overlayId).values());
        }
        return nodes;
    }
    
    private static void incTotal(Map<Integer, Integer> map, int overlayId, int count) {
        Integer total = map.get(overlayId);
        if (total == null) {
            total = 0;
            map.put(overlayId, total);
        }
        total += count;
    }
    
    private static int getNumPublicShufflesRecvd(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count += s.getPubShuffleRecvd();
        }
        incTotal(pubshufflesRecvd, overlayId, count);
        return count;
    }
    
    private static int getNumPrivateShufflesRecvd(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count += s.getPrivShuffleRecvd();
        }
        incTotal(privshufflesRecvd, overlayId, count);
        return count;
    }
    
    private static int getNumShufflesTimeouts(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count += s.getShuffleTimeout();
        }
        incTotal(shufflesTimedout, overlayId, count);
        return count;
    }
    
    private static int getNumShufflesResp(int overlayId) {
        int count = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count += s.getShuffleResp();
        }
        incTotal(shufflesSent, overlayId, count);
        return count;
    }
    
    private static int stdShufflesRecvd(int overlayId, int avg) {
        int std = 0;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (s.getNatType() == NatType.OPEN) {
                std += Math.abs(avg - s.getShufflesRecvd());
            }
        }
        return std;
    }
    
    private static int maxShufflesRecvd(int overlayId) {
        int count = -1;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            count = (s.getShufflesRecvd() > count)
                    ? s.getShufflesRecvd() : count;
        }
        return count;
    }
    
    private static Map<Integer, Integer> shufflesRecvdHistogram(int overlayId) {
        Set<Stats> nodes = getOverlayStats(overlayId);
        Map<Integer, Integer> h = new HashMap<Integer, Integer>();
        for (Stats s : nodes) {
            if (s.getNatType() == NatType.OPEN) {
                int c = s.getShufflesRecvd();
                Integer k = h.get(c);
                if (k == null) {
                    k = 1;
                } else {
                    k++;
                }
                h.put(c, k);
            }
        }
        return h;
    }
    
    private static int minShufflesRecvd(int overlayId) {
        int count = Integer.MAX_VALUE;
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            if (s.getNatType() == NatType.OPEN) {
                count = (s.getShufflesRecvd() < count)
                        ? s.getShufflesRecvd() : count;
            }
        }
        return count;
    }

//    public static void finalReport(int overlayId) {
//        // total shuffle counts
//    }
//-------------------------------------------------------------------
    public static void report(int overlayId) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("current step: ").append(counter.getAndIncrement()).
                append(", current time: ").append(System.currentTimeMillis()).
                append(", number of nodes: ").append(numNodes(overlayId));
        sb.append(reportShuffles(overlayId));
        //        sb.append(reportStaleParents(overlayId));
//        sb.append(reportNumParents(overlayId));
        sb.append(reportParentChangeEvents(overlayId));
        sb.append(reportEstimation(overlayId));
        sb.append(reportReboots(overlayId));
//        sb.append(reportParentChangeEvents(overlayId));
        sb.append(reportHitStats(overlayId));
//        sb.append(reportAvgRTT(overlayId));
//        sb.append(reportConnectionTimes(overlayId));
//        sb.append(reportOldParents(overlayId));
//        sb.append(reportPrivatePeersWithAllParents(overlayId));



//        sb.append(reportFanoutHistogram());
//        sb.append(reportFaninHistogram());
//        //str += reportDetailes();
//        sb.append("graph statistics: ").append(reportGraphStat());
//        sb.append(reportEstimation());
//        sb.append(reportNumParentSwitches());
//        sb.append("\nSwitched parents counter = ").append(counter).append( "\t").append(switchedParentsCounter);
//        sb.append("\nFailed parents counter = ").append(counter).append("\t").append(failedParentsCounter);
//        sb.append(reportNoParents());
//        sb.append("\n###\n");
//
        logger.info(sb.toString());
//                
//        oldParentsCounter = 0;
//        successParentsCounter = 0;
//        switchedParentsCounter = 0;
//        shuffleTimeoutCounter.set(0);
    }
    
    private static String reportShuffles(int overlayId) {
        StringBuilder sb = new StringBuilder();
        int pub = getNumPublicShufflesRecvd(overlayId);
        int priv = getNumPrivateShufflesRecvd(overlayId);
        int sum = pub + priv;
        int t = getNumShufflesTimeouts(overlayId);
        int recvd = getNumShufflesResp(overlayId);
        int sz = numNodes(overlayId);
        int privSz = numPrivateNodes(overlayId);
        int pubSz = numPubNodes(overlayId);
        int max = maxShufflesRecvd(overlayId);
        int min = minShufflesRecvd(overlayId);
        int std = stdShufflesRecvd(overlayId, (sum / (pubSz == 0 ? 1 : pubSz)));
        
        
        if (sz != 0 && pubSz != 0 && sz != 0 && privSz != 0) {
            sb.append("---\n");
            sb.append("Shuffles messages:\t");
            sb.append("\nTotal Stats: ").append("avg(").append((sum / pubSz)).append("), max(").append(max).
                    append("), min(").append(min).
                    append("), std(").append(std).
                    append("), timedOut(").append(t).
                    append("), avg resps(").append(recvd / sz).append(")\n");
//            sb.append(shufflesRecvdHistogram(overlayId)).append("\n");
        }
        return sb.toString();
    }

//-------------------------------------------------------------------
    private static String reportStaleParents(int overlayId) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        
        int totalNumStaleParents = 0;
        int peerCorrectParents = 0;
        int notContactableNodes = 0;
        Set<Address> believedParents;
        
        Set<Stats> nodes = getOverlayStats(overlayId);
        for (Stats s : nodes) {
            
            if (s.getNatType() == NatType.NAT) {
                believedParents = getParents(s.getNodeId());
                
                peerCorrectParents = 0;
                
                for (Address parent : believedParents) {
                    Stats peerParentInfo = nodeMap.get(ToVodAddr.hpServer(parent));
                    
                    boolean found = false;
                    if (peerParentInfo != null) {
                        for (VodAddress child : peerParentInfo.getChildren()) {
                            if (child.getId() == s.getNodeId()) {
                                found = true;
                            }
                        }
                    }
                    if (found) {
                        peerCorrectParents++;
                    } else {
                        totalNumStaleParents++;
                    }
                }
                
                if (peerCorrectParents == 0) {
                    notContactableNodes++;
                }
            }
        }
        
        sb.append("Stale parents = ").append(counter).append("\t").append(totalNumStaleParents).append("\n");
        sb.append("Not contactable nodes = ").append(counter).append("\t").append(notContactableNodes).append("\n");
        
        return sb.toString();
    }
//-------------------------------------------------------------------

    // Look at all private partners, and check to see how many of my references to 
    // those private peers contain stale parents.
    private static StringBuilder reportOldParents(int overlayId) {
        long totalLatencyToAllPrivatePartners = 0;
        
        StringBuilder sb = new StringBuilder();
        
        List<VodAddress> privatePartners;
        
        
        for (Stats peerInfo : getOverlayStats(overlayId)) {
            privatePartners = peerInfo.getPrivatePartners();
            
            if (privatePartners == null) {
                continue;
            }
            
            for (VodAddress d : privatePartners) {
                Set<Address> believedParents = d.getParents();
                Stats pi = nodeMap.get(d);
                //long latencyToPrivatePeer = ParentMaker.LATENCY_FAILED_PARENT; // MAX LATENCY
                long latencyToPrivatePeer = 100000; // MAX LATENCY

                if (pi != null) {
                    Set<Address> actualParents = getParents(pi.getNodeId());
                    
                    for (Address p : believedParents) {
                        if (actualParents.contains(p)) {
                            RewriteableMsg m = new CroupierStats.DummyMessage(
                                    d.getPeerAddress(), p);
//                            RewriteableMsg n = new CroupierStats.DummyMessage(
//                                    peerInfo., p);
                            long parentLatency = networkModel.getLatencyMs(m);
                            long latency = parentLatency; // + self to parent latency
                            if (latency < latencyToPrivatePeer) {
                                latencyToPrivatePeer = latency;
                            }
                        }
                    }
                }

                // XXX don't count empty parents
//                if (latencyToPrivatePeer == 100000 && believedParents.size() > 0) {
//                    oldParentsCounter++;
//                    totalOldParentsCounter++;
//                } 

                totalLatencyToAllPrivatePartners += latencyToPrivatePeer;
            }

            // jim: calculate connection latency to all nodes in my view!!
            peerInfo.setTotalLatencyToPrivatePartners(totalLatencyToAllPrivatePartners);
            
        }

        //sb.append("old parents counter: ").append(oldParentsCounter).append("\n");
//        sb.append("total old parents counter: ").append(totalOldParentsCounter).append("\n");
        //sb.append("Successful parents counter = ").append(counter).append("\t").append(successParentsCounter).append("\n");
        sb.append(reportConnectionTimes(overlayId));
        
        return sb;
    }
    
    private static String reportParentChangeEvents(int overlayId) {
        StringBuffer sb = new StringBuffer().append("---\n");
        sb.append(counter.get()).append(" Parent Change events (").
                append(getNodes(overlayId).size()).append(") :\n");
        int[] counts = new int[HpRegisterMsg.RegisterStatus.values().length];
        for (Stats s : getNodes(overlayId)) {
            if (s.getNatType() == NatType.NAT) {
//                ConcurrentHashMap<Address, CopyOnWriteArrayList<Integer>> pms =
//                        s.getParentStatus();
//                logger.info("PARENTS: " + s.getParents());
//                logger.info("NUM PARENTS: " + s.getParents().size());
//                for (Address parent : s.getParents()) {
//                    for (int i = 0; i < HpRegisterMsg.RegisterStatus.values().length; i++) {
//                        counts[i] += pms.get(parent).get(i);
//                    }
//                }
                int[] sc = s.parentChangeEventCounts();
                for (int i = 0; i < HpRegisterMsg.RegisterStatus.values().length; i++) {
                    counts[i] += sc[i];
                }
            }
        }
        for (int i = 0; i < HpRegisterMsg.RegisterStatus.values().length; i++) {
            sb.append(HpRegisterMsg.RegisterStatus.values()[i]).append(" = ").append(counts[i]);
            sb.append("\n");
        }
        return sb.toString();
    }

//-------------------------------------------------------------------
    private static String reportPrivatePeersWithAllParents(int overlayId) {
        String str = "---\n";
        str += "Peers with parents: ";
        str += counter + " ";
        // sizes = 0, 1, ..., n parents
        HashMap<Integer, Integer> numOfParents = new HashMap<Integer, Integer>();
        int num;
        Integer sum;
        
        for (Stats info : getNodes(overlayId)) {
            
            if (info.getNatType() == VodAddress.NatType.NAT) {
                num = getParents(info.getNodeId()).size();
                
                sum = numOfParents.get(num);
                if (sum != null) {
                    numOfParents.put(num, sum + 1);
                } else {
                    numOfParents.put(num, 1);
                }
            }
        }
        
        
        for (int i = 0; i <= VodConfig.DEFAULT_PARENT_SIZE; i++) {
            str += numOfParents.get(i) == null ? 0 : numOfParents.get(i);
            str += " ";
        }
        
        str += "\n";
        
        return str;
    }

//-------------------------------------------------------------------
    private static String reportNetworkState(int overlayId) {
        String str = new String();
        int publicNodes = 0;
        int privateNodes = 0;
        
        for (Stats pi : getOverlayStats(overlayId)) {
            if (pi.getNatType() == VodAddress.NatType.OPEN) {
                publicNodes++;
            } else {
                privateNodes++;
            }
        }
        
        str += "number of peers: " + numNodes(overlayId) + "\n";
        str += "public peers: " + publicNodes + "\n";
        str += "private peers: " + privateNodes + "\n";
        
        
        return str;
    }

//-------------------------------------------------------------------
    private static String reportConnectionTimes(int overlayId) {
        String str = new String();
//		str += "total number of peers: " + peers.size() + "\n";
        long totalLatency = 0;
        for (Stats peerInfo : getOverlayStats(overlayId)) {
            totalLatency += peerInfo.getTotalLatencyToPrivatePartners();
        }
        
        
        totalLatencyToPrivateNodes += (totalLatency / 1000);

        //str += "Sum private peer latencies = " + counter + "\t" + totalLatency + "\n";
        //str += "sum private peer latencies (s): " + totalLatency / 1000 + "\n";
        str += "total latency (s): " + totalLatencyToPrivateNodes + "\n";
        return str;
    }
    
    private static String reportReboots(int overlayId) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("Reboots: ");
        for (Stats info : getOverlayStats(overlayId)) {
            if (info.getNumReboots() != 0) {
                sb.append(info.getNumReboots()).append(", ");
            }
        }
        return sb.toString();
    }

//-------------------------------------------------------------------
    private static String reportEstimation(int overlayId) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        double sum = 0;
        double max = 0;
        double min = 1.0;
        double std = 0;
        double estimation;
        
        int bins[] = new int[100];
        int numPeersToEstimate = 0;
        for (Stats info : getOverlayStats(overlayId)) {
            if (info.getNatType() == VodAddress.NatType.NAT) {
                continue;
            }
            numPeersToEstimate++;
            double val = info.getPublicEstimation();
            sum += val;
            if (val > max) {
                max = val;
            }
            if (val < min) {
                min = val;
            }
            double offset = 0.01d;
            for (int i = 0; i < 100; i++) {
                if ((offset - 0.01d) < val && val <= offset) {
                    bins[i]++;
                }
                offset += 0.01;
            }
//            str += val + ", ";
        }
        double avg = sum / numPeersToEstimate;
        for (Stats info : getOverlayStats(overlayId)) {
            std += Math.abs(info.getPublicEstimation() - avg);
        }
        double var = 0;
        double ci = 0;
        double size = numNodes(overlayId);
        
        for (Stats info : getOverlayStats(overlayId)) {
            estimation = info.getPublicEstimation();
            var += Math.pow(estimation - avg, 2);
        }

//        ci = 2.262 * Math.sqrt(var / (size * (size - 1)));
        ci = 1.960 * Math.sqrt(var / (size * (size - 1)));
        
        sb.append("\npublic nodes estimation:\n");
        sb.append("avg: ").append(avg).append("\n");
        sb.append("std: ").append(std).append("\n");
        sb.append("max: ").append(max).append("\n");
        sb.append("min: ").append(min).append("\n");
//        sb.append("var: ").append(var).append("\n");
//        sb.append("upper confidence interval (95%) of public nodes estimation: ").append(avg + ci).append("\n");
//        sb.append("lower confidence interval (95%) of public nodes estimation: ").append(avg - ci).append("\n");

        
        return sb.toString();
    }
    
    private static String reportHitStats(int overlayId) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        double sum = 0;
        double max = 0;
        double min = 1.0;
        double std = 0;
        double estimation;
        int numPeersToEstimate = 0;
        Stats worst = null;
        for (Stats info : getOverlayStats(overlayId)) {
            numPeersToEstimate++;
            int val = info.getNumHitStats();
            sum += val;
            if (val > max) {
                max = val;
            }
            if (val < min) {
                min = val;
                worst = info;
            }
        }
        double avg = sum / numPeersToEstimate;
        for (Stats info : getOverlayStats(overlayId)) {
            std += Math.abs(info.getNumHitStats() - avg);
        }
        double var = 0;
        for (Stats info : getOverlayStats(overlayId)) {
            estimation = info.getNumHitStats();
            var += Math.pow(estimation - avg, 2);
        }
        
        
        sb.append("\nNumber of hit stats samples at nodes:\n");
        sb.append("avg: ").append(avg).append("\n");
        sb.append("std: ").append(std).append("\n");
        sb.append("max: ").append(max).append("\n");
        sb.append("min: ").append(min).append("\n");
        sb.append("var: ").append(var).append("\n");
        sb.append("Worst: ").append(worst).append("\n");
        
        return sb.toString();
    }

//-------------------------------------------------------------------
    private static String reportAvgRTT(int overlayId) {
        String str = "---\n";
        ArrayList<Long> parentsRtt = new ArrayList<Long>();
        long sum = 0;
        int count = 0;
        
        
        for (Stats info : getOverlayStats(overlayId)) {
            parentsRtt.clear();
            Set<Address> parents = getParents(info.getNodeId());
            
            for (Address p : parents) {
                RTT rtt = RTTStore.getRtt(info.getNodeId(),
                        ToVodAddr.systemAddr(p));
                if (rtt != null) {
                    count++;
                    sum += rtt.getRTO();
                }
            }
        }
        
        if (count == 0) {
            str += "avg parent rtt = " + counter + " " + Integer.MAX_VALUE + "\n";
        } else {
            str += "avg parent rtt = " + counter + " " + sum / count + "\n";
        }
        
        return str;
    }

//-------------------------------------------------------------------
    private static String reportRandomness(int overlayId) {
        String str = "---\n";
        HashMap<Integer, Integer> randomness = new HashMap<Integer, Integer>();
        
        int selectedTimes;
        Integer count;
        for (Stats info : getOverlayStats(overlayId)) {
            selectedTimes = info.getSelectedTimes() / 10;
            count = randomness.get(selectedTimes);
            
            if (count == null) {
                randomness.put(selectedTimes, 1);
            } else {
                randomness.put(selectedTimes, count + 1);
            }
        }
        
        str += "global randomness: " + randomness.toString() + "\n";
        
        return str;
    }

//-------------------------------------------------------------------
    private static String reportFanoutHistogram(int overlayId) {
        HashMap<Integer, Integer> fanoutHistogram = new HashMap<Integer, Integer>();
        String str = "---\n";
        
        Integer n;
        for (Stats s : getOverlayStats(overlayId)) {
            Integer num = s.getFanout();
            n = fanoutHistogram.get(num);
            
            if (n == null) {
                fanoutHistogram.put(num, 1);
            } else {
                fanoutHistogram.put(num, n + 1);
            }
        }
        
        str += "out-degree: " + fanoutHistogram.toString() + "\n";
        
        return str;
    }

//-------------------------------------------------------------------
//    private static String reportFaninHistogram(int overlayId) {
//        HashMap<Integer, Integer> faninHistogram = new HashMap<Integer, Integer>();
//        String str = "---\n";
//
//        int count;
//        for (VodAddress node : fanin.keySet()) {
//            count = 0;
//            for (Stats peerInfo : getOverlayStats(overlayId)) {
//                if (peerInfo.getAllPartners() != null && peerInfo.isPartner(node)) {
//                    count++;
//                }
//            }
//
//            fanin.put(node, count);
//        }
//
//        Integer n;
//
//        for (Integer num : fanin.values()) {
//            n = faninHistogram.get(num);
//
//            if (n == null) {
//                faninHistogram.put(num, 1);
//            } else {
//                faninHistogram.put(num, n + 1);
//            }
//        }
//
//        str += "in-degree: " + faninHistogram.toString() + "\n";
//
//        return str;
//    }
//-------------------------------------------------------------------
    private static String reportGraphStat(int overlayId) {
        String str = "---\n";
        double id, od, cc, pl, istd;
        int diameter;
        
        g.init(nodeMap);
        id = g.getMeanInDegree();
        istd = g.getInDegreeStdDev();
        od = g.getMeanOutDegree();
        cc = g.getMeanClusteringCoefficient();
        pl = g.getMeanPathLength();
        diameter = g.getDiameter();
        
        str += "Diameter: " + diameter + "\n";
        str += "Average path length: " + String.format("%.4f", pl) + "\n";
        str += "Clustering-coefficient: " + String.format("%.4f", cc) + "\n";
        str += "Average in-degree: " + String.format("%.4f", id) + "\n";
        str += "In-degree standard deviation: " + String.format("%.4f", istd) + "\n";
        str += "Average out-degree: " + String.format("%.4f", od) + "\n";
        
        return str;
    }

//-------------------------------------------------------------------
//    public static String reportNumParentSwitches(int overlayId) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("");
//        int totalSwitches = 0;
//
//        for (Integer v : parentSwitches.values()) {
//            totalSwitches += v;
//        }
//
//        sb.append("total parent switches: ").append(totalSwitches).append("\n");
//
//        return sb.toString();
//    }
    private static Set<Address> getParents(int nodeId) {
        Set<Address> parents = SelfFactory.parents.get(nodeId);
        Set p = new HashSet<Address>();
        if (parents != null) {
            p.addAll(parents);
        }
        return p;
    }
    
//    public static String reportNumParents(int overlayId) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("---\n");
//        int[] numParents = new int[VodConfig.PARENT_SIZE + 2];
//        
//        List<VodAddress> danglingChildren = new ArrayList<VodAddress>();
//        for (Integer nodeId : SelfFactory.parents.keySet()) {
//            Set<Address> parents = SelfFactory.parents.get(nodeId);
//            numParents[parents.size()]++;
//            if (parents.isEmpty()) {
//                danglingChildren.add(new VodAddress(
//                        new Address(VodConfig.getIp(),
//                        VodConfig.getPort(), nodeId),
//                        Croupier.SYSTEM_CROUPIER_OVERLAY_ID,
//                        VodConfig.getNatPolicy(),
//                        SelfFactory.parents.get(nodeId)));
//            }
//        }
//
////        sb.append("Dangling children: ").append(danglingChildren.size()).append("\n");
////        for (VodAddress a : danglingChildren) {
////            Stats pi = nodeMap.get(a);
////            if (pi == null) {
////                sb.append("Null CroupierPeerInfo: ").append(a).append("\n");
////            } else {
////                sb.append("(").append(a.getId()).append("):").append(pi).append("\n");
////            }
////        }
//
//        for (int i = 0; i < numParents.length; i++) {
//            sb.append("Number of nodes with ").append(i).append(" parents: ").append(numParents[i]).append("\n");
//        }
//        
//        return sb.toString();
//    }
}
