package se.sics.gvod.stun.upnp.events;

import java.net.InetAddress;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Response;

public final class MapPortResponse extends Response {

    private final int allocatedPort;
    private final InetAddress externalIp;
    private final int natId; // in our case actually id is used to distinguish between peers and nats. set it to 0 if testing in real env
    private final boolean status;
    private final int privatePort;
    private final TimeoutId requestId;

    public MapPortResponse(MapPortRequest request,
            int privatePort, int allocatedPort, InetAddress externalIp,
            int natId,
            boolean status) {
        super(request);
        this.privatePort = privatePort;
        this.allocatedPort = allocatedPort;
        this.externalIp = externalIp;
        this.requestId = request.getRequestId();
        this.status = status;
        this.natId = natId;
    }

    public int getNatId()
    {
        return natId;
    }

    
    public TimeoutId getRequestId() {
        return requestId;
    }

    public boolean isStatus() {
        return status;
    }

    public InetAddress getExternalIp() {
        return externalIp;
    }

    public int getPrivatePort() {
        return privatePort;
    }

    public int getAllocatedPort() {
        return allocatedPort;
    }
}
