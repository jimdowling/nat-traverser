/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.filters;

import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.kompics.ChannelFilter;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class MsgDestFilterNodeId extends ChannelFilter<RewriteableMsg, Integer> {

    public MsgDestFilterNodeId(int id) {
        super(RewriteableMsg.class, id, true);
    }


    @Override
    public Integer getValue(RewriteableMsg event) {
        return event.getDestination().getId();
    }
}
