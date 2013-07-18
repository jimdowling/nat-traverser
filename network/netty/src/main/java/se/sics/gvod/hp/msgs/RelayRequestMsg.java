/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.Encodable;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.msgs.RewriteableMsg;

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

    public final static class ClientToServer extends HpMsg.Request
    {

        static final long serialVersionUID = 1L;
        private final Encodable message;

        public ClientToServer(VodAddress src, VodAddress dest,
                int remoteClientID, DirectMsg msg)
        {
            super(src, dest, remoteClientID, msg.getTimeoutId());
            if (msg instanceof Encodable == false) {
                throw new IllegalArgumentException("Message not encodable of type: "
                        + msg.getClass());
            }
            this.message = (Encodable) msg;
        }

        public DirectMsg getMessage()
        {
            return (DirectMsg) message;
        }

  
        @Override
        public int getSize() {
            DirectMsg msg = (DirectMsg) message;
            return getHeaderSize()
                    + msg.getSize();
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.RELAY_CLIENT_TO_SERVER;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
        	ByteBuf msgBuffer = message.toByteArray();
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

     public final static class ServerToClient extends HpMsg.Response
    {

        static final long serialVersionUID = 1L;
        private final Encodable message;

        public ServerToClient(VodAddress src, VodAddress dest,
                int remoteClientId, DirectMsg msg)
        {
            super(src, dest, remoteClientId, msg.getTimeoutId());
            this.message = (Encodable) msg;
        }

        public DirectMsg getMessage()
        {
            return (DirectMsg) message;
        }

        @Override
        public int getSize() {
            DirectMsg msg = (DirectMsg) message;
            return getHeaderSize()
                    + msg.getSize();
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.RELAY_SERVER_TO_CLIENT;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
        	ByteBuf msgBuffer = message.toByteArray();
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
