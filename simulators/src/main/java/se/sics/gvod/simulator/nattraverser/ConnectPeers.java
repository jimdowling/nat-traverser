package se.sics.gvod.simulator.nattraverser;

import se.sics.kompics.Event;

public final class ConnectPeers extends Event {

    private final Integer srcId;
    private final Integer destId;

    public ConnectPeers(Integer srcId, Integer destId) {
        this.srcId = srcId;
        this.destId = destId;
    }

    public Integer getDestId() {
        return destId;
    }

    public Integer getSrcId() {
        return srcId;
    }
}
