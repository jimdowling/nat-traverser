/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.traversal.events;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author Salman
 */
public class ConnectionEstablishmentTimeout extends Timeout
{
    private final VodAddress destAddress;
    private final TimeoutId msgTimeoutId;
    public ConnectionEstablishmentTimeout(ScheduleTimeout request,
            VodAddress destAddress, TimeoutId msgTimeoutId)
    {
        super(request);
        this.destAddress = destAddress;
        this.msgTimeoutId = msgTimeoutId;
    }

    public VodAddress getDestAddress()
    {
        return destAddress;
    }

    public TimeoutId getMsgTimeoutId() {
        return msgTimeoutId;
    }

}
