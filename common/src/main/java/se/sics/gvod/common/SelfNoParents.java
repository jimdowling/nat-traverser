/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Nat;

/**
 *
 * @author Owner
 */
public class SelfNoParents extends SelfBase
{
    /**
     * This is like a cached self-address.
     * When I need to use my real GVodAddress, i call
     * self.getAddress().
     * Only the parents of a GVodAddress may change, i can access
     * any other attribute of GVodAddress using the cached object.
     */
    private final VodAddress vodAddr;
    
    
    public SelfNoParents(VodAddress addr) {
        this(addr.getNat(), addr.getIp(), addr.getPort(), addr.getId(), addr.getOverlayId());
    }
    
    public SelfNoParents(Nat nat, InetAddress ip, int port, int nodeId, int overlayId) {
        super(nat, ip, port, nodeId, overlayId);
        this.vodAddr = new VodAddress(this.addr, overlayId, nat);
    }

    /**
     * Gets the self address with the current set of parents.
     * @return 
     */
    @Override
    public VodAddress getAddress() {
        return vodAddr;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateUtility(Utility utility) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VodDescriptor getDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Address> getParents() {
        return new HashSet<Address>();
    }
    
    @Override
    public boolean removeParent(Address parent) {
        return true;
    }

    @Override
    public void addParent(Address parent) {
        return;
    }

    @Override
    public Self clone(int overlayId)  {
        return new SelfNoParents(this.getNat(), ip, port, nodeId, overlayId);
    }    

}