package se.sics.gvod.stun.upnp.events;

import java.net.InetAddress;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Response;

public final class UpnpGetPublicIpResponse extends Response {

    private final InetAddress externalIp;
    private final TimeoutId requestId;

    public UpnpGetPublicIpResponse(UpnpGetPublicIpRequest request, InetAddress externalIp) {
        super(request);
        this.externalIp = externalIp;
        requestId = request.getRequestId();
    }

    public InetAddress getExternalIp() {
        return externalIp;
    }

    public TimeoutId getRequestId() {
        return requestId;
    }
}
