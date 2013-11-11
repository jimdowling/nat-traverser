/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.timer;


/**
 * This class is used to filter timeouts to Vod components.
 * Without it, every Vod component would receive N * #timeouts
 * for N Vod components.
 * 
 * Components that use the Timer's timer port will all receive all Timeout Events 
 * that come from the timer component. To make sure components only receive
 * timer events destined for them, we introduce an OverlayTimeout event that
 * contains an id (the overlayId), for which timeout events are filtered.
 * It is assumed that each overlay component has its own unique overlayId that is
 * used to filter the OverlayTimeout events destined for it.
 * @author jim
 */
public abstract class OverlayTimeout extends Timeout implements OverlayId {

    private final int overlayId;
    
    public OverlayTimeout(ScheduleTimeout schedule, int overlayId) {
        super(schedule);
        this.overlayId = overlayId;
    }
    
    public OverlayTimeout(SchedulePeriodicTimeout schedule, int overlayId) {
        super(schedule);
        this.overlayId = overlayId;
    }

    @Override
    public int getOverlayId() {
        return overlayId;
    }
}
