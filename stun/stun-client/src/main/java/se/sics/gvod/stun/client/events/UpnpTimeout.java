/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.client.events;

import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author jim
 */
public class UpnpTimeout extends Timeout{
    
    private final TimeoutId requestId;

        public UpnpTimeout(ScheduleTimeout request, TimeoutId requestId) {
            super(request);
            this.requestId = requestId;
        }

        public TimeoutId getRequestId() {
            return requestId;
        }
    
}
