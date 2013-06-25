package se.sics.gvod.hp.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.OpCode;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.timer.TimeoutId;

/**
 * 
 * @author jim
 */
public class HolePunchingMsg {

    public static final class Request extends HpMsg {

        static final long serialVersionUID = 654322451L;

        private final int zServerId;
        
        public Request(VodAddress src, VodAddress dest,
                int zServerId, TimeoutId msgTimeoutId) {
            super(src, dest, dest.getId(), msgTimeoutId);
            this.zServerId = zServerId;
        }

        private Request(Request msg, VodAddress src) {
            super(src, msg.getVodDestination(), msg.getRemoteClientId(),
                    msg.getMsgTimeoutId());
            this.zServerId = msg.getzServerId();
        }

        public int getzServerId() {
            return zServerId;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 4 // zServerId
                    ;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.HOLE_PUNCHING_REQUEST;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            buffer.writeInt(zServerId);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            HolePunchingMsg.Request copy = new HolePunchingMsg.Request(vodSrc, vodDest,
                    zServerId, msgTimeoutId);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

    public final static class Response extends HpMsg {

        static final long serialVersionUID = 1L;

        public Response(VodAddress src, VodAddress dest,  
                TimeoutId msgTimeoutId) {
            super(src, dest, dest.getId(), msgTimeoutId);
        }

        
        @Override
        public int getSize() {
            return getHeaderSize()
                    ;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.HOLE_PUNCHING_RESPONSE;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            HolePunchingMsg.Response r = new HolePunchingMsg.Response(vodSrc, vodDest, 
                    msgTimeoutId);
            r.setTimeoutId(timeoutId);
            return r;
        }
    }

    public static final class ResponseAck extends HpMsg {

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
        public OpCode getOpcode() {
            return OpCode.HOLE_PUNCHING_REQUEST;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
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
