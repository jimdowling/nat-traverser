/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public class GoMsg_PortResponse extends PortBindResponse
{
    private Integer key;
    private final int retries;
    private final boolean fixedPort;
    private final TimeoutId msgTimeoutId;
    
    public GoMsg_PortResponse(PortBindRequest request, Integer key, 
            int retries, boolean fixedPort, TimeoutId msgTimeoutId) {
        super(request);
        this.key = key;
        this.retries = retries;
        this.fixedPort = fixedPort;
        this.msgTimeoutId = msgTimeoutId;
    }

    public boolean isFixedPort() {
        return fixedPort;
    }

    public int getRetries() {
        return retries;
    }
    
    public Integer getKey() {
        return key;
    }

    public TimeoutId getMsgTimeoutId() {
        return msgTimeoutId;
    }

}
