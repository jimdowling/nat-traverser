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
public class RoundTripTime implements Serializable, Comparable<RoundTripTime> {
    
    //round-trip time
    private final long rtt;
    private final Address address;

    public RoundTripTime(long rtt, Address address) {
        this.address = address;
        this.rtt = rtt;
    }

    public long getRtt() {
        return rtt;
    }

    public Address getAddress() {
        return address;
    }
    
    @Override
    public int compareTo(RoundTripTime pt) {
        if (pt.equals(this) || pt.getRtt() == this.rtt)
            return 0;
        else if (this.rtt < pt.getRtt())
            return -1;
        else return 1;
    }

}
