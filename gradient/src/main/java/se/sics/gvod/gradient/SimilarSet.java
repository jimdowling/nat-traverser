/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.Utility;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author Jim
 */
public class SimilarSet {

    private final int DEFAULT_MAX_SIZE = 10;
    private int maxSize = DEFAULT_MAX_SIZE;
    private Set<VodDescriptor> entries = new HashSet<VodDescriptor>();
    private Map<VodAddress, VodDescriptor> d2e = new HashMap<VodAddress, VodDescriptor>();
    private Self self;
    private Random r;
    private final double temperature;

    public static class ComparatorUtility implements Comparator<VodDescriptor> {

        Utility utility;

        public ComparatorUtility(Utility selfUtility) {
            this.utility = selfUtility;
        }

        @Override
        public int compare(VodDescriptor o1, VodDescriptor o2) {
            
            if (o1.getUtility().getValue() == o2.getUtility().getValue()) {
                if (o1.getAge() < o2.getAge()) {
                    return -1;
                } else if (o2.getAge() < o1.getAge()) {
                    return 1;
                } else {
                    return 0;
                }
            } else if (o1.getUtility().getValue() < utility.getValue()
                    && o2.getUtility().getValue() > utility.getValue()) {
                return 1;
            } else if (o2.getUtility().getValue() < utility.getValue()
                    && o1.getUtility().getValue() > utility.getValue()) {
                return -1;
            } else if (Math.abs(o1.getUtility().getValue() - utility.getValue())
                    < Math.abs(o2.getUtility().getValue() - utility.getValue())) {
                return -1;
            } else {
                return 1;
            }
        }
    }
//    private class ComparatorDistance implements Comparator<VodDescriptor> {
//
//        UtilityVod utility;
//
//        public ComparatorDistance(UtilityVod selfUtility) {
//            this.utility = selfUtility;
//        }
//
//        @Override
//        public int compare(VodDescriptor o1, VodDescriptor o2) {
//            
//            if (o1.getUtility().getPiece() == o2.getUtility().getPiece()) {
//                if (o1.getAge() < o2.getAge()) {
//                    return -1;
//                } else if (o2.getAge() < o1.getAge()) {
//                    return 1;
//                } else {
//                    return 0;
//                }
//            } else if (o1.getUtility().getChunk() < 0
//                    && o2.getUtility().getPiece() > utility.getPiece()) {
//                return 1;
//            } else if (o2.getUtility().getChunk() < 0
//                    && o1.getUtility().getPiece() > utility.getPiece()) {
//                return -1;
//            } else if (o1.getUtility().getPiece() > utility.getPiece()
//                    && o2.getUtility().getPiece() < utility.getPiece()) {
//                return -1;
//            } else if (o1.getUtility().getPiece() < utility.getPiece()
//                    && o2.getUtility().getPiece() > utility.getPiece()) {
//                return 1;
//            } else if (Math.abs(o1.getUtility().getPiece() - utility.getPiece())
//                    < Math.abs(o2.getUtility().getPiece() - utility.getPiece())) {
//                return -1;
//            } else {
//                return 1;
//            }
//        }
//    }

    private class ComparatorMaxUtility implements Comparator<VodDescriptor> {

        public ComparatorMaxUtility() {
        }

        @Override
        public int compare(VodDescriptor o1, VodDescriptor o2) {

            if (o1.getUtility().getValue() == o2.getUtility().getValue()) {
                if (o1.getAge() < o2.getAge()) {
                    return 1;
                } else if (o2.getAge() < o1.getAge()) {
                    return -1;
                } else {
                    return 0;
                }
            } else if (o1.getUtility().getValue() < o2.getUtility().getValue()) {
                return 1;
            }
            return -1;
        }
    }

    public SimilarSet(Self self, int maxSize, double temperature) {
        this.self = self;
        this.maxSize = maxSize;
        r = new Random(VodConfig.getSeed());
        this.temperature = temperature;
    }

    public boolean isFull() {
        if (d2e.size() >= maxSize && maxSize > 0) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized void incrementDescriptorAges() {
        for (VodDescriptor entry : entries) {
            entry.incrementAndGetAge();
        }
    }

    public List<VodDescriptor> addSimilarPeers(List<VodDescriptor> peers) {

        for (VodDescriptor desc : peers) {
            // don't add self to list of peers
            if (!desc.equals(self.getDescriptor())) {
                // check if the age of the new reference is newer than the old references
                VodDescriptor existingDesc = d2e.get(desc.getVodAddress());
                if (existingDesc != null) {
                    // Entry already exists, update if entry is newer
                    if (desc.getAge() < existingDesc.getAge()) {
                        existingDesc.setUtility(desc.getUtility());
                        existingDesc.setAge(desc.getAge());
                        if (!existingDesc.getVodAddress().isOpen()) {
                            VodAddress addr = existingDesc.getVodAddress();
                            addr.replaceParents(desc.getVodAddress().getParents());
                        }
                    }
                } else {
                    entries.add(desc);
                    d2e.put(desc.getVodAddress(), desc);
                }
            }

        }

        List<VodDescriptor> tempEntries =
                new ArrayList<VodDescriptor>(entries);
        List<VodDescriptor> toBeRemoved = new ArrayList<VodDescriptor>();

        //Sort and remove worst peers if it exceeds the max size.
        if (tempEntries.size() > maxSize) {
            int numToRemove = tempEntries.size() - maxSize;
            //Sorted from best to worst
            Collections.sort(tempEntries, new ComparatorUtility(self.getUtility()));
            for (int i = 0; i < numToRemove; i++) {
                toBeRemoved.add(tempEntries.remove(tempEntries.size() - 1));
            }
        }

        for (VodDescriptor desc : toBeRemoved) {
            removePeer(desc);
        }
        return toBeRemoved;
    }

    private void removePeer(VodDescriptor nodeDescriptor) {
        entries.remove(nodeDescriptor);
        d2e.remove(nodeDescriptor.getVodAddress());
    }

    public boolean removeNeighbour(VodAddress nodeAddr) {
        VodDescriptor d = d2e.get(nodeAddr);
        if (d == null) {
            return false;
        }
        entries.remove(d);
        d2e.remove(nodeAddr);
        return true;
    }

    /**
     * Returns the most similar peers to itself.
     * @param numberOfPeers
     * @return 
     */
    public List<VodDescriptor> getSimilarPeers(int numberOfPeers) {
        return getSimilarPeers(numberOfPeers, self.getUtility());
    }

    public List<VodDescriptor> getSimilarPeers(int numberOfPeers, Utility targetUtility) {
        if (entries.isEmpty()) {
            return new ArrayList<VodDescriptor>();
        }

        List<VodDescriptor> tempEntries = new ArrayList<VodDescriptor>(entries);
        if (numberOfPeers >= tempEntries.size()) {
            return tempEntries;
        }
        ArrayList<VodDescriptor> bestPeers = new ArrayList<VodDescriptor>();
        //Sort from best to worst
        Collections.sort(tempEntries, new ComparatorUtility(targetUtility));
        for (int i = 0; i < numberOfPeers; i++) {
            bestPeers.add(tempEntries.get(i));
        }

        return bestPeers;
    }

    public VodDescriptor getHighestUtilityPeer() {
        List<VodDescriptor> allNodes = new ArrayList<VodDescriptor>();
        allNodes.addAll(entries);

        Collections.sort(allNodes, new ComparatorMaxUtility());
        if (allNodes.isEmpty()) {
            return null;
        }
        return allNodes.get(0);
    }

    public List<VodDescriptor> getAllSimilarPeers() {
        ArrayList<VodDescriptor> tempEntries =
                new ArrayList<VodDescriptor>(entries);
        return tempEntries;
    }

    public VodAddress getBestSimilarPeerAddress() {
        VodDescriptor best = getBestSimilarNodeDescriptor(self.getUtility());
        if (best != null) {
            return best.getVodAddress();
        }
        return null;
    }

//    public int weightedFactorial( int n )
//    {
//        if( n <= 1 )     // base case
//            return 1;
//        else
//            return n * factorial( n - 1 );
//    }
    public VodAddress getSoftMaxPeerAddress() {
        if (entries.isEmpty()) {
            return null;
        }
        List<VodDescriptor> tempEntries =
                new ArrayList<VodDescriptor>(entries);
        Collections.sort(tempEntries, new ComparatorUtility(self.getUtility()));

        double rnd = r.nextDouble();
        double total = 0.0d;
        double[] values = new double[tempEntries.size()];
        int j = tempEntries.size() + 1;
        for (int i = 0; i < tempEntries.size(); i++) {
            // get inverse of values - lowest have highest value.
            double val = j;
            j--;
            values[i] = Math.exp(val / temperature);
            total += values[i];
        }

        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                values[i] += values[i - 1];
            }
            // normalise the probability
            double normalisedReward = values[i] / total;
            if (normalisedReward >= rnd) {
                return tempEntries.get(i).getVodAddress();
            }
        }
        return tempEntries.get(tempEntries.size() - 1).getVodAddress();
    }

    private VodDescriptor getBestSimilarNodeDescriptor(Utility newUtility) {
        if (entries.isEmpty()) {
            return null;
        }
        List<VodDescriptor> tempEntries =
                new ArrayList<VodDescriptor>(entries);
        Collections.sort(tempEntries, new ComparatorUtility(newUtility));
        return tempEntries.get(0);
    }

    protected VodDescriptor getWorstUtilityNode() {
        if (entries.isEmpty()) {
            return null;
        }
        List<VodDescriptor> tempEntries = new ArrayList<VodDescriptor>(entries);
        Collections.sort(tempEntries, new ComparatorUtility(self.getUtility()));
        return tempEntries.get(tempEntries.size() - 1);
    }

//    public boolean isSimilarSetValid(Utility newUtility, int threshold) {
//        VodDescriptor bestNode = getBestSimilarNodeDescriptor(newUtility);
//        if (bestNode.getUtility().notInBittorrentSet(newUtility)) {
//            return false;
//        }
//        return true;
//    }
}
