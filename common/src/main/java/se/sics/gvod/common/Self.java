/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common;

import java.net.InetAddress;
import java.util.Set;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodAddress;

/**
 * Self is used to create a VodAddress or get the peer's NatType or its utility.
 * It exists because a node's address may be accessed from multiple components,
 * and modified at different components. Instead of sending events to notify
 * components about updates to addresses (e.g., a parent added to a private node's
 * VodAddress), nodes call the method getAddress() whenever they need to use their
 * own VodAddress, similar for Utility values and the peer's NAT type.
 * @author jim
 */
public interface Self 
{
    public VodAddress getAddress();

    public VodDescriptor getDescriptor();

    public Utility getUtility();

    public void updateUtility(Utility utility);
    
    public int getId();
    
    public int getOverlayId();
    
    public int getPort();
    
    public InetAddress getIp();

    public Nat getNat();
    
    public void setNat(Nat nat);
    
    public Set<Address> getParents();

    public boolean removeParent(Address parent);
    
    public void addParent(Address parent);
    
    public Self clone(int overlayId);

    public boolean isOpen();

    public boolean isPacingReqd();
    
    public HPMechanism getHpMechanism(VodAddress dest);

}