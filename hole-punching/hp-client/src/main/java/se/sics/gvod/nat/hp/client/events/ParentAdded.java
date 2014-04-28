package se.sics.gvod.nat.hp.client.events;

import se.sics.kompics.Request;
import se.sics.gvod.address.Address;

public final class ParentAdded extends Request {

    private final Address server;
    private final Address parentToRemove;

    public ParentAdded(Address server) {
        this.server = server;
        parentToRemove=null;
    }
    public ParentAdded(Address server, Address parentToRemove) {
        this.server = server;
        this.parentToRemove = parentToRemove;
    }

    public Address getParentToRemove() {
        return parentToRemove;
    }


    public Address getServer() {
        return server;
    }
}
