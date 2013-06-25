package se.sics.gvod.nat.hp.client.events;

import se.sics.kompics.Request;

public final class DeleteConnectionRequest extends Request
{
    // request sent by the upper/outer component to delete the connection

    private final Integer connectionKey;

    public DeleteConnectionRequest(Integer connectionKey)
    {
        this.connectionKey = connectionKey;
    }

    public Integer getConnectionKey()
    {
        return connectionKey;
    }

}
