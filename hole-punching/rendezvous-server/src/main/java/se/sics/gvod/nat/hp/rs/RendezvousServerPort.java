package se.sics.gvod.nat.hp.rs;


import se.sics.kompics.PortType;
import se.sics.gvod.nat.hp.rs.events.ChildAdded;
import se.sics.gvod.nat.hp.rs.events.RemoveChild;

public final class RendezvousServerPort extends PortType
{
    {
        negative(ChildAdded.class);
        negative(RemoveChild.class);
    }
}
