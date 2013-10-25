package se.sics.gvod.simulator.nattraverser;

import se.sics.kompics.Event;

public final class Disconnect extends Event {

    private final int peerId;
    private final int numToDisconnect;

    public Disconnect(int peerId, int numToDisconnect) {
        this.peerId = peerId;
        this.numToDisconnect = numToDisconnect;
    }

    public int getNumToDisconnect() {
        return numToDisconnect;
    }
    
    public int getPeerId() {
        return peerId;
    }
}
