/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.hp.client;

import se.sics.gvod.address.Address;

/**
 *
 * @author Jim
 */
public class OpenedConnection {

    final Address holeOpened;
    final int portInUse;
    long lastUsed;
    short numSuccessfulPings;
    final int natBindingTimeout;
    final boolean heartbeat;

    /**
     *
     * @param portInUseLocally the port used by the client to send to this
     * openedConnection
     * @param holeOpened
     * @param natBindingTimeout
     * @param heartbeat
     */
    public OpenedConnection(
            int portInUseLocally, Address holeOpened,
            int natBindingTimeout,
            boolean heartbeat) {
        this.portInUse = portInUseLocally;
        this.holeOpened = holeOpened;
        this.lastUsed = System.currentTimeMillis();
        this.natBindingTimeout = natBindingTimeout;
        this.heartbeat = heartbeat;
        this.numSuccessfulPings = 0;
    }

    public int getNatBindingTimeout() {
        return natBindingTimeout;
    }

    public boolean isHeartbeat() {
        return heartbeat;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public void incNumSuccessfulPings() {
        this.numSuccessfulPings++;
    }

    public short getNumSuccessfulPings() {
        return numSuccessfulPings;
    }

    public int getPortInUse() {
        return portInUse;
    }

    public boolean isExpired() {
        return ((System.currentTimeMillis() - getLastUsed())
                > getNatBindingTimeout());
    }

    public Address getHoleOpened() {
        return holeOpened;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OpenedConnection( ").append(holeOpened);
        sb.append("; lastUsed=").append(lastUsed).append("; portInUse=").append(portInUse).append("\n");
        return sb.toString();
    }
}
