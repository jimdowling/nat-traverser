/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.OpCode;
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
        public OpCode getOpcode() {
            return OpCode.ACK;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            return createChannelBufferWithHeader();
        }
    }
