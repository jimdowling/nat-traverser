/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.events;

/**
 *
 * @author jdowling
 */
public class PortAllocRequest extends DoubleDispatchRequestId<PortAllocResponse>
{

    private final int numPorts;
    private final int startPortRange;
    private final int endPortRange;

    public PortAllocRequest(int id, int numPorts) {
        this(id, numPorts, 1025, 65535);
    }
    public PortAllocRequest(int id, int numPorts, int startPortRange, int endPortRange) {
        super(id);
        this.startPortRange = startPortRange;
        this.endPortRange = endPortRange;
        this.numPorts = numPorts;
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
    
}
