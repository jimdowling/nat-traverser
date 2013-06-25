package se.sics.gvod.nat.hp.rs.events;

import se.sics.gvod.net.Nat;
import se.sics.kompics.Event;
import se.sics.gvod.address.Address;

/**
 *
 * @author Jim
 */

public final class ChildAdded extends Event
{
    private final Address publicAddress;
    private final Nat nat;

    public ChildAdded(Address publicAddress,  Nat nat) {
        this.publicAddress = publicAddress;
        this.nat = nat;
    }

    public Nat getNat() {
        return nat;
    }

    public Address getPublicAddress() {
        return publicAddress;
    }

}
