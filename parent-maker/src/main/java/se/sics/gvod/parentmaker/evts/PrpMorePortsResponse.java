/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.parentmaker.evts;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class PrpMorePortsResponse extends PortAllocResponse
{
    private final long rto;
    private final TimeoutId timeoutId;
    private final TimeoutId msgTimeoutId;
    
    public PrpMorePortsResponse(PortAllocRequest request, VodAddress server, TimeoutId timeoutId, 
            long rto, TimeoutId msgTimeoutId) {
        super(request,server);
        this.rto = rto;
        this.timeoutId = timeoutId;
        this.msgTimeoutId = msgTimeoutId;
    }

    public TimeoutId getMsgTimeoutId() {
        return msgTimeoutId;
    }

    public TimeoutId getTimeoutId() {
        return timeoutId;
    }
    
    public long getRto() {
        return rto;
    }
    
    public VodAddress getServer() {
        return (VodAddress) getKey();
    }
    
    
}
