/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;
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
public class DisconnectMsg {

    public static class Request extends DirectMsgNetty.Request {

        public Request(VodAddress source, VodAddress destination) {
            super(source, destination);
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.DISCONNECT_REQUEST;
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
            ;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            ByteBuf buf = createChannelBufferWithHeader();
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            Request r = new Request(vodSrc, vodDest);
            r.setTimeoutId(timeoutId);
            return r;
        }
    }

    public static class Response extends DirectMsgNetty.Response {

        private final int ref;

        public Response(VodAddress source, VodAddress destination, TimeoutId timeoutId, int ref) {
            super(source, destination, timeoutId);
            this.ref = ref;
        }

        public int getRef() {
            return ref;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.DISCONNECT_RESPONSE;
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + 2 /* refs */;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, ref);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            return new Response(vodSrc, vodDest, timeoutId, ref);
        }
    }

    public static class RequestTimeout extends RewriteableRetryTimeout {

        private final VodAddress peer;

        public RequestTimeout(ScheduleRetryTimeout request, Request requestMsg) {
            super(request, requestMsg, requestMsg.getVodSource().getOverlayId());
            this.peer = requestMsg.getVodDestination();
        }

        public VodAddress getPeer() {
            return peer;
        }
    }
}
