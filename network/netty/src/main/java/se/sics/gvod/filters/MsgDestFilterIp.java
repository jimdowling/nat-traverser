/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.filters;

import java.net.InetAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.kompics.ChannelFilter;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class MsgDestFilterIp extends ChannelFilter<RewriteableMsg, InetAddress> {

    public MsgDestFilterIp(InetAddress ip) {
        super(RewriteableMsg.class, ip, true);
    }


    @Override
    public InetAddress getValue(RewriteableMsg event) {
        return event.getDestination().getIp();
    }
}
