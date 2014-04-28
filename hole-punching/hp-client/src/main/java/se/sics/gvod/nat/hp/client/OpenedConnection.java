/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.hp.client;

import se.sics.gvod.address.Address;
import se.sics.gvod.net.Nat;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author Jim
 */
public class OpenedConnection {

    final Address holeOpened;
    final int portInUse;
    final boolean sharedPort;
    long lastUsed;
    int numTimesUsed;
    final int natBindingTimeout;
    final boolean heartbeat;
    TimeoutId heartbeatTimeoutId = null;

    /**
     *
     * @param portInUseLocally the port used by the client to send to this
     * openedConnection
     * @param holeOpened
     * @param natBindingTimeout
     * @param heartbeat
     */
    public OpenedConnection(
            int portInUseLocally, 
            boolean sharedPort,
            Address holeOpened,
            int natBindingTimeout,
            boolean heartbeat) {
        this.portInUse = portInUseLocally;
        this.sharedPort = sharedPort;
        this.holeOpened = holeOpened;
        this.lastUsed = System.currentTimeMillis();
        this.natBindingTimeout = Math.max(Nat.DEFAULT_RULE_EXPIRATION_TIME, natBindingTimeout);
        this.heartbeat = heartbeat;
        this.numTimesUsed = 0;
    }

    public TimeoutId getHeartbeatTimeoutId() {
        return heartbeatTimeoutId;
    }

    public void setHeartbeatTimeoutId(TimeoutId heartbeatTimeoutId) {
        this.heartbeatTimeoutId = heartbeatTimeoutId;
    }

    public boolean isSharedPort() {
        return sharedPort;
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

    public void incNumTimesUsed() {
        this.numTimesUsed = (this.numTimesUsed == Integer.MAX_VALUE) ? Integer.MIN_VALUE :
                ++this.numTimesUsed;
    }

    public int getNumTimesUsed() {
        return numTimesUsed;
    }

    public int getPortInUse() {
        return portInUse;
    }

    public boolean isExpired() {
        return ((System.currentTimeMillis() - getLastUsed())
                > (getNatBindingTimeout()));
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
