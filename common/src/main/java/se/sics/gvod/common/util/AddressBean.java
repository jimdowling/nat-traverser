/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.util;

import java.io.Serializable;

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
}
