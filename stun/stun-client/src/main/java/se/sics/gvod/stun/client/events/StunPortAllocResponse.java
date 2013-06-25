/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.stun.client.events;

import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;


/**
 *
 * @author jdowling
 */
public class StunPortAllocResponse extends PortAllocResponse
{
    public StunPortAllocResponse(PortAllocRequest request, Object key) {
        super(request,key);
    }
}
