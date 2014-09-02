/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.croupier.msgs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.sics.gvod.common.DescriptorBuffer;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.msgs.DirectMsgNetty;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.RelayMsgNetty;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 */
public class ShuffleMsg {

    public static class Request extends DirectMsgNetty.Request {

        private static final long serialVersionUID = 8493601671018888143L;
        private final DescriptorBuffer buffer;
        private final VodDescriptor desc;

        public Request(VodAddress source,
                VodAddress destination, DescriptorBuffer buffer,
                VodDescriptor desc) {
            super(source, destination);
            this.buffer = buffer;
            this.desc = desc;
        }

        public DescriptorBuffer getBuffer() {
            return buffer;
        }

        public VodDescriptor getDesc() {
            return desc;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.SHUFFLE_REQUEST;
        }

        @Override
        public int getSize() {
            return getHeaderSize();
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            ByteBuf buf = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeDescriptorBuffer(buf, buffer);
            UserTypesEncoderFactory.writeVodNodeDescriptor(buf, desc);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            ShuffleMsg.Request copy = new ShuffleMsg.Request(vodSrc, vodDest, buffer, desc);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

    public static class Response extends DirectMsgNetty.Response {

        //<editor-fold defaultstate="collapsed" desc="comment">
        //</editor-fold>
        private static final long serialVersionUID = -5022051054665787770L;
        private final DescriptorBuffer buffer;
        private final VodDescriptor desc;

        public Response(VodAddress source, VodAddress destination, TimeoutId timeoutId,DescriptorBuffer buffer,
                VodDescriptor desc) {
            super(source, destination, timeoutId);
            assert (source.equals(destination) == false);
            this.buffer = buffer;
            this.desc = desc;
        }

        public DescriptorBuffer getBuffer() {
            return buffer;
        }

        public VodDescriptor getDesc() {
            return desc;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.SHUFFLE_RESPONSE;
        }

        @Override
        public int getSize() {
            return getHeaderSize();
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            ByteBuf buf = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeDescriptorBuffer(buf, buffer);
            UserTypesEncoderFactory.writeVodNodeDescriptor(buf, desc);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new ShuffleMsg.Response(vodSrc, vodDest, timeoutId, buffer, desc);
        }
    }

    public static class RequestTimeout extends RewriteableRetryTimeout {

        private final VodAddress peer;

        public RequestTimeout(ScheduleRetryTimeout timout, Request request, int overlayId) {
            super(timout, request, overlayId);
            this.peer = request.getVodDestination();
        }

        public VodAddress getPeer() {
            return peer;
        }
    }
}
