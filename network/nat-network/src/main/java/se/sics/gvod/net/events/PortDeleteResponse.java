/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.net.events;

import java.util.Set;

/**
 *
 * @author jdowling
 */
public abstract class PortDeleteResponse extends DoubleDispatchResponseId<PortDeleteRequest>

{
 private Set<Integer> ports;
 private final Object key;

    public PortDeleteResponse(PortDeleteRequest request, Object key) {
        super(request);
        this.key = key;
    }

    public PortDeleteResponse(PortDeleteRequest request, Object key, Set<Integer> ports) {
        super(request);
        this.key = key;
        this.ports = ports;
    }

    public Object getKey() {
        return key;
    }

    public Set<Integer> getPorts() {
        return ports;
    }

    public void setPorts(Set<Integer> ports) {
        this.ports = ports;
    }
}
