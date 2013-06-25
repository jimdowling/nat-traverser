package se.sics.gvod.stun.client;

import se.sics.gvod.stun.client.events.GetNatTypeRequest;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.kompics.PortType;
import se.sics.gvod.stun.client.events.GetNatTypeResponseRuleExpirationTime;

public final class StunPort extends PortType {
	{
		negative(GetNatTypeRequest.class);
                positive(GetNatTypeResponse.class);
                positive(GetNatTypeResponseRuleExpirationTime.class);
	}
}
