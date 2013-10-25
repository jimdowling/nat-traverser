package se.sics.gvod.nat.traversal.events;

import se.sics.kompics.Event;



/**
 *
 * @author Jim
 */

public final class DisconnectNeighbour extends Event
{
    private final int remoteId;
    public DisconnectNeighbour(int remoteId)
    {
        this.remoteId = remoteId;
    }

    public int getRemoteId() {
        return remoteId;
    }

}
