/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.parentmaker.evts;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class PrpPortsResponse extends PortAllocResponse
{
    private final long rto;

    public PrpPortsResponse(PortAllocRequest request, VodAddress server, long rto) {
        super(request,server);
        this.rto = rto;
    }

    public long getRto() {
        return rto;
    }
    
    public VodAddress getServer() {
        return (VodAddress) getKey();
    }
}
