package se.sics.gvod.stun.server;

import se.sics.kompics.PortType;
import se.sics.gvod.stun.server.events.AddPartnerStunServer;

public class StunServerPort extends PortType {
	{
		negative(AddPartnerStunServer.class);
	}
}
