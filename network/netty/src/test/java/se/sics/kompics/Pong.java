/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
    public class Pong extends Ping {

        public Pong(VodAddress client, VodAddress server) {
            super(client, server);
        }

        @Override
        public int getSize() {
            return getHeaderSize();
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PONG;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            return createChannelBufferWithHeader();
        }
    }
