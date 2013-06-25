/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.timer;

import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;


/**
 * This class is used to filter timeouts to Vod components.
 * Without it, every Vod component would receive N * #timeouts
 * for N Vod components.
 * @author jim
 */
public abstract class OverlayTimeout extends Timeout {

    private final int overlayId;
    
    public OverlayTimeout(ScheduleTimeout schedule, int overlayId) {
        super(schedule);
        this.overlayId = overlayId;
    }
    
    public OverlayTimeout(SchedulePeriodicTimeout schedule, int overlayId) {
        super(schedule);
        this.overlayId = overlayId;
    }

    public int getOverlayId() {
        return overlayId;
    }
}
