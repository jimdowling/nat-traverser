package se.sics.gvod.net.events;

import java.net.InetAddress;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.Transport;

/**
 *
 * @author jdowling
 */
public class PortBindRequest extends DoubleDispatchRequestId<PortBindResponse> {

    private final InetAddress ip;
    private final int port;
    private final boolean bindAllNetworkIfs;
    private final Transport transport;

    public PortBindRequest(Address address, boolean bindAllNetworkIfs,
            Transport transport) {
        super(address.getId());
        assert (address.getIp() != null);
        assert (address.getPort() != 0);
        assert (transport != null);
        this.ip = address.getIp();
        this.port = address.getPort();
        this.bindAllNetworkIfs = bindAllNetworkIfs;
        this.transport = transport;
    }

    public InetAddress getIp() {
        return ip;
    }

    public boolean isBindAllNetworkIfs() {
        return bindAllNetworkIfs;
    }

    public int getPort() {
        return port;
    }

    public Transport getTransport() {
        return transport;
    }
}
