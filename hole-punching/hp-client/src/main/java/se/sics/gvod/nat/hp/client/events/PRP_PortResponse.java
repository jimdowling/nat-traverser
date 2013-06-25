/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;

/**
 *
 * @author jdowling
 */
public class PRP_PortResponse extends PortAllocResponse
{
    public PRP_PortResponse(PortAllocRequest request, Integer key) {
        super(request,key);
    }

}
