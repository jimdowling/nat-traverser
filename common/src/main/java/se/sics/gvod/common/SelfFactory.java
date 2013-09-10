/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Nat;

/**
 * This includes support for simulation. 
 * Each nodeId has their own set of parents and its own NAT.
 * In production, there would be only a single nodeId
 */
public class SelfFactory {

    private static Logger logger = LoggerFactory.getLogger(SelfFactory.class);
    /**
     * <nodeId, set-of-parents>
     */
    public static ConcurrentHashMap<Integer, Set<Address>> parents =
            new ConcurrentHashMap<Integer, Set<Address>>();
    /**
     * <nodeId, natType>
     */
    private static ConcurrentHashMap<Integer, Nat> nats = new ConcurrentHashMap<Integer, Nat>();

    private static AtomicInteger overlayId = new AtomicInteger();
    
    
    public static synchronized Set<Address> getParents(int nodeId) {
        Set<Address> myParents = new HashSet<Address>();
        if (parents.containsKey(nodeId)) {
            myParents.addAll(parents.get(nodeId));
        }
        return myParents;
    }

    /**
     *
     * @param nodeId for the node with this id, add a parent to all its
     * overlayPeer addresses. Replace all the old GVodNodeAddresses
     * @param newParent address of new parent
     */
    static void addParent(int nodeId, Address newParent) {

        Set<Address> myParents = parents.get(nodeId);
        if (myParents == null) {
            myParents = new HashSet<Address>();
            parents.put(nodeId, myParents);
        }
        myParents.add(newParent);
        parents.put(nodeId, myParents);
    }

    static boolean removeParent(int nodeId, Address oldParent) {

        Set<Address> myParents = parents.get(nodeId);
        if (myParents == null) {
            logger.error("Tried to remove a parent from a non-existant set of parents: " + nodeId);
            return false;
        }
        return myParents.remove(oldParent);
    }

    public static synchronized VodAddress getSystemNodeAddress(int nodeId) {
        return new VodAddress(
                new Address(VodConfig.getIp(),
                VodConfig.getPort(), VodConfig.getNodeId()),
                VodConfig.SYSTEM_OVERLAY_ID,
                VodConfig.getNatPolicy(),
                parents.get(nodeId));
    }

    static void setNat(int nodeId, Nat nat) {
        nats.put(nodeId, nat);
    }

    static Nat getNat(int nodeId) {
        if (nats.containsKey(nodeId) == false) {
            return new Nat(Nat.Type.NAT, Nat.MappingPolicy.PORT_DEPENDENT,
                    Nat.AllocationPolicy.RANDOM, Nat.FilteringPolicy.PORT_DEPENDENT,
                    0, Nat.DEFAULT_RULE_EXPIRATION_TIME);
        }
        // return a reference, as the object should be immutable (with the 
        // exception of bindingTimeout that is anyways, not updated).
        return nats.get(nodeId);
    }
    
    static void setOverlayId(int overlayId) {
        SelfFactory.overlayId.set(overlayId);
    }
    
    static int getOverlayId() {
        return SelfFactory.overlayId.get();
    }
}
