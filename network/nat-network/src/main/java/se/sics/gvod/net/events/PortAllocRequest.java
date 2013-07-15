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
    private final int startPortRange;
    private final int endPortRange;
    private final boolean bindAllNetworkIfs;
    private final Transport transport;

    public PortAllocRequest(InetAddress ip, int id, int numPorts,
            int startPortRange, int endPortRange, boolean bindAllNetworkIfs,
            Transport protocol) {
        super(id);
        this.ip = ip;
        this.startPortRange = startPortRange;
        this.endPortRange = endPortRange;
        this.numPorts = numPorts;
        this.bindAllNetworkIfs = bindAllNetworkIfs;
        this.transport = protocol;
    }

    public InetAddress getIp() {
        return ip;
    }

    public boolean isBindAllNetworkIfs() {
        return bindAllNetworkIfs;
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

    public Transport getTransport() {
        return transport;
    }
}
