/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.croupier.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.DescriptorBuffer;
import se.sics.gvod.common.VodDescriptor;
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


    public static class Request extends RelayMsgNetty.Request {

        private static final long serialVersionUID = 8493601671018888143L;
        private final DescriptorBuffer buffer;
        private final VodDescriptor desc;

        public Request(VodAddress source,
                VodAddress destination, DescriptorBuffer buffer,
                VodDescriptor desc) {
            super(source, destination, source.getId(), destination.getId());
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
            return super.getSize()
                    + UserTypesEncoderFactory.getDescriptorBufferSize(buffer)
                    + UserTypesEncoderFactory.GVOD_NODE_DESCRIPTOR_LEN
                    + 1 // numSamples
                    ;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buf = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeDescriptorBuffer(buf, buffer);
            UserTypesEncoderFactory.writeVodNodeDescriptor(buf, desc);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            ShuffleMsg.Request copy = new ShuffleMsg.Request(vodSrc, vodDest, buffer,
                    desc);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

    public static class Response extends RelayMsgNetty.Response {

        //<editor-fold defaultstate="collapsed" desc="comment">
        //</editor-fold>
        private static final long serialVersionUID = -5022051054665787770L;
        private final DescriptorBuffer buffer;
        private final VodDescriptor desc;

        public Response(VodAddress source, VodAddress destination,
                int clientId, int remoteId,
                VodAddress nextDest, TimeoutId timeoutId,
                RelayMsgNetty.Status status, DescriptorBuffer buffer,
                VodDescriptor desc) {
            super(source, destination, clientId, remoteId, nextDest, timeoutId, status);
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
            return super.getSize()
                    + UserTypesEncoderFactory.getDescriptorBufferSize(buffer)
                    + UserTypesEncoderFactory.GVOD_NODE_DESCRIPTOR_LEN
                    + 1
                    ;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buf = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeDescriptorBuffer(buf, buffer);
            UserTypesEncoderFactory.writeVodNodeDescriptor(buf, desc);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new ShuffleMsg.Response(vodSrc, vodDest, clientId, remoteId,
                    nextDest, timeoutId, getStatus(), buffer, desc);
        }
    }

    public static class RequestTimeout extends RewriteableRetryTimeout {

        private final VodAddress peer;

        public RequestTimeout(ScheduleRetryTimeout timout, Request request) {
            super(timout, request);
            this.peer = request.getVodDestination();
        }

        public VodAddress getPeer() {
            return peer;
        }
    }
}
