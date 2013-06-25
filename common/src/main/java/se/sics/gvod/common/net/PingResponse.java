package se.sics.gvod.common.net;

import se.sics.kompics.Response;
import se.sics.gvod.address.Address;

public final class PingResponse extends Response {

    private final Address addr;
    private final long timeTaken;
    private final int pingTimeout;
    private final boolean alive;

    public PingResponse(PingRequest request, long timeTaken, boolean alive) {
        super(request);
        this.addr = request.getAddr();
        this.pingTimeout = request.getPingTimeout();
        this.timeTaken = timeTaken;
        this.alive = alive;
    }

    public Address getAddr() {
        return addr;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public boolean isAlive() {
        return alive;
    }

    public int getPingTimeout() {
        return pingTimeout;
    }

}
