package se.sics.gvod.nat.traversal.events;

import se.sics.gvod.net.Nat;
import se.sics.kompics.Event;



/**
 *
 * @author Salman
 */

public final class SetNat extends Event
{
    public final Nat nat;
    public SetNat(Nat nat)
    {
        this.nat = nat;
    }

    public Nat getNat()
    {
        return nat;
    }  
}
