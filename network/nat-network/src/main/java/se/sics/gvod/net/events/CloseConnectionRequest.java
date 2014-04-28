package se.sics.gvod.net.events;

import se.sics.gvod.address.Address;
import se.sics.gvod.net.Transport;

/**
 *
 * @author: Steffen Grohsschmiedt
 */
public class CloseConnectionRequest extends DoubleDispatchRequestId<CloseConnectionResponse> {

    private final Address localAddress;
    private final Transport transport;

    public CloseConnectionRequest(int id, Address remoteAddress, Transport transport) {
        super(id);
        this.localAddress = remoteAddress;
        this.transport = transport;
    }

    public Address getRemoteAddress() {
        return localAddress;
    }

    public Transport getTransport() {
        return transport;
    }
}
