/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common;

import java.net.InetAddress;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author jdowling
 */
public abstract class SelfBase implements Self {

    protected final InetAddress ip;
    protected final int nodeId;
    protected final int port;
    protected final Address addr;
    
    public SelfBase(Nat nat, InetAddress ip, int port, int nodeId, int overlayId) {
        if (nat != null)  {
            SelfFactory.setNat(nodeId, nat);
        }
        this.ip = ip;
        this.port = port;
        this.nodeId = nodeId;
        this.addr = new Address(ip, port, nodeId);
        SelfFactory.setOverlayId(overlayId);
    }
    
    @Override
    public HPMechanism getHpMechanism(VodAddress dest) {
        return getAddress().getHpMechanism(dest);
    }

    @Override
    public boolean isPacingReqd() {
        Nat n = getNat();
        if (n == null) {
            return false;
        }
        if (n.getAllocationPolicy() == Nat.AllocationPolicy.PORT_CONTIGUITY && (
                n.getMappingPolicy() != Nat.MappingPolicy.ENDPOINT_INDEPENDENT ||
                n.getFilteringPolicy()!= Nat.FilteringPolicy.ENDPOINT_INDEPENDENT 
                )) {
            return true;
        }
        
        return false;
    }

    @Override
    public boolean isOpen() {
        return getNat().isOpen();
    }


    @Override
    public final int getOverlayId() {
        return SelfFactory.getOverlayId();
    }
    /**
     * Sets the overlayId for this node (self).
     * @param overlayId
     */
    @Override
    public final void setOverlayId(int overlayId) {
        SelfFactory.setOverlayId(overlayId);
    }

    @Override
    public final InetAddress getIp() {
       return ip;
    }

    @Override
    public final int getId() {
        return nodeId;
    }
    
    @Override
    public final int getPort() {
        return port;
    }
    
}
