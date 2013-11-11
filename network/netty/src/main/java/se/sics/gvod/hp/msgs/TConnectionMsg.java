package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNetty;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 * This class uses the ObjectDecoder/Encoder and is only used for testing.
 *
 * @author salman
 */
public class TConnectionMsg {

    public static final class Ping extends DirectMsgNetty.Request {

        static final long serialVersionUID = 1L;

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    ;
        }

        public Ping(VodAddress src, VodAddress dest,
                Transport protocol, TimeoutId timeoutId) {
            super(src, dest, protocol, timeoutId);
        }

        public Ping(VodAddress src, VodAddress dest,
                TimeoutId timeoutId) {
            super(src, dest, timeoutId);
        }

        private Ping(Ping msg, VodAddress src) {
            super(src, msg.getVodDestination());
        }

        private Ping(VodAddress dest, Ping msg) {
            super(msg.getVodSource(), dest);
        }

        @Override
        public RewriteableMsg copy() {
            TConnectionMsg.Ping copy = new TConnectionMsg.Ping(vodSrc, vodDest,
                    timeoutId);
            copy.setTimeoutId(timeoutId);
            return copy;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            return createChannelBufferWithHeader();
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PING;
        }
    }

    public final static class Pong extends DirectMsgNetty.Response {

        static final long serialVersionUID = 1L;

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    ;
        }

        public Pong(VodAddress src, VodAddress dest,
                Transport protocol, TimeoutId timeoutId) {
            super(src, dest, protocol, timeoutId);
        }

        public Pong(VodAddress src, VodAddress dest, TimeoutId timeoutId) {
            super(src, dest);
            setTimeoutId(timeoutId);
        }

        private Pong(Pong msg, VodAddress src) {
            super(src, msg.getVodDestination());
            setTimeoutId(msg.getTimeoutId());
        }

        private Pong(VodAddress dest, Pong msg) {
            super(msg.getVodSource(), dest);
            setTimeoutId(msg.getTimeoutId());
        }

        @Override
        public RewriteableMsg copy() {
            return new TConnectionMsg.Pong(vodSrc, vodDest, timeoutId);
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            return createChannelBufferWithHeader();
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PONG;
        }
    }
    
    public final static class Pang extends DirectMsgNetty.Oneway {

        static final long serialVersionUID = 1L;

        private final TimeoutId msgTimeoutId;
        @Override
        public int getSize() {
            return super.getHeaderSize()
            + 4;
        }

        public Pang(VodAddress src, VodAddress dest,
                Transport protocol, TimeoutId msgTimeoutId) {
            super(src, dest, protocol);
            this.msgTimeoutId = msgTimeoutId;
        }

        public Pang(VodAddress src, VodAddress dest, TimeoutId msgTimeoutId) {
            this(src, dest, Transport.UDP, msgTimeoutId);
        }

        private Pang(Pang msg, VodAddress src) {
            super(src, msg.getVodDestination());
            this.msgTimeoutId = msg.getMsgTimeoutId();
        }

        private Pang(VodAddress dest, Pang msg) {
            super(msg.getVodSource(), dest);
            this.msgTimeoutId = msg.getMsgTimeoutId();
        }

        public TimeoutId getMsgTimeoutId() {
            return msgTimeoutId;
        }

        @Override
        public RewriteableMsg copy() {
            return new TConnectionMsg.Pang(vodSrc, vodDest, protocol, msgTimeoutId);
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            ByteBuf b = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeTimeoutId(b, msgTimeoutId);
            return b;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PANG;
        }
    }

    public static final class RequestRetryTimeout extends RewriteableRetryTimeout {

        private final Ping requestMsg;

        public RequestRetryTimeout(ScheduleRetryTimeout st, Ping requestMsg) {
            super(st, requestMsg, requestMsg.getVodSource().getOverlayId());
            this.requestMsg = requestMsg;
        }

        public Ping getRequestMsg() {
            return requestMsg;
        }
    }
}
