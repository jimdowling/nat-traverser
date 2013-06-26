package se.sics.gvod.croupier.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import se.sics.gvod.address.Address;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.hp.msgs.HpRegisterMsg;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodAddress.NatType;

public class Stats {

    private final int nodeId, overlayId;
    private volatile int selectedTimes = 0;
    private volatile VodAddress.NatType natType;
    private List<VodAddress> publicPartners = new CopyOnWriteArrayList<VodAddress>();
    private List<VodAddress> privatePartners = new CopyOnWriteArrayList<VodAddress>();
    private final ConcurrentHashMap<Address, CopyOnWriteArrayList<Integer>> parentStatus =
            new ConcurrentHashMap<Address, CopyOnWriteArrayList<Integer>>();
    private List<VodAddress> children = new ArrayList<VodAddress>();
    private volatile double publicEstimation;
    private volatile long totalLatencyToPrivatePartners;
    private volatile int publicSize, privateSize, numReboots = 0, numHitStats;
    private volatile int shuffleTimeout, numPrivShufflesRecvd, numPubShufflesRecvd, numShufflesResp;

//-------------------------------------------------------------------
    public Stats(int nodeId, int overlayId) {
        this.nodeId = nodeId;
        this.overlayId = overlayId;
    }

    public int[] parentChangeEventCounts() {
//        System.err.println(overlayId + " Stats Num parents 1: " + parentStatus.keySet().size());
        int[] counts = new int[HpRegisterMsg.RegisterStatus.values().length];
        for (Address parent : parentStatus.keySet()) {
            for (int i = 0; i < HpRegisterMsg.RegisterStatus.values().length; i++) {
                counts[i] += parentStatus.get(parent).get(i);
            }
        }
        return counts;
    }

    public void parentChangeEvent(Address address, HpRegisterMsg.RegisterStatus status) {
        CopyOnWriteArrayList<Integer> changeEvts = parentStatus.get(address);
//        System.err.println(overlayId + " Stats Num parents 2: " + parentStatus.keySet().size());
        if (changeEvts == null) {
            changeEvts = new CopyOnWriteArrayList<Integer>();
            for (int i = 0; i < HpRegisterMsg.RegisterStatus.values().length; i++) {
                changeEvts.add(i, 0);
            }
            // add the list to the hashMap at the end of the function, as it is
            // a concurrent hashmap
            parentStatus.put(address, changeEvts);
        }
        Integer n = changeEvts.get(status.ordinal());
        n++;
        changeEvts.remove(status.ordinal());
        changeEvts.add(status.ordinal(), n);
    }

    public ConcurrentHashMap<Address, CopyOnWriteArrayList<Integer>> getParentStatus() {
        return parentStatus;
    }

    public Set<Address> getParents() {
        return parentStatus.keySet();
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getOverlayId() {
        return overlayId;
    }

    public void setNatType(NatType peerType) {
        this.natType = peerType;
    }

    public void incNumReboots() {
        numReboots++;
    }

    public int getNumReboots() {
        return numReboots;
    }

//-------------------------------------------------------------------
    public void updatePublicEstimation(double estimation) {
        this.publicEstimation = estimation;
    }

//-------------------------------------------------------------------
    public void updatePartners(List<VodAddress> publicPartners,
            List<VodAddress> privatePartners, double estimation) {
        this.publicPartners.clear();
        this.publicPartners.addAll(publicPartners);
        this.privatePartners.clear();
        this.privatePartners.addAll(privatePartners);

        if (publicEstimation == 0) {
            publicSize = VodConfig.CROUPIER__VIEW_SIZE / 2;
        } else {
            if (publicEstimation > 1.0d || publicEstimation < 0.0d) {
                publicSize = VodConfig.CROUPIER__VIEW_SIZE / 2;
            } else {
                publicSize = (int) Math.round(VodConfig.CROUPIER__VIEW_SIZE * publicEstimation);
            }
        }
        this.publicEstimation = estimation;

        privateSize = VodConfig.CROUPIER__VIEW_SIZE - publicSize;


    }

//-------------------------------------------------------------------
    public void addChild(VodAddress child) {
        this.children.add(child);
    }

//-------------------------------------------------------------------
    public void removeChild(VodAddress child) {
        this.children.remove(child);
    }

//-------------------------------------------------------------------
    public void setTotalLatencyToPrivatePartners(long totalLatencyToPrivatePartners) {
        this.totalLatencyToPrivatePartners = totalLatencyToPrivatePartners / 1000;
    }

    public void setNumHitStats(int numHitStats) {
        this.numHitStats = numHitStats;
    }

    public int getNumHitStats() {
        return numHitStats;
    }

//-------------------------------------------------------------------
    public long getTotalLatencyToPrivatePartners() {
        return totalLatencyToPrivatePartners;
    }

//-------------------------------------------------------------------
//    public int updateParents(ConcurrentHashMap<Address, Integer> parents) {
//        this.parents.keySet().removeAll(parents.keySet());
//        this.danglingParents.putAll(this.parents);
//        int diff = this.parents.size();
//        this.parents.clear();
//        this.parents.putAll(parents);
//        return this.parents.size() - diff;
//    }
//-------------------------------------------------------------------
//    public ConcurrentHashMap<Address, Integer> getParents() {
//        return this.parents;
//    }
//-------------------------------------------------------------------
//    public Map<Address, Integer> getAllParents() {
//        Map<Address, Integer> allParents = new HashMap<Address, Integer>();
//        allParents.putAll(danglingParents);
//        allParents.putAll(parents);
//        return allParents;
//    }
//-------------------------------------------------------------------
    public List<VodAddress> getAllPartners() {
        List<VodAddress> allPartners = new ArrayList<VodAddress>();
        if (publicPartners.size() > publicSize) {
            allPartners.addAll(publicPartners.subList(0, publicSize));
        } else {
            allPartners.addAll(publicPartners);
        }

        if (privatePartners.size() > privateSize) {
            allPartners.addAll(privatePartners.subList(0, privateSize));
        } else {
            allPartners.addAll(privatePartners);
        }
        return allPartners;
    }

//-------------------------------------------------------------------
    public List<VodAddress> getPublicPartners() {
        return this.publicPartners;
    }

//-------------------------------------------------------------------
    public List<VodAddress> getPrivatePartners() {
        return this.privatePartners;
    }

//-------------------------------------------------------------------
    public List<VodAddress> getChildren() {
        return this.children;
    }

//-------------------------------------------------------------------
    public void incSelectedTimes() {
        this.selectedTimes++;
    }

//-------------------------------------------------------------------
    public int getSelectedTimes() {
        return this.selectedTimes;
    }

    public void incShuffleResp() {
        this.numShufflesResp++;
    }

    public void resetShuffleResp() {
        this.numShufflesResp = 0;
    }

    public int getShuffleResp() {
        return this.numShufflesResp;
    }

    public void incShuffleRecvd(VodAddress src) {
        if (src.isOpen()) {
            this.numPubShufflesRecvd++;
        } else {
            this.numPrivShufflesRecvd++;
        }
    }

    public void resetPubShuffleRecvd() {
        this.numPubShufflesRecvd = 0;
    }

    public int getPubShuffleRecvd() {
        return this.numPubShufflesRecvd;
    }

    public void resetPrivShuffleRecvd() {
        this.numPrivShufflesRecvd = 0;
    }

    public int getPrivShuffleRecvd() {
        return this.numPrivShufflesRecvd;
    }

    public void incShuffleTimeout() {
        this.shuffleTimeout++;
    }

    public void resetShuffleTimeout() {
        this.shuffleTimeout = 0;
    }

    public int getShuffleTimeout() {
        return this.shuffleTimeout;
    }

    public int getShufflesRecvd() {
        return this.getPrivShuffleRecvd() + getPubShuffleRecvd();
    }

//-------------------------------------------------------------------
    public VodAddress.NatType getNatType() {
        return this.natType;
    }

//-------------------------------------------------------------------
    public double getPublicEstimation() {
        return this.publicEstimation;
    }

//-------------------------------------------------------------------
    public boolean isPartner(VodAddress peer) {
        for (VodAddress desc : getAllPartners()) {
            if (desc.equals(peer)) {
                return true;
            }
        }

        return false;
    }

    public int getFanout() {
        return publicPartners.size() + privatePartners.size();
    }

//    public void removeDanglingParent(Address parent) {
//        danglingParents.remove(parent);
//    }
    @Override
    public String toString() {
        return "Peer : nat(" + this.natType + ") - pubSize(" + (publicPartners == null ? 0
                : this.publicPartners.size())
                + "), privateSize(" + (privatePartners == null ? 0 : this.privatePartners.size())
                + "), parents(" + parentStatus.keySet().size()
                //                + "), danglingParents(" 
                //                + (danglingParents == null ? 0 : danglingParents.size())
                + "), numReboots(" + numReboots + ")";
    }
}
