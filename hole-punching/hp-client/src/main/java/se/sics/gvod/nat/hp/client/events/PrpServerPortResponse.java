/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;
import se.sics.gvod.common.hp.HPSessionKey;

/**
 *
 * @author jdowling
 */
public class PrpServerPortResponse extends PortAllocResponse
{

    public PrpServerPortResponse(PortAllocRequest request, HPSessionKey key) {
        super(request,key);
    }
}
