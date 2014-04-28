/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public class ServerHostChangeMsg {

    /**
     * This test involves sending req to StunServer1 who delegates to
     * StunServer2 who sends response.
     */
    public final static class Request extends StunRequestMsg {

        static final long serialVersionUID = 1L;
        private final Address clientPublicAddr;
        private final TimeoutId originalTimeoutId;

        public Request(VodAddress src, VodAddress dest,
                Address clientPublicAddr,
                long transactionId, TimeoutId originalTimeoutId) {
            super(src, dest, transactionId);
            this.clientPublicAddr = clientPublicAddr;
            this.originalTimeoutId = originalTimeoutId;
        }
        public Request(VodAddress src, VodAddress dest,
                Address clientPublicAddr,
                long transactionId, TimeoutId timeoutId, TimeoutId originalTimeoutId) {
            super(src, dest, transactionId, timeoutId);
            this.clientPublicAddr = clientPublicAddr;
            this.originalTimeoutId = originalTimeoutId;
        }

        public TimeoutId getOriginalTimeoutId() {
            return originalTimeoutId;
        }

        public Address getClientPublicAddr() {
            return clientPublicAddr;
        }


        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + UserTypesEncoderFactory.ADDRESS_LEN
                    + 4 /* timeoutId */
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.SERVER_HOST_CHANGE_REQUEST;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buf = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeAddress(buf, clientPublicAddr);
//            UserTypesEncoderFactory.writeTimeoutId(buf, originalTimeoutId);
            buf.writeInt(originalTimeoutId.getId());
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new ServerHostChangeMsg.Request(vodSrc, 
                    vodDest, clientPublicAddr, transactionId, timeoutId, originalTimeoutId);
        }
    }

    /**
     * This test involves sending req to StunServer1 who delegates to
     * StunServer2 who sends response.
     */
    public static final class Response extends StunResponseMsg {

        static final long serialVersionUID = 14245245245L;

        public Response(VodAddress src, VodAddress dest,
                long transactionId, TimeoutId timeoutId) {
            super(src, dest, src.getPeerAddress(), transactionId, timeoutId);
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.SERVER_HOST_CHANGE_RESPONSE;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buf = createChannelBufferWithHeader();

            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new ServerHostChangeMsg.Response(vodSrc, vodDest, transactionId, timeoutId);
        }
    }

    public static final class RequestTimeout extends RewriteableRetryTimeout {
        
        public RequestTimeout(ScheduleRetryTimeout request, 
                ServerHostChangeMsg.Request requestMsg) {
            super(request, requestMsg, requestMsg.getVodSource().getOverlayId());
        }

    }
}
