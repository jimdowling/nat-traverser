package se.sics.gvod.nat.hp.client.events;

import se.sics.kompics.Request;

/**
 * Request sent to delete the openedConnection object
 * representing a remote private node.
 * 
 * @author jdowling
 */
public final class DeleteConnectionRequest extends Request
{
    private final Integer remoteId;

    public DeleteConnectionRequest(Integer remoteId)
    {
        this.remoteId = remoteId;
    }

    public Integer getRemoteId()
    {
        return remoteId;
    }

}
