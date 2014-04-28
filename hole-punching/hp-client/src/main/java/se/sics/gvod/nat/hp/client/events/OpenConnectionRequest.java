package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Request;

public final class OpenConnectionRequest extends Request
{
    // Request Sent my upper component to open hole for communication between two clients
    // the client must know the public and private ip of the client to whom it wants to talk to

    private final VodAddress   remoteAddress;
    private final boolean skipPacing;
    private final boolean keepConnectionOpenWithHeartbeat;
    private final TimeoutId msgTimeoutId;

    public OpenConnectionRequest(VodAddress remoteAddress, boolean keepConnectionOpenWithHeartbeat,
            boolean skipPacing, TimeoutId msgTimeoutId)
    {
        this.remoteAddress = remoteAddress;
        this.skipPacing = skipPacing;
        this.keepConnectionOpenWithHeartbeat = keepConnectionOpenWithHeartbeat;
        this.msgTimeoutId = msgTimeoutId;
    }

    public TimeoutId getMsgTimeoutId() {
        return msgTimeoutId;
    }
    
    public boolean isKeepConnectionOpenWithHeartbeat() {
        return keepConnectionOpenWithHeartbeat;
    }
    
    public boolean isSkipPacing() {
        return skipPacing;
    }
    
    public int getRemoteClientId()
    {
        return remoteAddress.getId();
    }


    public VodAddress getRemoteAddress() {
        return remoteAddress;
    }
}
