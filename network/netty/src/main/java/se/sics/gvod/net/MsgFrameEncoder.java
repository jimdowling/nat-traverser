/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.MessageToMessageEncoder;
import se.sics.gvod.common.msgs.Encodable;

/**
 * 
 * @author jdowling
 */
public class MsgFrameEncoder extends MessageToMessageEncoder<Encodable> {

	public MsgFrameEncoder() {
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Encodable msg, MessageList<Object> out)
			throws Exception {
		out.add(msg.toByteArray());
	}
}
