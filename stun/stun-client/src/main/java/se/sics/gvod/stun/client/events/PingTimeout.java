/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.client.events;

import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author jim
 */
public class PingTimeout extends Timeout{
    
    public long transactionId;
    
    public PingTimeout(ScheduleTimeout request, long transactionId)
    {
        super(request);
        this.transactionId = transactionId;
    }

    /**
     * @return the transactionId
     */
    public long getTransactionId() {
        return transactionId;
    }
    
}
