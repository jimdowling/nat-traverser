/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.hp.client.events;

import java.util.Set;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;

/**
 *
 * @author jdowling
 */
public class InterleavedPRP_PortResponse extends PortAllocResponse
{
    private final Address parent;
    public InterleavedPRP_PortResponse(PortAllocRequest request, Address parent,
            Integer key) {
        super(request,key);
        this.parent = parent;
    }

    public Address getParent() {
        return parent;
    }
}
