package se.sics.gvod.nat.traversal;


import se.sics.gvod.nat.traversal.events.CloseOpenConnection;
import se.sics.gvod.nat.traversal.events.HpFailed;
import se.sics.gvod.nat.traversal.events.StartServices;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.kompics.PortType;

public final class NatTraverserPort extends PortType
{ 
    {
        positive(GetNatTypeResponse.class);
        positive(HpFailed.class);
        negative(StartServices.class);
        negative(CloseOpenConnection.class);
    }
}
