/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common;

import java.net.InetAddress;
import java.util.Set;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Nat;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.vod.VodView;


/**
 * 
 * @author jim
 */
public class SelfImpl extends SelfBase
{

    public SelfImpl(VodAddress addr) {
        this(addr.getNat(), addr.getIp(), addr.getPort(), addr.getId(), addr.getOverlayId());
    }
    
    
    public SelfImpl(Nat nat, InetAddress ip, int port, int nodeId, int overlayId) {
        super(nat, ip, port, nodeId, overlayId);
    }
    
    /**
     * Gets the self address with the current set of parents.
     * @return 
     */
    @Override
    public VodAddress getAddress() {
        return new VodAddress(addr, overlayId, 
                SelfFactory.getNat(nodeId), SelfFactory.getParents(nodeId));
    }

    @Override
    public int getId() {
        return nodeId;
    }

    @Override
    public int getOverlayId() {
        return overlayId;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public InetAddress getIp() {
        return ip;
    }
    

    @Override
    public Nat getNat() {
        return SelfFactory.getNat(nodeId);
    }

    @Override
    public void setNat(Nat nat) {
        assert(nat != null);
        SelfFactory.setNat(nodeId, nat);
    }
    
    
    @Override
    public Utility getUtility() {
        return VodView.getPeerUtility(this);
    }

    @Override
    public void updateUtility(Utility utility) {
        VodView.updateUtility(this, (UtilityVod) utility);
    }

    @Override
    public VodDescriptor getDescriptor() {
        int age = 0;
        return new VodDescriptor(getAddress(), VodView.getPeerUtility(this), 
                age, VodConfig.LB_MTU_MEASURED);
    }

    @Override
    public Set<Address> getParents() {
        return SelfFactory.getParents(nodeId);
    }
    
    @Override
    public boolean removeParent(Address parent) {
        return SelfFactory.removeParent(nodeId, parent);
    }

    @Override
    public void addParent(Address parent) {
        SelfFactory.addParent(nodeId, parent);
    }

    @Override
    public Self clone(int overlayId)  {
        return new SelfImpl(this.getNat(), this.ip, this.port, this.nodeId, overlayId);
    }
    

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Self other = (Self) obj;
        if (addr == null) {
            return false;
        } 
        if (addr.getId() != other.getId()) {
            return false;
        }
        if (overlayId != other.getOverlayId()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (nodeId * 7 + overlayId * 3) / 10 + 13 ;
    }

}