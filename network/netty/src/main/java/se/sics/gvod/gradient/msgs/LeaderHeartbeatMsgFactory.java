package se.sics.gvod.gradient.msgs;

import se.sics.gvod.common.VodDescriptor;
import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.Utility;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.RelayMsgNettyFactory;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class LeaderHeartbeatMsgFactory {

    public static class Request extends RelayMsgNettyFactory.Request {

        Request() {
        }

        public static GradientSearchMsg.Request fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (GradientSearchMsg.Request) 
                    new LeaderHeartbeatMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            int ttl = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            Utility targetUtility = UserTypesDecoderFactory.readUtility(buffer);
            VodAddress origSrc = UserTypesDecoderFactory.readVodAddress(buffer);
            return new GradientSearchMsg.Request(gvodSrc, gvodDest, origSrc,
                    timeoutId, targetUtility, ttl);
        }
    }

    public static class Response extends RelayMsgNettyFactory.Response {

        private Response() {
        }

        public static GradientSearchMsg.Response fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (GradientSearchMsg.Response)
                    new LeaderHeartbeatMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            List<VodDescriptor> similarSet = UserTypesDecoderFactory.readListGVodNodeDescriptors(buffer);
            return new GradientSearchMsg.Response(gvodSrc, gvodDest, clientId, remoteId, nextDest, timeoutId, 
                    similarSet);
        }

    }
};
