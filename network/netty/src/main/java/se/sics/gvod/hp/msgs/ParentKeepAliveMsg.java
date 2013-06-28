package se.sics.gvod.hp.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.common.msgs.VodMsgNetty;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.timer.TimeoutId;

/**
 * 
 * @author salman
 */
public class ParentKeepAliveMsg {

    public static final class Ping extends VodMsgNetty {

        static final long serialVersionUID = 12342356463L;
        
        @Override
        public int getSize() {
            return getHeaderSize();
        }

        public Ping(VodAddress src, VodAddress dest) {
            super(src, dest);
        }

        public int getClientId() {
            return vodSrc.getId();
        }
        
        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PARENT_KEEP_ALIVE_REQUEST;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            return createChannelBufferWithHeader();
        }

        @Override
        public RewriteableMsg copy() {
            ParentKeepAliveMsg.Ping copy = new ParentKeepAliveMsg.Ping(vodSrc, vodDest);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
        
    }

    public final static class Pong extends VodMsgNetty {

        static final long serialVersionUID = 6624678722345L;
        
        @Override
        public int getSize() {
            return getHeaderSize();
        }

        public Pong(VodAddress src, VodAddress dest, TimeoutId timeoutId) {
            super(src, dest, timeoutId);
        }

         @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PARENT_KEEP_ALIVE_RESPONSE;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            return createChannelBufferWithHeader();
        }

        @Override
        public RewriteableMsg copy() {
            return new ParentKeepAliveMsg.Pong(vodSrc, vodDest, timeoutId);
        }

    }

    public static final class PingTimeout extends RewriteableRetryTimeout {

        private final Ping requestMsg;

        public PingTimeout(ScheduleRetryTimeout st, Ping requestMsg) {
            super(st, requestMsg);
            this.requestMsg = requestMsg;
        }

        public Ping getRequestMsg() {
            return requestMsg;
        }
    }
}
