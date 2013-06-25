package se.sics.gvod.stun.upnp.events;

import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Request;

public final class MapPortRequest extends Request {

    public static enum Protocol {
        TCP, UDP
    };

//    private final NetworkInterface networkInterface;
//    private final Address internalAddr;
    private final int privatePort;
    private final Protocol protocol;
    private final int requestedPort;
    private final TimeoutId requestId;

    public MapPortRequest(TimeoutId requestId, int privatePort, Protocol protocol,
            int requestedPort) {
        this.requestId = requestId;
        this.protocol = protocol;
        this.requestedPort = requestedPort;
        this.privatePort = privatePort;
    }

    public TimeoutId getRequestId() {
        return requestId;
    }

    public int getPrivatePort() {
        return privatePort;
    }

    public Protocol getProtocol() {
        return protocol;
    }
    public int getRequestedPort() {
        return requestedPort;
    }
}
