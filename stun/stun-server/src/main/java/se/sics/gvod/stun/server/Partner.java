/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.server;

import java.io.Serializable;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.net.RttStats;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.RTTStore;

/**
 *
 * @author jdowling
 */
public class Partner implements Comparable<Partner>, Serializable {

    private static final long serialVersionUID = 1L;
    public final static int DEFAULT_RTT = 1000;
    private final VodAddress addr;
    private RTTStore.RTT rtt;

    public Partner(int nodeId, VodAddress partner) {
        this.addr = partner;
        rtt = RTTStore.getRtt(nodeId, addr);
        if (rtt == null) {
            RTTStore.addSample(nodeId, addr, DEFAULT_RTT);
            rtt = RTTStore.getRtt(nodeId, addr);
        }
    }

    public VodAddress getVodAddress() {
        return addr;
    }

    public Address getAddress() {
        return addr.getPeerAddress();
    }

    public long getRTO() {
        return rtt.getRTO();
    }

    public void updateRtt(int nodeId, long rtt) {
        RTTStore.addSample(nodeId, addr, rtt);
    }

    public RttStats getRttStats() {
        return rtt.getRttStats();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Partner == false) {
            return false;
        }
        Partner that = (Partner) obj;
        return addr.getPeerAddress().equals(that.getAddress());
    }

    @Override
    public int hashCode() {
        return addr.hashCode();
    }

    @Override
    public int compareTo(Partner that) {
        if (this.getRTO() == that.getRTO()) {
            // if same RTT, pick node with highest id
            if (this.getAddress().getId() < that.getAddress().getId()) {
                return 1;
            } else {
                return 0;
            }
        } else if (this.getRTO() < that.getRTO()) {
            return 1;
        } else {
            return -1;
        }
    }
}
