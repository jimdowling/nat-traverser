/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.OpKod;
import static se.sics.gvod.net.DecoderState.READ_CONTENT;
import static se.sics.gvod.net.DecoderState.READ_TYPE;
import se.sics.gvod.net.msgs.RewriteableMsg;

/**
 *
 * http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/replay/ReplayingDecoder.html
 * 
 * @author jdowling
 */
public abstract class MsgFrameDecoder
        extends ReplayingDecoder<DecoderState> {

    private static final Logger logger = LoggerFactory.getLogger(MsgFrameDecoder.class);
    protected OpKod opKod;

    public MsgFrameDecoder() {
        // Set the initial state.
        super(DecoderState.READ_TYPE);
    }

    public Object parse(ChannelBuffer buffer) throws Exception {
        return decode(null, null, buffer, DecoderState.READ_TYPE);
    }

    protected abstract RewriteableMsg decodeMsg(ChannelHandlerContext ctx,
            Channel channel,
            ChannelBuffer buffer) throws MessageDecodingException;            
    
    @Override
    protected Object decode(ChannelHandlerContext ctx, 
            Channel channel,
            ChannelBuffer buffer,
            DecoderState state) throws Exception {

        switch (state) {
            case READ_TYPE:
                Byte type = buffer.readByte();
                opKod = OpKod.fromByte(type);
                checkpoint(DecoderState.READ_CONTENT);
            case READ_CONTENT:

                RewriteableMsg msg = decodeMsg(ctx, channel, buffer);
           
                if (msg == null) {
                    throw new MessageDecodingException("Could not decode msg with header: " + opKod);
                }
//                channel.getPipeline().replace("RS2Decoder", "RS2Decoder",
//                        new GameDecoder());
//                checkpoint(DecoderState.READ_LENGTH);
                checkpoint(DecoderState.READ_TYPE);
                return msg;
            default:
                throw new Error("Problem in MsgFrameDecoder. Shouldn't reach here.");
        }
    }
}
