/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.emu;

import java.net.InetAddress;

/**
 *
 * @author Salman
 *
 * the notion that IP uniquely identifies every peer/node does not work in
 * Kompics; coz every peer/node has same IP. But Address of every node/peer has
 * a unique attribute called id. IP combined with id can uniquely identify the
 * node/peer.
 */
public class IpIntPair {

    private InetAddress ip;
    private int kompicsID;

    public IpIntPair(InetAddress ip, int id) {
        this.ip = ip;
        this.kompicsID = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = ((prime * kompicsID) % Integer.MAX_VALUE) + ((ip == null) ? 0 : ip.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return " (" + kompicsID + ") " + ip.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof IpIntPair == false) {
            return false;
        }
        IpIntPair pair = (IpIntPair) obj;
        if (pair.getIp().equals(ip) && pair.getKompicsID() == kompicsID) {
            return true;
        } else {
            return false;
        }
    }

    public int getKompicsID() {
        return kompicsID;
    }

    public InetAddress getIp() {
        return ip;
    }
};