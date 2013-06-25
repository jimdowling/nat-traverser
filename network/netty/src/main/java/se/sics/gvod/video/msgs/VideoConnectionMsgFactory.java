package se.sics.gvod.video.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.VodMsgNettyFactory;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class VideoConnectionMsgFactory {

    public static class Request extends VodMsgNettyFactory {

        Request() {
        }

        public static VideoConnectionMsg.Request fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (VideoConnectionMsg.Request) new VideoConnectionMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            boolean randomRequest = UserTypesDecoderFactory.readBoolean(buffer);
            return new VideoConnectionMsg.Request(vodSrc, vodDest, timeoutId, randomRequest);
        }
    }

    public static class Response extends VodMsgNettyFactory {

        private Response() {
        }

        public static VideoConnectionMsg.Response fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (VideoConnectionMsg.Response) new VideoConnectionMsgFactory.Response().decode(buffer, false);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            boolean randomRequest = UserTypesDecoderFactory.readBoolean(buffer);
            boolean acceptConnection = UserTypesDecoderFactory.readBoolean(buffer);
            return new VideoConnectionMsg.Response(vodSrc, vodDest, timeoutId, randomRequest,
                    acceptConnection);
        }
    }

    public static class Disconnect extends VodMsgNettyFactory {

        private Disconnect() {
        }

        public static VideoConnectionMsg.Disconnect fromBuffer(ChannelBuffer buffer) throws MessageDecodingException {
            return (VideoConnectionMsg.Disconnect) new VideoConnectionMsgFactory.Disconnect().decode(buffer, false);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            boolean randomConnection = UserTypesDecoderFactory.readBoolean(buffer);
            return new VideoConnectionMsg.Disconnect(vodSrc, vodDest, timeoutId, randomConnection);
        }
    }
};
