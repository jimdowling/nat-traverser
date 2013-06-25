/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.kompics.nat.utils.getip;

import se.sics.kompics.nat.utils.getip.events.IpChange;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.PortType;

/**
 *
 * @author jdowling
 */
public final class ResolveIpPort extends PortType {
	{
		negative(GetIpRequest.class);
		positive(IpChange.class);
		positive(GetIpResponse.class);
	}
}
