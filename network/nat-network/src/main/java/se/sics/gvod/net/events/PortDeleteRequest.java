package se.sics.gvod.net.events;

import java.util.Set;

import se.sics.gvod.net.Transport;

/**
 * 
 * @author jdowling
 */
public class PortDeleteRequest extends DoubleDispatchRequestId<PortDeleteResponse> {

	private final Set<Integer> portsToDelete;
	private final Transport transport;

	public PortDeleteRequest(int id, Set<Integer> portsToDelete, Transport transport) {
		super(id);
		this.portsToDelete = portsToDelete;
		this.transport = transport;
	}
	
	public PortDeleteRequest(int id, Set<Integer> portsToDelete) {
		this(id, portsToDelete, Transport.UDP);
	}

	public Set<Integer> getPortsToDelete() {
		return portsToDelete;
	}

	public Transport getTransport() {
		return transport;
	}
}
