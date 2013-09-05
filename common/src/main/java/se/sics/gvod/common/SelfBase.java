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

    protected final int nodeId;
    protected int overlayId;
    protected final int port;
    
    public SelfBase(Nat nat, InetAddress ip, int port, int nodeId, int overlayId) {
        if (nat != null)  {
            SelfFactory.setNat(nodeId, nat);
        }
        this.port = port;
        this.nodeId = nodeId;
        this.overlayId = overlayId;
        setIp(ip);
    }
    
    @Override
    public final HPMechanism getHpMechanism(VodAddress dest) {
        return getAddress().getHpMechanism(dest);
    }

    @Override
    public final boolean isPacingReqd() {
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
    public final boolean isOpen() {
        return getNat().isOpen();
    }    

    @Override
    public final boolean isUpnp() {
        return SelfFactory.isUpnpEnabled(getId());
    }

    @Override
    public final void setUpnp(boolean enabled) {
        SelfFactory.setUpnp(getId(), enabled);
    }

    
    
    @Override
    public final InetAddress getIp() {
        return isUpnp() == true ? SelfFactory.getUpnpIp(getId()) 
                : SelfFactory.getIp(getId());
    }

    protected Address getAddr() {
        return new Address(getIp(), port, nodeId);
    }
    
    /**
     * Gets the self address with the current set of parents.
     * @return 
     */
    @Override
    public final VodAddress getAddress() {
        return new VodAddress(getAddr(), overlayId, 
                SelfFactory.getNat(nodeId), SelfFactory.getParents(nodeId));
    }

    @Override
    public final int getId() {
        return nodeId;
    }

    @Override
    public final int getOverlayId() {
        return overlayId;
    }

    @Override
    public final int getPort() {
        return port;
    }
    

    @Override
    public final Nat getNat() {
        return SelfFactory.getNat(nodeId);
    }

    @Override
    public final void setNat(Nat nat) {
        assert(nat != null);
        SelfFactory.setNat(nodeId, nat);
    }

    @Override
    public final void setIp(InetAddress ip) {
        SelfFactory.setIp(getId(), ip);
    }
    
    @Override
    public final void setUpnpIp(InetAddress ip) {
        SelfFactory.setUpnpIp(getId(), ip);
    }

}
