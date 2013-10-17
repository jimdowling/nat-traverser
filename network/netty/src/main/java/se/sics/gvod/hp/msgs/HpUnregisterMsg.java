package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNetty;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.hp.msgs.HpRegisterMsg.RegisterStatus;
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
public class HpUnregisterMsg {

    public static final class Request extends DirectMsgNetty.Request {

        static final long serialVersionUID = 44574788L;
        private final int delay;
        private final RegisterStatus status;

        public Request(VodAddress src, VodAddress dest, int delay,
                RegisterStatus status) {
            super(src, dest);
            this.delay = delay;
            this.status = status;
        }

        public Request(Request msg, VodAddress src, int delay,
                RegisterStatus status) {
            super(src, msg.getVodDestination());
            this.delay = delay;
            this.status = status;
            this.timeoutId = msg.getTimeoutId();
        }

        public RegisterStatus getStatus() {
            return status;
        }

        public int getDelay() {
            return delay;
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + 16
                    + 1;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.HP_UNREGISTER_REQUEST;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            ByteBuf buffer = createChannelBufferWithHeader();
            buffer.writeInt(delay);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, status.ordinal());
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
             HpUnregisterMsg.Request hum = new HpUnregisterMsg.Request(this, vodSrc, delay, status);
             hum.setTimeoutId(timeoutId);
             return hum;
        }
    }

    public final static class Response extends DirectMsgNetty.Response {

        static final long serialVersionUID = 12453454251L;

        public static enum Status {

            SUCCESS, NOT_REGISTERED, FAIL, DELAY_LESS_THAN_ZERO,
            ALREADY_MOVING
        };
        private final Status status;

        public Response(VodAddress src, VodAddress dest, Status status,
                TimeoutId timeoutID) {
            super(src, dest, timeoutID);
            this.status = status;
            if (timeoutId == null) {
                throw new NullPointerException("TimeoutId was null");
            }

        }

        public Status getStatus() {
            return status;
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + 1;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.HP_UNREGISTER_RESPONSE;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    status.ordinal());
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            return new HpUnregisterMsg.Response(vodSrc, vodDest, status, timeoutId);
        }
    }

    public static final class RequestRetryTimeout extends RewriteableRetryTimeout {

        private final Request requestMsg;
        private final Request unregisterFromRendezvousServerRequest;

        public RequestRetryTimeout(ScheduleRetryTimeout st, Request requestMsg, Request registerWithRendezvousServerRequest) {
            super(st, requestMsg);
            this.requestMsg = requestMsg;
            this.unregisterFromRendezvousServerRequest = registerWithRendezvousServerRequest;
        }

        public Request getUnregisterFromRendezvousServerRequest() {
            return unregisterFromRendezvousServerRequest;
        }

        public Request getRequestMsg() {
            return requestMsg;
        }
    }
}
