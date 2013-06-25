/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.emu.events;

import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author jdowling
 */
public class RuleCleanupTimeout extends Timeout {

    public RuleCleanupTimeout(SchedulePeriodicTimeout request) {
        super(request);
    }
}
