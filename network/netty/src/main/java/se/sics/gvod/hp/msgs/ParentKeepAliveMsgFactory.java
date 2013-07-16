package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;

public class ParentKeepAliveMsgFactory {

    public static class Request extends DirectMsgNettyFactory {

        private Request() {
        }

        public static ParentKeepAliveMsg.Ping fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (ParentKeepAliveMsg.Ping)
                    new ParentKeepAliveMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected ParentKeepAliveMsg.Ping process(ByteBuf buffer) throws MessageDecodingException {
            return new ParentKeepAliveMsg.Ping(vodSrc, vodDest);
        }
    }

    public static class Response extends DirectMsgNettyFactory {

        private Response() {
        }

        public static ParentKeepAliveMsg.Pong fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (ParentKeepAliveMsg.Pong)
                    new ParentKeepAliveMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected ParentKeepAliveMsg.Pong process(ByteBuf buffer) throws MessageDecodingException {

            return new ParentKeepAliveMsg.Pong(vodSrc, vodSrc, timeoutId);
        }

    }
}
