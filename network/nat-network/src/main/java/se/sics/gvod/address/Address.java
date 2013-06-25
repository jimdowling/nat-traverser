/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.address;

import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author jdowling
 */
public final class Address implements Serializable, Comparable<Address> {

    private static final long serialVersionUID = -9375656166039991L;
    private InetAddress ip;
    private int port;
    private int nodeId;

    /**
     * Instantiates a new address.
     * 
     * @param ip
     *            the ip
     * @param port
     *            the port
     * @param id
     *            the id
     */
    public Address(InetAddress ip, int port, int id) {
        this.ip = ip;
        this.port = port;
        this.nodeId = id;
    }

    public Address(int id) {
        this.ip = null;
        this.port = 0;
        this.nodeId = id;
    }

    public Address(Address addr) {
        this.ip = addr.getIp();
        this.port = addr.getPort();
        this.nodeId = addr.getId();
    }    
    
    /**
     * Gets the ip.
     * 
     * @return the ip
     */
    public final InetAddress getIp() {
        return ip;
    }

    /**
     * Gets the port.
     * 
     * @return the port
     */
    public final int getPort() {
        return port;
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public final int getId() {
        return nodeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeId).append("@").append(ip == null ? "null" : ip.getHostAddress())
                .append(":").append(port);
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + nodeId;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Address other = (Address) obj;
        if (nodeId != other.nodeId) {
            return false;
        }
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        } else if (!ip.equals(other.ip)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Address that) {
        if (this.nodeId < that.nodeId) {
            return -1;
        }
        if (this.nodeId > that.nodeId) {
            return 1;
        }
//		if (this.port < that.port)
//			return -1;
//		if (this.port > that.port)
//			return 1;
//		ByteBuffer thisIpBytes = ByteBuffer.wrap(this.ip.getAddress()).order(
//				ByteOrder.BIG_ENDIAN);
//		ByteBuffer thatIpBytes = ByteBuffer.wrap(that.ip.getAddress()).order(
//				ByteOrder.BIG_ENDIAN);
//		return thisIpBytes.compareTo(thatIpBytes);
        return 0;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public void setId(int id) {
        this.nodeId = id;
    }
}