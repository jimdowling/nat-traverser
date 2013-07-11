package se.sics.gvod.net.events;

import se.sics.gvod.net.Transport;

/**
 * 
 * @author jdowling
 */
public class PortBindRequest extends DoubleDispatchRequestId<PortBindResponse> {

	private final int port;
	private final Transport protocol;
	
	public PortBindRequest(int id, int port, Transport protocol) {
		super(id);
		this.port = port;
		this.protocol = protocol;
	}
	
	public PortBindRequest(int id, int port) {
		this(id, port, Transport.UDP);
	}

	public int getPort() {
		return port;
	}

	public Transport getProtocol() {
		return protocol;
	}
}
