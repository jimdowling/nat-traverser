package se.sics.gvod.nat.traversal.events;

import java.util.ArrayList;
import se.sics.gvod.address.Address;
import se.sics.kompics.Event;

/**
 *
 * @author Salman
 */

public final class ParentsChanged extends Event
{
    private final ArrayList<Address> parents;
    public ParentsChanged(ArrayList<Address> parents)
    {
        this.parents = parents;
    }

    public ArrayList<Address> getParents()
    {
        return parents;
    }

}
