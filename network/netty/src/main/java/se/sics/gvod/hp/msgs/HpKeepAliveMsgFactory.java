package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;

public class HpKeepAliveMsgFactory {

    public static class Request extends DirectMsgNettyFactory.Request {

        private Request() {
        }

        public static HpKeepAliveMsg.Ping fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (HpKeepAliveMsg.Ping)
                    new HpKeepAliveMsgFactory.Request().decode(buffer);
        }

        @Override
        protected HpKeepAliveMsg.Ping process(ByteBuf buffer) throws MessageDecodingException {
            return new HpKeepAliveMsg.Ping(vodSrc, vodDest);
        }
    }

    public static class Response extends DirectMsgNettyFactory.Response {

        private Response() {
        }

        public static HpKeepAliveMsg.Pong fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (HpKeepAliveMsg.Pong)
                    new HpKeepAliveMsgFactory.Response().decode(buffer);
        }

        @Override
        protected HpKeepAliveMsg.Pong process(ByteBuf buffer) throws MessageDecodingException {

            return new HpKeepAliveMsg.Pong(vodSrc, vodDest, timeoutId);
        }

    }
}
