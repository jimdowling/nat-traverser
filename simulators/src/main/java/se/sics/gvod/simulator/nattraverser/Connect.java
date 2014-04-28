package se.sics.gvod.simulator.nattraverser;

import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;

public final class Connect extends Event {

    private final VodAddress dest;

    public Connect(VodAddress dest) {
        this.dest = dest;
    }

    public VodAddress getDest() {
        return dest;
    }
}
