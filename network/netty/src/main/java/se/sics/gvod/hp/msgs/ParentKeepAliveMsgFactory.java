package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;

public class ParentKeepAliveMsgFactory {

    public static class Request extends VodMsgNettyFactory {

        private Request() {
        }

        public static ParentKeepAliveMsg.Ping fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (ParentKeepAliveMsg.Ping)
                    new ParentKeepAliveMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected ParentKeepAliveMsg.Ping process(ChannelBuffer buffer) throws MessageDecodingException {
            return new ParentKeepAliveMsg.Ping(vodSrc, vodDest);
        }
    }

    public static class Response extends VodMsgNettyFactory {

        private Response() {
        }

        public static ParentKeepAliveMsg.Pong fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (ParentKeepAliveMsg.Pong)
                    new ParentKeepAliveMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected ParentKeepAliveMsg.Pong process(ChannelBuffer buffer) throws MessageDecodingException {

            return new ParentKeepAliveMsg.Pong(vodSrc, vodSrc, timeoutId);
        }

    }
}
