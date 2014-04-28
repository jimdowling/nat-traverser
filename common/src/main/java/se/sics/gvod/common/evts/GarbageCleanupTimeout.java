/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.evts;

import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
    public class GarbageCleanupTimeout extends Timeout {

        public GarbageCleanupTimeout(SchedulePeriodicTimeout request) {
            super(request);
        }
    }
