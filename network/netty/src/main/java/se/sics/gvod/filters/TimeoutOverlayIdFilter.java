/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.filters;

import se.sics.gvod.timer.OverlayTimeout;
import se.sics.kompics.ChannelFilter;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class TimeoutOverlayIdFilter extends ChannelFilter<OverlayTimeout, Integer> {

    public TimeoutOverlayIdFilter(int id) {
        super(OverlayTimeout.class, id, true);
    }

    @Override
    public Integer getValue(OverlayTimeout event) {
        return event.getOverlayId();
    }
}
