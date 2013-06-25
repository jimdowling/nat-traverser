package se.sics.gvod.gradient.msgs;

import se.sics.gvod.common.VodDescriptor;
import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.RelayMsgNettyFactory;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class SetsExchangeMsgFactory {

    public static class Request extends RelayMsgNettyFactory.Request {

        Request() {
        }

        public static SetsExchangeMsg.Request fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (SetsExchangeMsg.Request) 
                    new SetsExchangeMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            return new SetsExchangeMsg.Request(gvodSrc, gvodDest, clientId, remoteId, timeoutId);
        }
    }

    public static class Response extends RelayMsgNettyFactory.Response {

        private Response() {
        }

        public static SetsExchangeMsg.Response fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (SetsExchangeMsg.Response)
                    new SetsExchangeMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            List<VodDescriptor> utilitySet = UserTypesDecoderFactory.readListGVodNodeDescriptors(buffer);
            List<VodDescriptor> upperSet = UserTypesDecoderFactory.readListGVodNodeDescriptors(buffer);
            return new SetsExchangeMsg.Response(gvodSrc, gvodDest, clientId, remoteId, nextDest, timeoutId, 
                    utilitySet, upperSet);
        }

    }
};
