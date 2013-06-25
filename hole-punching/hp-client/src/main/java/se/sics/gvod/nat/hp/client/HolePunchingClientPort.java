package se.sics.gvod.nat.hp.client;

import se.sics.kompics.PortType;
import se.sics.gvod.nat.hp.client.events.DeleteConnectionRequest;
import se.sics.gvod.nat.hp.client.events.OpenConnectionRequest;
import se.sics.gvod.nat.hp.client.events.OpenConnectionResponse;
import se.sics.gvod.nat.hp.client.events.RegisterWithRendezvousServerRequest;
import se.sics.gvod.nat.hp.client.events.RegisterWithRendezvousServerResponse;
import se.sics.gvod.nat.hp.client.events.UnregisterFromRendezvousServer;

public final class HolePunchingClientPort extends PortType {
	{
            // Request-Response
		negative(OpenConnectionRequest.class);
                positive(OpenConnectionResponse.class);

                negative(RegisterWithRendezvousServerRequest.class);
                positive(RegisterWithRendezvousServerResponse.class);
                
            // One-way
                negative(DeleteConnectionRequest.class);
                negative(UnregisterFromRendezvousServer.class);
//                negative(SetHpClientNat.class);
//                negative(ParentAdded.class);

	}
}
