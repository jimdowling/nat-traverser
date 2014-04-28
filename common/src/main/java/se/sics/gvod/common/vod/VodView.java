/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.vod;

import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.UtilityVod;

/**
 * This class is needed for simulation. 
 * This class keeps {@link GVodNodeDescriptor}s of all existing peers of this node.
 * @author jim, jim
 */
public class VodView {

    private static final Logger logger = LoggerFactory.getLogger(VodView.class);
    // For details on the performance of ConcurrentHashMap vs HashMap, see
    // http://www.informit.com/guides/content.aspx?g=java&seqNum=246
    // http://gregluck.com/blog/archives/2009/06/performance-problems-in-concurrenthashmap-vs-synchronized-hashmap/
    // For our small scales, I think ConcurrentHashMap should be fine.
    // I don't expect more than hundreds/thousands of entries per map
    private static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, VodDescriptor>> peerDescriptorMapping = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, VodDescriptor>>();
    private static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UtilityVod>> utilityMapping = 
            new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UtilityVod>>();

    /**
     * 
     * @param overlayId
     * @return 
     */
    public static VodDescriptor getPeerDescriptor(Self self) {
        int nodeId = self.getId();
        int overlayId = self.getOverlayId();
        ConcurrentHashMap<Integer, VodDescriptor> peerDescriptors = getPeerDescriptors(nodeId);
        return peerDescriptors.get(overlayId);
    }

    /**
     * 
     * @param overlayId
     * @param descriptor 
     */
    public static void addOrUpdatePeerDescriptor(Self self, VodDescriptor descriptor) {
        int nodeId = self.getId();
        int overlayId = self.getOverlayId();
        ConcurrentHashMap<Integer, VodDescriptor> peerDescriptors = getPeerDescriptors(nodeId);
        peerDescriptors.put(overlayId, descriptor);
    }

    private static ConcurrentHashMap<Integer, VodDescriptor> getPeerDescriptors(int nodeId) {
        ConcurrentHashMap<Integer, VodDescriptor> peerDescriptors = peerDescriptorMapping.get(nodeId);
        if (peerDescriptors == null) {
            peerDescriptors = new ConcurrentHashMap<Integer, VodDescriptor>();
        }
        return peerDescriptors;
    }

    /**
     * 
     * @param overlayId
     * @param utility 
     */
    public static void updateUtility(Self self, UtilityVod utility) {
        int nodeId = self.getId();
        int overlayId = self.getOverlayId();
        // get the utility to create it if it doesnt already exist.
        getPeerUtility(self);
        utilityMapping.get(nodeId).put(overlayId, utility);
    }

    /**
     * 
     * @param overlayId
     * @param utility 
     */
    public static void updateUtilityChunk(Self self, int chunk) {
        UtilityVod u = getPeerUtility(self);
        u.setChunk(chunk);
    }

    public static void updateUtilityPiece(Self self, int piece) {
        UtilityVod u = getPeerUtility(self);
        u.setPiece(piece);
    }

    /**
     * 
     * @param overlayId 
     */
    public static void removePeerDescriptor(Self self) {
        int nodeId = self.getId();
        int overlayId = self.getOverlayId();
        ConcurrentHashMap<Integer, VodDescriptor> peerDescriptors = getPeerDescriptors(nodeId);
        peerDescriptors.remove(overlayId);
    }

    public static UtilityVod getPeerUtility(Self self) {
        int nodeId = self.getId();
        int overlayId = self.getOverlayId();
        ConcurrentHashMap<Integer, UtilityVod> peerDescriptors = utilityMapping.get(nodeId);
        if (peerDescriptors == null) {
            peerDescriptors = new ConcurrentHashMap<Integer, UtilityVod>();
            utilityMapping.put(nodeId, peerDescriptors);
        }
        UtilityVod utility = null;
        if (peerDescriptors.containsKey(overlayId)) {
            utility = peerDescriptors.get(overlayId);
        } else {
//            throw new IllegalStateException("Peer utility not set for " +
//                    nodeId + ":" + overlayId);
            utility = new UtilityVod(0);
            peerDescriptors.put(overlayId, utility);
        }
        return utility;
    }
}
