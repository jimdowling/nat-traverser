package se.sics.gvod.stun.client.events;

import java.net.InetAddress;
import se.sics.gvod.net.Nat;
import se.sics.kompics.Event;
import se.sics.gvod.address.Address;

public final class GetNatTypeResponse extends Event {

    public static enum Status {
        SUCCEED,
        FAIL,
        NO_UPNP,
        UPNP_STUN_SERVERS_ENABLED,
        FIRST_SERVER_FAILED,
        SECOND_SERVER_FAILED,
        NO_SESSION,
        ALL_HOSTS_TIMED_OUT,
        ONGOING,
        NO_SERVER
    };
    private final Nat nat;
    private final Status status;
    private final InetAddress externalUpnpIp;
    private final int mappedUpnpPort;
    private final Address stunServer;
    private final long timeTaken;
    
    public GetNatTypeResponse(Nat nat, Status status, Address stunServer, long timeTaken) {
        this.nat = nat;
        this.status = status;
        this.mappedUpnpPort = 0;
        this.externalUpnpIp = null;
        this.stunServer = stunServer;
        this.timeTaken = timeTaken;
    }

    public GetNatTypeResponse(Nat natType, Status status, InetAddress externalUpnpIp, 
            int mappedUpnpPort, long timeTaken) {
        this.nat = natType;
        this.status = status;
        this.externalUpnpIp = externalUpnpIp;
        this.mappedUpnpPort = mappedUpnpPort;
        this.stunServer = null;
        this.timeTaken = timeTaken;
    }

    public Address getStunServer() {
        return this.stunServer;
    }

    public InetAddress getExternalUpnpIp() {
        return externalUpnpIp;
    }

    public int getMappedUpnpPort() {
        return mappedUpnpPort;
    }

    public Nat getNat() {
        return nat;
    }

    public Status getStatus() {
        return status;
    }

    public long getTimeTaken() {
        return timeTaken;
    }
}
