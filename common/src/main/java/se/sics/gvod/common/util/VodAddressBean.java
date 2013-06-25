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

/**
 *
 * @author jim
 */
public class VodAddressBean implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(VodAddressBean.class);
    private AddressBean addressBean;
    private List<AddressBean> parentsBeanAddress;
    private short natPolicy;

    public VodAddressBean() {
        this.addressBean = new AddressBean();
        this.parentsBeanAddress = new ArrayList<AddressBean>();
        this.natPolicy = 0;
    }

    public VodAddressBean(Address address, Set<Address> parents, short natPolicy) {
        this.addressBean = new AddressBean(address.getId(), address.getIp().getHostAddress(), address.getPort());
        this.parentsBeanAddress = new ArrayList<AddressBean>();
        for (Address a : parents) {
            this.parentsBeanAddress.add(new AddressBean(a.getId(), a.getIp().getHostAddress(), a.getPort()));
        }
        this.natPolicy = natPolicy;
    }

    /**
     * @return the natPolicy
     */
    public short getNatPolicy() {
        return natPolicy;
    }

    /**
     * @param natPolicy the natPolicy to set
     */
    public void setNatPolicy(short natPolicy) {
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
}
