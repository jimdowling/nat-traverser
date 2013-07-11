/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.ReplayingDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.msgs.RewriteableMsg;

/**
 * 
 * http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/replay/
 * ReplayingDecoder.html
 * 
 * @author jdowling
 */
public abstract class MsgFrameDecoder extends ReplayingDecoder<DecoderState> {

	private static final Logger logger = LoggerFactory.getLogger(MsgFrameDecoder.class);
	protected byte opKod;

	public MsgFrameDecoder() {
		// Set the initial state.
		super(DecoderState.READ_TYPE);
	}

	public Object parse(ByteBuf buffer) throws Exception {
		MessageList<Object> out = MessageList.newInstance();
		state(DecoderState.READ_TYPE);
		decode(null, buffer, out);
		Object result = out.get(0);
		out.recycle();
		return result;
	}

	protected abstract RewriteableMsg decodeMsg(ChannelHandlerContext ctx, ByteBuf buffer)
			throws MessageDecodingException;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, MessageList<Object> out)
			throws Exception {

		switch (state()) {
		case READ_TYPE:
			opKod = buffer.readByte();
			checkpoint(DecoderState.READ_CONTENT);
		case READ_CONTENT:
			RewriteableMsg msg = decodeMsg(ctx, buffer);
			if (msg == null) {
				throw new MessageDecodingException("Could not decode msg with header: " + opKod);
			}
			// channel.getPipeline().replace("RS2Decoder", "RS2Decoder",
			// new GameDecoder());
			// checkpoint(DecoderState.READ_LENGTH);
			checkpoint(DecoderState.READ_TYPE);
			out.add(msg);
			break;
		default:
			throw new Error("Problem in MsgFrameDecoder. Shouldn't reach here.");
		}
	}
}
