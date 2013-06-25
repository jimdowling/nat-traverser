/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.client;

import java.io.Serializable;
import se.sics.gvod.address.Address;

/**
 *
 */
public class PingTime implements Serializable, Comparable<PingTime> {
    
    //round-trip time
    private long rtt = Long.MAX_VALUE;
    private Address address;

    public PingTime(long startTime, Address address) {
        this.address = address;
        this.rtt = System.currentTimeMillis() - startTime;
    }

    public long getRtt() {
        return rtt;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public int compareTo(PingTime pt) {
        if (pt.equals(this) || pt.getRtt() == this.rtt)
            return 0;
        else if (this.rtt < pt.getRtt())
            return -1;
        else return 1;
    }

}
