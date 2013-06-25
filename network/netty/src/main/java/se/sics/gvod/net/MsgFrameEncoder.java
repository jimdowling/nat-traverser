/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.net;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import se.sics.gvod.common.msgs.Encodable;

/**
 *
 * @author jdowling
 */
public class MsgFrameEncoder extends OneToOneEncoder
{

    public MsgFrameEncoder() {
    }

    @Override
    protected Object encode(ChannelHandlerContext chc, Channel chnl, Object o) throws Exception {

        if (o instanceof Encodable) {
            Encodable msg = (Encodable) o;
            return msg.toByteArray();
        }

        return o;
    }
}
