/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.client.events;

import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author jdowling
 */
public class DetermineNatTimeout extends Timeout {

    public DetermineNatTimeout(ScheduleTimeout request) {
        super(request);
    }

}
