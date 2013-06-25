/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public class PRC_PortResponse extends PortAllocResponse
{
    private final VodAddress zServer;
    private final TimeoutId msgTimeoutId;
    
    public PRC_PortResponse(PortAllocRequest request, Integer key, VodAddress zServer,
            TimeoutId msgTimeoutId) {
        super(request,key);
        this.zServer = zServer;
        this.msgTimeoutId = msgTimeoutId;
    }

    public VodAddress getzServer() {
        return zServer;
    }

    public TimeoutId getMsgTimeoutId() {
        return msgTimeoutId;
    }
}
