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
    protected int overlayId;
    protected final int port;
    protected final Address addr;
    
    public SelfBase(Nat nat, InetAddress ip, int port, int nodeId, int overlayId) {
        if (nat != null)  {
            SelfFactory.setNat(nodeId, nat);
        }
        this.ip = ip;
        this.port = port;
        this.nodeId = nodeId;
        this.overlayId = overlayId;
        this.addr = new Address(ip, port, nodeId);
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

    public void setOverlayId(int overlayId) {
        this.overlayId = overlayId;
    }
}
