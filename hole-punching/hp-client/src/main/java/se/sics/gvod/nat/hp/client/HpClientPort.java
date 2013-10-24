package se.sics.gvod.nat.hp.client;

import se.sics.kompics.PortType;
import se.sics.gvod.nat.hp.client.events.DeleteConnection;
import se.sics.gvod.nat.hp.client.events.OpenConnectionRequest;
import se.sics.gvod.nat.hp.client.events.OpenConnectionResponse;

public final class HpClientPort extends PortType {
	{
            // Request-Response
		negative(OpenConnectionRequest.class);
                positive(OpenConnectionResponse.class);
            // One-way
                negative(DeleteConnection.class);

	}
}
