/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.filters;

import se.sics.gvod.net.msgs.NatMsg;
import se.sics.kompics.ChannelFilter;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class MsgDestFilterOverlayId extends ChannelFilter<NatMsg, Integer> {

    public MsgDestFilterOverlayId(int id) {
        super(NatMsg.class, id, true);
    }

    @Override
    public Integer getValue(NatMsg event) {
        return event.getVodDestination().getOverlayId();
    }
}
