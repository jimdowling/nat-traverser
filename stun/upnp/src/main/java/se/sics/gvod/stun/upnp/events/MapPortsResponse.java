package se.sics.gvod.stun.upnp.events;

import java.net.InetAddress;
import java.util.Map;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Response;

public final class MapPortsResponse extends Response {

    private final Map<Integer, Integer> privatePublicPorts;
    private final InetAddress externalIp;
    private final int natId;
    // in our case actually id is used to distinguish between peers and nats. set it to 0 if testing in real env
    private final boolean status;
    private final TimeoutId requestId;

    public MapPortsResponse(MapPortsRequest request,
            Map<Integer,Integer> privatePublicPorts, InetAddress externalIp,
            int natId,
            boolean status) {
        super(request);
        this.privatePublicPorts = privatePublicPorts;
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

    public Map<Integer, Integer> getPrivatePublicPorts() {
        return privatePublicPorts;
    }

}
