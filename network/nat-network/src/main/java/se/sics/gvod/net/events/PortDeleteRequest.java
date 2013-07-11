package se.sics.gvod.net.events;

import java.util.Set;

import se.sics.gvod.net.Transport;

/**
 * 
 * @author jdowling
 */
public class PortDeleteRequest extends DoubleDispatchRequestId<PortDeleteResponse> {

	private final Set<Integer> portsToDelete;
	private final Transport protocol;

	public PortDeleteRequest(int id, Set<Integer> portsToDelete, Transport protocol) {
		super(id);
		this.portsToDelete = portsToDelete;
		this.protocol = protocol;
	}
	
	public PortDeleteRequest(int id, Set<Integer> portsToDelete) {
		this(id, portsToDelete, Transport.UDP);
	}

	public Set<Integer> getPortsToDelete() {
		return portsToDelete;
	}

	public Transport getProtocol() {
		return protocol;
	}
}
