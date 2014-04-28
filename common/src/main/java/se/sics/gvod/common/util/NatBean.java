/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.util;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author jim
 */
public class NatBean implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(NatBean.class);
    private AddressBean addressBean;
    private List<AddressBean> parentsBeanAddress;
    private String natPolicy="NAT_m(PD)_a(R)_m(PD)";
    private boolean upnpSupported=false;
    private int numTimesUnchanged=0;
    private int numTimesSinceStunLastRun=0;
    
    public NatBean() {
        this.addressBean = new AddressBean(VodConfig.getNodeId(), 
                VodConfig.getIp().getHostAddress(), VodConfig.getPort());
        this.parentsBeanAddress = new ArrayList<AddressBean>();
    }

    public NatBean(Address address, Set<Address> parents, String natPolicy, 
            int numTimesChanged, int numTimesSinceStunLastRun, boolean upnpSupported) {
        this.addressBean = new AddressBean(address.getId(), address.getIp().getHostAddress(), address.getPort());
        this.parentsBeanAddress = new ArrayList<AddressBean>();
        for (Address a : parents) {
            this.parentsBeanAddress.add(new AddressBean(a.getId(), a.getIp().getHostAddress(), a.getPort()));
        }
        this.natPolicy = natPolicy;
        this.numTimesUnchanged = numTimesChanged;
        this.numTimesSinceStunLastRun = numTimesSinceStunLastRun;
        this.upnpSupported = upnpSupported;
    }

    public int getNumTimesUnchanged() {
        return numTimesUnchanged;
    }

    public int getNumTimesSinceStunLastRun() {
        return numTimesSinceStunLastRun;
    }

    public boolean isUpnpSupported() {
        return upnpSupported;
    }

    public void incNumTimesSinceStunLastRun() {
        this.numTimesSinceStunLastRun++;
    }
    
    public void resetNumTimesSinceStunLastRun() {
        this.numTimesSinceStunLastRun = 0;
    }

    public void incNumTimesUnchanged() {
        this.numTimesUnchanged++;
    }
    
    public void resetNumTimesUnchanged() {
        this.numTimesUnchanged=0;
    }
    
    
    public void setUpnpSupported(boolean upnpSupported) {
        this.upnpSupported = upnpSupported;
    }

    public VodAddress getVodAddress() {
        Nat n = Nat.parseToNat(natPolicy);
        if (n == null) {
            n = new Nat(Nat.Type.OPEN);
        }
        return new VodAddress(addressBean.getAddress(), VodConfig.SYSTEM_OVERLAY_ID, n);
    }
    
    /**
     * @return the natPolicy
     */
    public String getNatPolicy() {
        return natPolicy;
    }

    /**
     * @param natPolicy the natPolicy to set
     */
    public void setNatPolicy(String natPolicy) {
        this.natPolicy = natPolicy;
    }

    /**
     * @return the addressBean
     */
    public AddressBean getAddressBean() {
        return addressBean;
    }

    /**
     * @param addressBean the addressBean to set
     */
    public void setAddressBean(AddressBean addressBean) {
        this.addressBean = addressBean;
    }

    /**
     * @return the parentsBeanAddress
     */
    public List<AddressBean> getParentsBeanAddress() {
        return parentsBeanAddress;
    }

    /**
     * @param parentsBeanAddress the parentsBeanAddress to set
     */
    public void setParentsBeanAddress(List<AddressBean> parentsBeanAddress) {
        this.parentsBeanAddress = parentsBeanAddress;
    }

    public Address getAddress() {
        try {
            return new Address(InetAddress.getByName(this.addressBean.getHostAddress()),
                    this.addressBean.getPort(), this.addressBean.getId());
        } catch (UnknownHostException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    public Set<Address> getParents() {
        Set<Address> parents = new HashSet<Address>();
        try {
            for (AddressBean a : parentsBeanAddress) {

                parents.add(new Address(InetAddress.getByName(a.getHostAddress()),
                        a.getPort(), a.getId()));

            }
        } catch (UnknownHostException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return parents;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        return hash + addressBean.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NatBean other = (NatBean) obj;
        if (this.addressBean.getId() != other.addressBean.getId()) {
            return false;
        }
        return true;
    }

    public void setNumTimesSinceStunLastRun(int numTimesSinceStunLastRun) {
        this.numTimesSinceStunLastRun = numTimesSinceStunLastRun;
    }

    public void setNumTimesUnchanged(int numTimesUnchanged) {
        this.numTimesUnchanged = numTimesUnchanged;
    }
    
}
