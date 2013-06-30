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
import se.sics.gvod.common.RTTStore.RTT;

/**
 *
 * @author jdowling
 */
public class Partner implements Comparable<Partner>, Serializable {

    private static final long serialVersionUID = 1L;
    private final VodAddress addr;
    private final int nodeId;
    private final long defaultRto;

    public Partner(int nodeId, VodAddress partner, long defaultRto) {
        this.addr = partner;
        this.nodeId = nodeId;
        this.defaultRto = defaultRto;
    }

    public VodAddress getVodAddress() {
        return addr;
    }

    public Address getAddress() {
        return addr.getPeerAddress();
    }

    public long getRTO() {
        RTT r = RTTStore.getRtt(nodeId, addr);
        if (r == null) {
            return defaultRto;
        }
        return r.getRTO();
    }

    public void updateRtt(long rtt) {
        RTTStore.addSample(nodeId, addr, rtt);
    }

    public RttStats getRttStats() {
        RTT r = RTTStore.getRtt(nodeId, addr);
        if (r == null) {
            throw new NullPointerException("Cannot get RTTStats on partner, as none exist");
        }
        return r.getRttStats();
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
