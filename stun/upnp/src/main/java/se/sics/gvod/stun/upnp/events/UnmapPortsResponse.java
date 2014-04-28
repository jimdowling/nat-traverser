package se.sics.gvod.stun.upnp.events;

import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Response;

public final class UnmapPortsResponse extends Response {

    private final TimeoutId requestId;
    private final boolean success;

    public UnmapPortsResponse(UnmapPortsRequest request, 
            boolean success) {
        super(request);
        this.success = success;
        this.requestId = request.getRequestId();
    }

    public boolean isSuccess() {
        return success;
    }

    public TimeoutId getRequestId() {
        return requestId;
    }

}
