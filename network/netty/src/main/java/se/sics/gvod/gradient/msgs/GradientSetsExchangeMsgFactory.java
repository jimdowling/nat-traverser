package se.sics.gvod.gradient.msgs;

import se.sics.gvod.common.VodDescriptor;
import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.RelayMsgNettyFactory;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class GradientSetsExchangeMsgFactory {

    public static class Request extends RelayMsgNettyFactory.Request {

        Request() {
        }

        public static GradientSetsExchangeMsg.Request fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (GradientSetsExchangeMsg.Request) 
                    new GradientSetsExchangeMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            return new GradientSetsExchangeMsg.Request(gvodSrc, gvodDest, clientId, remoteId, timeoutId);
        }
    }

    public static class Response extends RelayMsgNettyFactory.Response {

        private Response() {
        }

        public static GradientSetsExchangeMsg.Response fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (GradientSetsExchangeMsg.Response)
                    new GradientSetsExchangeMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            List<VodDescriptor> similarSet = UserTypesDecoderFactory.readListGVodNodeDescriptors(buffer);
            return new GradientSetsExchangeMsg.Response(gvodSrc, gvodDest, nextDest, timeoutId, 
                    similarSet);
        }

    }
};
