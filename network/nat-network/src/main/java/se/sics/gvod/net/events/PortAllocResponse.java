/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.events;

import java.util.Set;

/**
 *
 * @author jdowling
 * 
 * TODO: Assumption is that this always succeeds.
 * Add a status enum indicating if it succeeded, and if it failed - reason.
 */
public abstract class PortAllocResponse extends DoubleDispatchResponseId<PortAllocRequest> 
{

    private Set<Integer> allocatedPorts;
    private final Object key;

    public PortAllocResponse(PortAllocRequest request, Object key) {
        super(request);
        this.key = key;
    }

    public Set<Integer> getAllocatedPorts() {
        return allocatedPorts;
    }


    public Object getKey() {
        return key;
    }

    public void setAllocatedPorts(Set<Integer> ports) {
        this.allocatedPorts = ports;
    }
}
