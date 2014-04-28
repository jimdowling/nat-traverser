package se.sics.gvod.nat.hp.client.events;

import se.sics.kompics.Event;

/**
 * Sent to delete the openedConnection object
 * representing a remote private node.
 * 
 * @author jdowling
 */
public final class DeleteConnection extends Event
{
    private final Integer remoteId;

    public DeleteConnection(Integer remoteId)
    {
        this.remoteId = remoteId;
    }

    public Integer getRemoteId()
    {
        return remoteId;
    }

}
