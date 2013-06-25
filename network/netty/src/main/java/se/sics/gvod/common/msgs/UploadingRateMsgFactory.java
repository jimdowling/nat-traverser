package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class UploadingRateMsgFactory {

    public static class Request extends VodMsgNettyFactory {

        private Request() {
        }

        public static UploadingRateMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (UploadingRateMsg.Request)
                    new UploadingRateMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            VodAddress target = UserTypesDecoderFactory.readVodAddress(buffer);
            return new UploadingRateMsg.Request(vodSrc, vodDest, timeoutId, target);
        }
    }

    public static class Response extends VodMsgNettyFactory {

        private Response() {
        }

        public static UploadingRateMsg.Response fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (UploadingRateMsg.Response)
                    new UploadingRateMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            VodAddress target = UserTypesDecoderFactory.readVodAddress(buffer);
            int rate = buffer.readInt();
            return  new UploadingRateMsg.Response(vodSrc, vodDest, timeoutId,  target, rate);
        }
    }
};
