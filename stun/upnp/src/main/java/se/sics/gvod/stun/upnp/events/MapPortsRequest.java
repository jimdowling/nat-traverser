package se.sics.gvod.stun.upnp.events;

import java.util.Map;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Request;
import se.sics.gvod.stun.upnp.events.MapPortRequest.Protocol;

public final class MapPortsRequest extends Request {

    private final Map<Integer,Integer> privatePublicPorts;
    private final Protocol protocol;
    private final TimeoutId requestId;

    public MapPortsRequest(TimeoutId requestId, Map<Integer,Integer> privatePublicPorts, Protocol protocol) {
        this.requestId = requestId;
        this.protocol = protocol;
        if (privatePublicPorts == null) {
            throw new IllegalArgumentException("MapPortsRequest map of private to public ports was NULL");
        }
        this.privatePublicPorts = privatePublicPorts;
    }

    public TimeoutId getRequestId() {
        return requestId;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Map<Integer, Integer> getPrivatePublicPorts() {
        return privatePublicPorts;
    }

}
