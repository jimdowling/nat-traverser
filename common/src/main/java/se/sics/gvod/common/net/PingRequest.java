package se.sics.gvod.common.net;

import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Request;
import se.sics.gvod.address.Address;
import se.sics.gvod.timer.UUID;

public final class PingRequest extends Request {

    private final TimeoutId id;
    private final Address addr;
    private final int pingTimeout;

    public PingRequest(Address addr, int pingTimeout) {
        if (addr == null) {
            throw new NullPointerException("Null InetAddress param to PingRequest constructor");
        }
        this.addr = addr;
        this.id = UUID.nextUUID();
        this.pingTimeout = pingTimeout;
    }

    public int getPingTimeout() {
        return pingTimeout;
    }

    public Address getAddr() {
        return addr;
    }

    public TimeoutId getId() {
        return id;
    }
}
