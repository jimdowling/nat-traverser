package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
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
 * @author jim
 */
public class HolePunchingMsg {

    public static final class Request extends HpMsg.Request {

        static final long serialVersionUID = 654322451L;

        private final int destport;
        
        public Request(VodAddress src, VodAddress dest,
                TimeoutId msgTimeoutId) {
            super(src, dest, dest.getId(), msgTimeoutId);
            this.destport = dest.getPort();
        }
        
        public Request(VodAddress src, VodAddress dest,
                TimeoutId msgTimeoutId, int destPort) {
            super(src, dest, dest.getId(), msgTimeoutId);
            this.destport = destPort;
        }

        private Request(Request msg, VodAddress src) {
            super(src, msg.getVodDestination(), msg.getRemoteClientId(),
                    msg.getMsgTimeoutId());
            this.destport = msg.getVodDestination().getPort();
        }

        public int getDestport() {
            return destport;
        }
        
        @Override
        public int getSize() {
            return getHeaderSize()
                    + 2 // destPort
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.HOLE_PUNCHING_REQUEST;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
                UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, destport);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            HolePunchingMsg.Request copy = new HolePunchingMsg.Request(vodSrc, vodDest,
                    msgTimeoutId, destport);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

        public final static class Response extends HpMsg.Response {

        static final long serialVersionUID = 1L;

        private final TimeoutId srcTimeoutId;
        public Response(VodAddress src, VodAddress dest,  
                TimeoutId srcTimeoutId, TimeoutId msgTimeoutId) {
            super(src, dest, dest.getId(), msgTimeoutId);
            this.srcTimeoutId = srcTimeoutId;
        }

        public TimeoutId getSrcTimeoutId() {
            return srcTimeoutId;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.HOLE_PUNCHING_RESPONSE;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
                UserTypesEncoderFactory.writeTimeoutId(buffer, srcTimeoutId);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            HolePunchingMsg.Response r = new HolePunchingMsg.Response(vodSrc, vodDest, 
                    msgTimeoutId, srcTimeoutId);
            r.setTimeoutId(timeoutId);
            return r;
        }
    }

    public static final class ResponseAck extends HpMsg.Response {

        static final long serialVersionUID = 445522451L;

        public ResponseAck(VodAddress src, VodAddress dest, TimeoutId timeoutId,
                TimeoutId msgTimeoutId) {
            super(src, dest, timeoutId, src.getId(), msgTimeoutId);
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.HOLE_PUNCHING_REQUEST;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            HolePunchingMsg.ResponseAck copy = 
                    new HolePunchingMsg.ResponseAck(vodSrc, vodDest, timeoutId,
                    msgTimeoutId);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }    
    
    
    public static final class RequestRetryTimeout extends RewriteableRetryTimeout {

        private final Request requestMsg;

        public RequestRetryTimeout(ScheduleRetryTimeout st, Request requestMsg) {
            super(st, requestMsg);
            this.requestMsg = requestMsg;
        }

        public Request getRequestMsg() {
            return requestMsg;
        }
    }
    public static final class ResponseRetryTimeout extends RewriteableRetryTimeout {

        private final Response responseMsg;

        public ResponseRetryTimeout(ScheduleRetryTimeout st, Response ResponseMsg) {
            super(st, ResponseMsg);
            this.responseMsg = ResponseMsg;
        }

        public Response getResponseMsg() {
            return responseMsg;
        }
    }
}
