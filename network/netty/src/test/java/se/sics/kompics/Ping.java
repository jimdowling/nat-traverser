/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNetty;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class Ping extends DirectMsgNetty.Request {

    public Ping(VodAddress client, VodAddress server) {
        super(client, server);
    }

    @Override
    public int getSize() {
        return super.getHeaderSize()
                ;
    }

    @Override
    public byte getOpcode() {
        return BaseMsgFrameDecoder.PING;
    }

    @Override
    public ByteBuf toByteArray() throws MessageEncodingException {
        return createChannelBufferWithHeader();
    }

    @Override
    public RewriteableMsg copy() {
        Ping copy = new Ping(vodDest, vodSrc);
        copy.setTimeoutId(timeoutId);
        return copy;
    }
}