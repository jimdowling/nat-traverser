package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.hp.events.OpenConnectionResponseType;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Response;
import se.sics.gvod.common.hp.HPMechanism;

public final class OpenConnectionResponse extends Response
{

    private final OpenConnectionResponseType responseType;
    private final OpenConnectionRequest openConnectionRequest;
    private final HPMechanism hpMechanismUsed;
    private final VodAddress remotePublicAddr;
    private final TimeoutId msgTimeoutId;

    public OpenConnectionResponse(OpenConnectionRequest request,
            VodAddress remotePublicAddr,
            OpenConnectionResponseType responseType, 
            HPMechanism hpMechanism,
            TimeoutId msgTimeoutId)
    {
        super(request);
        this.responseType = responseType;
        this.openConnectionRequest = request;
        this.remotePublicAddr = remotePublicAddr;
        this.hpMechanismUsed = hpMechanism;
        this.msgTimeoutId = msgTimeoutId;
    }

    public TimeoutId getMsgTimeoutId() {
        return msgTimeoutId;
    }
    

    public HPMechanism getHpMechanismUsed()
    {
        return hpMechanismUsed;
    }

    public OpenConnectionRequest getOpenConnectionRequest()
    {
        return openConnectionRequest;
    }
    
    public OpenConnectionResponseType getResponseType()
    {
        return responseType;
    }
    
    public VodAddress getRemotePublicAddr() {
        return remotePublicAddr;
    }
}
