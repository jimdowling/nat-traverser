/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
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

    public static class Request extends VodMsgNetty {

        public Request(VodAddress source, VodAddress destination) {
            super(source, destination);
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.DISCONNECT_REQUEST;
        }

        @Override
        public int getSize() {
            return getHeaderSize();
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buf = createChannelBufferWithHeader();
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            Request r = new Request(vodSrc, vodDest);
            r.setTimeoutId(timeoutId);
            return r;
        }
    }

    public static class Response extends VodMsgNetty {

        private final int ref;

        public Response(VodAddress source, VodAddress destination, TimeoutId timeoutId, int ref) {
            super(source, destination, timeoutId);
            this.ref = ref;
        }

        public int getRef() {
            return ref;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.DISCONNECT_RESPONSE;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 2 /* refs */;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
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

        public RequestTimeout(ScheduleRetryTimeout request, Request msg) {
            super(request, msg);
            this.peer = msg.getVodDestination();
        }

        public VodAddress getPeer() {
            return peer;
        }
    }
}
