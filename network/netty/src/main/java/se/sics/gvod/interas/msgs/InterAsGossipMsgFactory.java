package se.sics.gvod.interas.msgs;

import se.sics.gvod.common.VodDescriptor;
import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.RelayMsgNettyFactory;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class InterAsGossipMsgFactory {

    public static class Request extends RelayMsgNettyFactory.Request {

        Request() {
        }

        public static InterAsGossipMsg.Request fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (InterAsGossipMsg.Request) 
                    new InterAsGossipMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            return new InterAsGossipMsg.Request(gvodSrc, gvodDest, timeoutId);
        }
    }

    public static class Response extends RelayMsgNettyFactory.Response {

        private Response() {
        }

        public static InterAsGossipMsg.Response fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (InterAsGossipMsg.Response)
                    new InterAsGossipMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            List<VodDescriptor> asNeighbours = UserTypesDecoderFactory.readListGVodNodeDescriptors(buffer);
            return new InterAsGossipMsg.Response(gvodSrc, gvodDest, nextDest, timeoutId, 
                    asNeighbours);
        }

    }
};
