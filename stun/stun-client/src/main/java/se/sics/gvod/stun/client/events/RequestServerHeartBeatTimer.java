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
public class RequestServerHeartBeatTimer extends Timeout {
    
    private final long transactionId;

        public RequestServerHeartBeatTimer(ScheduleTimeout request, long transactionId) {
            super(request);
            this.transactionId = transactionId;
        }

        public long getTransactionId() {
            return transactionId;
        }
    
}
