package se.sics.gvod.net.events;

import se.sics.gvod.net.Transport;
import java.net.InetSocketAddress;

import se.sics.kompics.Event;

/**
 * 
 * @author Jim Dowling <jdowling@sics.se>
 */
public final class BandwidthStats extends Event {

    private final int lastSecReadBytes;
    private final int lastSecWroteBytes;

    public BandwidthStats(int lastSecReadBytes, int lastSecWroteBytes) {
        this.lastSecReadBytes = lastSecReadBytes;
        this.lastSecWroteBytes = lastSecWroteBytes;
    }

    public int getLastSecReadBytes() {
        return lastSecReadBytes;
    }

    public int getLastSecWroteBytes() {
        return lastSecWroteBytes;
    }
    
}
