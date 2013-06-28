/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 * This test involves sending req to StunServer1 who delegates to
 * StunServer2 who sends response.
 * @author jdowling
 */
public class EchoChangeIpAndPortMsg {

    public static final class Request extends StunRequestMsg {

        static final long serialVersionUID = 1L;

        public Request(VodAddress src, VodAddress dest, long transactionId) {
            super(src, dest, transactionId);
        }

        @Override
        public int getSize() {
            return getHeaderSize();
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            return buffer;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.ECHO_CHANGE_IP_AND_PORT_REQUEST;
        }

        @Override
        public RewriteableMsg copy() {
            EchoChangeIpAndPortMsg.Request copy = new EchoChangeIpAndPortMsg.Request(vodSrc, vodDest, transactionId);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

    public final static class Response extends StunResponseMsg {

        public static enum Status {

            SUCCESS, FAIL
        };
        static final long serialVersionUID = 1L;
        private final Status status;

        public Response(VodAddress src, VodAddress dest,
                long transactionId, TimeoutId timeoutId) {
            this(src, dest, dest.getPeerAddress(), transactionId, timeoutId, Status.SUCCESS);
        }

        public Response(VodAddress src, VodAddress dest,
                long transactionId, TimeoutId timeoutId, Response.Status status) {
            this(src, dest, dest.getPeerAddress(), transactionId, timeoutId, status);
        }

        public Response(VodAddress src, VodAddress dest, Address clientPublicAddr,
                long transactionId, TimeoutId timeoutId) {
            this(src, dest, clientPublicAddr,
                    transactionId, timeoutId, Status.SUCCESS);
        }

        public Response(VodAddress src, VodAddress dest, Address clientPublicAddr,
                long transactionId, TimeoutId timeoutId, Response.Status status) {
            super(src, dest, clientPublicAddr, transactionId, timeoutId);
            this.status = status;
        }

        public Status getStatus() {
            return status;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 1 /*status*/
                    ;
        }
        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, status.ordinal());
            return buffer;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.ECHO_CHANGE_IP_AND_PORT_RESPONSE;
        }

        @Override
        public RewriteableMsg copy() {
            return new EchoChangeIpAndPortMsg.Response(vodSrc, vodDest, 
                    replyAddr, transactionId, timeoutId, status);
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
}
