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
    private final Transport transport;
    private final boolean bindAllNetworkIfs;
    
    public PortBindRequest(Address address, Transport transport) {
        this(address, transport, true);
    }
    
    public PortBindRequest(Address address, Transport transport, 
            boolean bindAllNetworkIfs) {
        super(address.getId());
        assert (address.getIp() != null);
        assert (address.getPort() != 0);
        assert (transport != null);
        this.ip = address.getIp();
        this.port = address.getPort();
        this.transport = transport;
        this.bindAllNetworkIfs = bindAllNetworkIfs;
    }

    public boolean isBindAllNetworkIfs() {
        return bindAllNetworkIfs;
    }
    
    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Transport getTransport() {
        return transport;
    }
}
