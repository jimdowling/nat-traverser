package se.sics.gvod.simulator.nattraverser;

import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;

public final class ConnectionResult extends Event {

    private final VodAddress src;
    private final VodAddress dest;
    private final String natPair;
    private final boolean res;

    public ConnectionResult(VodAddress src, VodAddress dest, String natPair, boolean res) {
        this.src = src;
        this.dest = dest;
        this.natPair = natPair;
        this.res = res;
    }

    public String getNatPair() {
        return natPair;
    }
    
    public VodAddress getDest() {
        return dest;
    }

    public VodAddress getSrc() {
        return src;
    }

    public boolean isRes() {
        return res;
    }

}
