package se.sics.gvod.nat.traversal.events;

import java.util.ArrayList;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.Nat;
import se.sics.kompics.Event;


/**
 *
 * @author Salman
 */

public final class NatTraverserInitializationCheckResponse extends Event
{
    public enum Type
    {
        SUCCESS,
        FAILURE
    }

    private final Type type;
    private final int id;
    ArrayList<Address> parentsList;
    public NatTraverserInitializationCheckResponse(Type type, int id)
    {
        this.type = type;
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public Type getResponseType()
    {
        return type;
    }
}
