/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.hp.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.Encodable;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.OpCode;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.VodMsg;

/**
 *
 * @author Salman
 *
 * First message is request by the client asking the RS to send the attached message
 * to the remote client
 * second message is a message sent by the server to the client to deliver the
 * relay message
 */
public class RelayRequestMsg
{

    public final static class ClientToServer extends HpMsg
    {

        static final long serialVersionUID = 1L;
        private final Encodable message;

        public ClientToServer(VodAddress src, VodAddress dest,
                int remoteClientID, VodMsg msg)
        {
            super(src, dest, remoteClientID, msg.getTimeoutId());
            if (msg instanceof Encodable == false) {
                throw new IllegalArgumentException("Message not encodable of type: "
                        + msg.getClass());
            }
            this.message = (Encodable) msg;
        }

        public VodMsg getMessage()
        {
            return (VodMsg) message;
        }

  
        @Override
        public int getSize() {
            VodMsg msg = (VodMsg) message;
            return getHeaderSize()
                    + msg.getSize();
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.RELAY_CLIENT_TO_SERVER;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            ChannelBuffer msgBuffer = message.toByteArray();
            byte[] bytes = msgBuffer.array();
            buffer.writeBytes(bytes);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            RelayRequestMsg.ClientToServer copy = new 
                    RelayRequestMsg.ClientToServer(vodSrc, vodDest, remoteClientId, this);
            copy.setTimeoutId(timeoutId);
            return copy;
        }

    };

     public final static class ServerToClient extends HpMsg
    {

        static final long serialVersionUID = 1L;
        private final Encodable message;

        public ServerToClient(VodAddress src, VodAddress dest,
                int remoteClientId, VodMsg msg)
        {
            super(src, dest, remoteClientId, msg.getTimeoutId());
            this.message = (Encodable) msg;
        }

        public VodMsg getMessage()
        {
            return (VodMsg) message;
        }

        @Override
        public int getSize() {
            VodMsg msg = (VodMsg) message;
            return getHeaderSize()
                    + msg.getSize();
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.RELAY_SERVER_TO_CLIENT;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            ChannelBuffer msgBuffer = message.toByteArray();
            // Could use ByteBuffer instead of byte[], but it's not
            // guaranteed to be same buffer as used in ChannelBuffer
            byte[] bytes = msgBuffer.array();
            buffer.writeBytes(bytes);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            return new RelayRequestMsg.ServerToClient(vodSrc, vodDest, 
                    remoteClientId, this);
        }


    };
}
