/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.servermain;

import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author jim
 */
public class HeartbeatTimeout extends Timeout {

    public HeartbeatTimeout(ScheduleTimeout st) {
        super(st);
    }
}
