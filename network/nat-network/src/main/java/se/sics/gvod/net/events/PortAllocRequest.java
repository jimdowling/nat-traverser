package se.sics.gvod.net.events;

import se.sics.gvod.net.Transport;

/**
 * 
 * @author jdowling
 */
public class PortAllocRequest extends DoubleDispatchRequestId<PortAllocResponse> {

	private final int numPorts;
	private final int startPortRange;
	private final int endPortRange;
	private final Transport protocol;

	public PortAllocRequest(int id, int numPorts) {
		this(id, numPorts, 1025, 65535, Transport.UDP);
	}

	public PortAllocRequest(int id, int numPorts, int startPortRange, int endPortRange, Transport protocol) {
		super(id);
		this.startPortRange = startPortRange;
		this.endPortRange = endPortRange;
		this.numPorts = numPorts;
		this.protocol = protocol;
	}

	public int getNumPorts() {
		return numPorts;
	}

	public int getEndPortRange() {
		return endPortRange;
	}

	public int getStartPortRange() {
		return startPortRange;
	}

	public Transport getProtocol() {
		return protocol;
	}
}
