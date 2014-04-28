package se.sics.gvod.net.events;

import java.net.InetAddress;
import se.sics.gvod.net.Transport;

/**
 *
 * @author jdowling
 */
public class PortAllocRequest extends DoubleDispatchRequestId<PortAllocResponse> {

    private final InetAddress ip;
    private final int numPorts;
    private final Transport transport;

    public PortAllocRequest(InetAddress ip, int id, int numPorts, Transport protocol) {
        super(id);
        this.ip = ip;
        this.numPorts = numPorts;
        this.transport = protocol;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getNumPorts() {
        return numPorts;
    }

    public Transport getTransport() {
        return transport;
    }
}
