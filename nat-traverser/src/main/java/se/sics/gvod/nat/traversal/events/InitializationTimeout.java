/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.traversal.events;

import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author Salman
 */
public class InitializationTimeout extends Timeout
{
    public InitializationTimeout(ScheduleTimeout request)
    {
        super(request);
    }
}
