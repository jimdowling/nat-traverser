/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.parentmaker.evts;

import se.sics.gvod.net.events.PortDeleteRequest;
import se.sics.gvod.net.events.PortDeleteResponse;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class PrpDeletePortsResponse extends PortDeleteResponse
{

    public PrpDeletePortsResponse(PortDeleteRequest request, Object key) {
        super(request, key);
    }
    
}
