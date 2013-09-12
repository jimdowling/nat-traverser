/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.util;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.gvod.address.Address;

/**
 *
 * @author jim
 */
public class AddressBean implements Serializable {

    private int id;
    private int port;
    private String hostAddress;

    public AddressBean() {
        this.id = 0;
        this.hostAddress = null;
        this.port = 0;
    }

    public AddressBean(int id, String hostAddress, int port) {
        this.id = id;
        this.hostAddress = hostAddress;
        this.port = port;
    }
    
    public AddressBean(Address addr) {
        this.id = addr.getId();
        this.hostAddress = addr.getIp().getHostAddress();
        this.port = addr.getPort();
    }

    /**
     * @return the id
     */
    public int getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the hostAddress
     */
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * @param hostAddress the hostAddress to set
     */
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }
    
    public Address getAddress() {
        InetAddress ip=null;
        try {
            ip = InetAddress.getByName(getHostAddress());
        } catch (UnknownHostException ex) {
            // We don't need to propagate this exception up to the client, as it
            // will just fail when comparing addresses
            Logger.getLogger(AddressBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new Address(ip, getPort(), getId());
    }
}
