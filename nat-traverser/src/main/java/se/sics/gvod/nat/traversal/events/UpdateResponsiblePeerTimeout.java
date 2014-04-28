/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.traversal.events;

import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author Salman
 */
public class UpdateResponsiblePeerTimeout extends Timeout
{
    public UpdateResponsiblePeerTimeout(SchedulePeriodicTimeout request)
    {
        super(request);
    }
}
