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
import se.sics.gvod.timer.TimeoutId;

/**
 * TEST_3 involves sending req to StunServer1 who replies over a
 * different socket bound on a different port.
 * @author jdowling
 */
public class EchoChangePortMsg {

    public final static class Request extends StunRequestMsg {

        static final long serialVersionUID = 1L;

        public Request(VodAddress src, VodAddress dest,
                long transactionId) {
            super(src, dest, transactionId);
        }
        
        @Override
        public int getSize() {
            return getHeaderSize();
        }
        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            return buffer;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.ECHO_CHANGE_PORT_REQUEST;
        }

        @Override
        public RewriteableMsg copy() {
            EchoChangePortMsg.Request copy = new EchoChangePortMsg.Request(vodSrc, vodDest, transactionId);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

    public final static class Response extends StunResponseMsg {

        static final long serialVersionUID = 1L;

        public Response(VodAddress src, VodAddress dest,
                long transactionId, TimeoutId timeoutId) {
            super(src, dest, dest.getPeerAddress(), transactionId, timeoutId);
        }

        public Response(VodAddress src, VodAddress dest, Address clientPublicIp,
                long transactionId, TimeoutId timeoutId) {
            super(src, dest, clientPublicIp, transactionId, timeoutId);
        }

        @Override
        public int getSize() {
            return getHeaderSize();
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            return buffer;
        }

        @Override
        public byte getOpcode() {
           return BaseMsgFrameDecoder.ECHO_CHANGE_PORT_RESPONSE;
        }

        @Override
        public RewriteableMsg copy() {
            return new EchoChangePortMsg.Response(vodSrc, vodDest, 
                    replyAddr, transactionId, timeoutId);
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
