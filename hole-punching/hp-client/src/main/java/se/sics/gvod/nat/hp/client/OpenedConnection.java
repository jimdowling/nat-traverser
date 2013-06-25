/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.hp.client;

import se.sics.gvod.address.Address;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;

/**
 *
 * @author Salman, Jim
 */
public class OpenedConnection {

    final Address holeOpened;
    final VodAddress rendezvousServerAddress;              // only used in SHP when m(initiator) is not EI
    final HPMechanism hpMechanism;
    final HPRole hpRole;
    final int portInUse;
    final Address dummyAddress;                         // only used in PRP and RPC. initiator opens a hole by sending a packet to dummy address\
    long lastUsed;
    final int natBindingTimeout;
    final boolean heartbeat;
    TimeoutId hbTimeoutId;

    public OpenedConnection(VodAddress rendezvousServer, HPMechanism hpMechanism, HPRole hpRole,
            int portInUse, Address holeOpened, Address dummyAddress,
            int natBindingTimeout,
            boolean heartbeat, TimeoutId hbTimeoutId) {
        this.rendezvousServerAddress = rendezvousServer;
        this.hpMechanism = hpMechanism;
        this.hpRole = hpRole;
        this.portInUse = portInUse;
        this.holeOpened = holeOpened;
        this.dummyAddress = dummyAddress;
        this.lastUsed = System.currentTimeMillis();
        this.natBindingTimeout = natBindingTimeout;
        this.heartbeat = heartbeat;
        this.hbTimeoutId = hbTimeoutId;
    }

    public int getNatBindingTimeout() {
        return natBindingTimeout;
    }

    public TimeoutId getHbTimeoutId() {
        return hbTimeoutId;
    }

    public void setHbTimeoutId(TimeoutId hbTimeoutId) {
        this.hbTimeoutId = hbTimeoutId;
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

    public Address getDummyAddress() {
        return dummyAddress;
    }

    public HPMechanism getHpMechanism() {
        return hpMechanism;
    }

    public HPRole getHPRole() {
        return hpRole;
    }

    public int getPortInUse() {
        return portInUse;
    }

    public VodAddress getRendezvousServerAddress() {
        return rendezvousServerAddress;
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
        sb.append("OpenedConnection( ").append(holeOpened).append(" ; mechanism=").append(hpMechanism);
        sb.append("; role=").append(hpRole).append("; lastUsed=").append(lastUsed).append("; portInUse=").append(portInUse).append("\n");
        return sb.toString();
    }
}
