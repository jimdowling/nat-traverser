/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics.nat.utils.getip;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 *
 * @author jdowling
 */
public class IpAddrStatus {

    private final NetworkInterface networkInterface;
    private final InetAddress addr;
    private final boolean up;
    private final int networkPrefixLength;
    private final int mtu;

    public IpAddrStatus(NetworkInterface networkInterface,
            InetAddress addr, boolean up, int networkPrefixLength, int mtu) {
        this.networkInterface = networkInterface;
        this.addr = addr;
        this.up = up;
        this.networkPrefixLength = networkPrefixLength;
        this.mtu = mtu;
    }

    public IpAddrStatus(IpAddrStatus ipAddrStatus) {
        this.networkInterface = ipAddrStatus.getNetworkInterface();
        this.addr = ipAddrStatus.getAddr();
        this.up = ipAddrStatus.isUp();
        this.networkPrefixLength = ipAddrStatus.getNetworkPrefixLength();
        this.mtu = ipAddrStatus.getMtu();
    }

    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    public int getMtu() {
        return mtu;
    }

    public InetAddress getAddr() {
        return addr;
    }

    public int getNetworkPrefixLength() {
        return networkPrefixLength;
    }

    public boolean isUp() {
        return up;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj instanceof IpAddrStatus == false) {
            return false;
        }
        IpAddrStatus that = (IpAddrStatus) obj;
        if (this.up != that.up) {
            return false;
        }
        if (this.networkInterface == null && that.networkInterface != null) {
            return false;
        }
        if (this.networkInterface != null && that.networkInterface == null) {
            return false;
        }
        if (this.networkInterface != null & that.networkInterface != null) {
            if (this.networkInterface.equals(that.networkInterface) == false) {
                return false;
            }
        }
        if (this.addr.equals(that.addr) == false) {
            return false;
        }
        // ignore MTU and networkprefix length differences

        return true;
    }

    @Override
    public int hashCode() {

        int res = 37 + ((this.up == true) ? 1 : 0);
        if (networkInterface != null) {
            res += networkInterface.hashCode();
        }
        if (addr != null) {
            res += addr.hashCode();
        }
        // ignore MTU and networkprefix length values
        return res;
    }

}
