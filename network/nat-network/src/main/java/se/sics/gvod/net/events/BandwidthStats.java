package se.sics.gvod.net.events;


import se.sics.kompics.Event;

/**
 * 
 * @author Jim Dowling <jdowling@sics.se>
 */
public final class BandwidthStats extends Event {

    private final int lastSecBytesRead;
    private final int lastSecBytesWritten;
    private final int totalBytesDownloaded;

    public BandwidthStats(int lastSecBytesRead, int lastSecBytesWritten, 
            int totalBytesDownloaded) {
        this.lastSecBytesRead = lastSecBytesRead;
        this.lastSecBytesWritten = lastSecBytesWritten;
        this.totalBytesDownloaded = totalBytesDownloaded;
    }

    public int getTotalBytesDownloaded() {
        return totalBytesDownloaded;
    }
    
    public int getLastSecBytesRead() {
        return lastSecBytesRead;
    }

    public int getLastSecBytesWritten() {
        return lastSecBytesWritten;
    }
    
}
