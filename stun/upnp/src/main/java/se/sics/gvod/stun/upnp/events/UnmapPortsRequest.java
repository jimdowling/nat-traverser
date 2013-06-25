package se.sics.gvod.stun.upnp.events;

import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Request;

public final class UnmapPortsRequest extends Request {
    private final TimeoutId requestId;

    public UnmapPortsRequest(TimeoutId requestId) {
        this.requestId = requestId;
    }

    public TimeoutId getRequestId() {
        return requestId;
    }
}
