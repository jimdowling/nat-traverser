/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient.events;

import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author jim
 */
public class GradientSetsExchangeCycle extends Timeout {

    public GradientSetsExchangeCycle(SchedulePeriodicTimeout periodicTimeout) {
        super(periodicTimeout);
    }

    public GradientSetsExchangeCycle(ScheduleTimeout timeout) {
        super(timeout);
    }
}
