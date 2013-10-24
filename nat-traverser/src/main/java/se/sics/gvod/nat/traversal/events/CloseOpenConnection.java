package se.sics.gvod.nat.traversal.events;

import se.sics.kompics.Event;



/**
 *
 * @author Jim
 */

public final class CloseOpenConnection extends Event
{
    private final int remoteId;
    public CloseOpenConnection(int remoteId)
    {
        this.remoteId = remoteId;
    }

    public int getRemoteId() {
        return remoteId;
    }

}
