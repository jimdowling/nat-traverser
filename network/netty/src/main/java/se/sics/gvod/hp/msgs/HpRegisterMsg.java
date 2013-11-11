package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Set;

import se.sics.gvod.common.msgs.DirectMsgNetty;
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
 * @author salman, jim
 */
public class HpRegisterMsg {

    public static final class Request extends DirectMsgNetty.SystemRequest {

        static final long serialVersionUID = 62456664L;
        private final long rtt;
        private final Set<Integer> prpPorts;

        public Request(VodAddress src, VodAddress dest, long rtt) {
            this(src, dest, rtt, null);
        }

        public Request(VodAddress src, VodAddress dest, long rtt,
                Set<Integer> prpPorts) {
            super(src, dest);
            this.rtt = rtt;
            this.prpPorts = (prpPorts == null) ? new HashSet<Integer>() : prpPorts;
        }

        public Request(Request msg, VodAddress src, long rtt) {
            super(src, msg.getVodDestination());
            this.rtt = rtt;
            this.prpPorts = msg.getPrpPorts();
        }

        public Set<Integer> getPrpPorts() {
            return prpPorts;
        }

        public long getRtt() {
            return rtt;
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + 1 /*delta */
                    + 2 /* rtt */
                    + 1 + (prpPorts == null ? 0 : prpPorts.size() * 4) /* prpPorts */;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.HP_REGISTER_REQUEST;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, (int) rtt);
            UserTypesEncoderFactory.writeSetUnsignedTwoByteInts(buffer, prpPorts);
            return buffer;

        }

        @Override
        public RewriteableMsg copy() {
            HpRegisterMsg.Request copy =
                    new HpRegisterMsg.Request(this, vodSrc, rtt);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

    public enum RegisterStatus {

        ACCEPT, REJECT, ALREADY_REGISTERED, BETTER_CHILD, BETTER_PARENT, 
        PARENT_EXITING, CHILD_EXITING,
        NOT_CHILD, DEAD_PARENT, PARENT_REQUEST_FAILED;
    };

    public final static class Response extends DirectMsgNetty.SystemResponse {

        static final long serialVersionUID = 987545675L;
        private final RegisterStatus responseType;
        private final Set<Integer> prpPorts;

        public Response(VodAddress src, VodAddress dest, RegisterStatus responseType,
                TimeoutId timeoutId, Set<Integer> prpPorts) {
            super(src, dest, timeoutId);
            this.responseType = responseType;
            this.prpPorts = (prpPorts == null) ? new HashSet<Integer>() : prpPorts;
        }

        public Set<Integer> getPrpPorts() {
            return prpPorts;
        }

        public RegisterStatus getResponseType() {
            return responseType;
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + 1
                    + 1 + prpPorts.size() * 4;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.HP_REGISTER_RESPONSE;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, responseType.ordinal());
            UserTypesEncoderFactory.writeSetUnsignedTwoByteInts(buffer, prpPorts);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            return new HpRegisterMsg.Response(vodSrc, vodDest, responseType, timeoutId,
                    prpPorts);
        }
    }

    public static final class RequestRetryTimeout extends RewriteableRetryTimeout {

        public RequestRetryTimeout(ScheduleRetryTimeout st, HpRegisterMsg.Request requestMsg) {
            super(st, requestMsg, requestMsg.getVodSource().getOverlayId());
        }

        public HpRegisterMsg.Request getRequest() {
            return (HpRegisterMsg.Request) getMsg();
        }
    }
}
