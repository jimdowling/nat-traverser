package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class DisconnectMsgFactory {

    public static class Request extends DirectMsgNettyFactory {

        private Request() {
        }

        public static DisconnectMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (DisconnectMsg.Request)
                    new DisconnectMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected DirectMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            return new DisconnectMsg.Request(vodSrc, vodDest);
        }
    }

    public static class Response extends DirectMsgNettyFactory {

        private Response() {
        }

        public static DisconnectMsg.Response fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (DisconnectMsg.Response)
                    new DisconnectMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected DirectMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            int ref = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            return new DisconnectMsg.Response(vodSrc, vodDest, timeoutId, ref);
        }
    }
};
