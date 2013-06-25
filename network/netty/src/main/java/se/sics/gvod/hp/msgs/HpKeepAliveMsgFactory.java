package se.sics.gvod.hp.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.VodMsgNettyFactory;

public class HpKeepAliveMsgFactory {

    public static class Request extends VodMsgNettyFactory {

        private Request() {
        }

        public static HpKeepAliveMsg.Ping fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (HpKeepAliveMsg.Ping)
                    new HpKeepAliveMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected HpKeepAliveMsg.Ping process(ChannelBuffer buffer) throws MessageDecodingException {
            return new HpKeepAliveMsg.Ping(vodSrc, vodDest);
        }
    }

    public static class Response extends VodMsgNettyFactory {

        private Response() {
        }

        public static HpKeepAliveMsg.Pong fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (HpKeepAliveMsg.Pong)
                    new HpKeepAliveMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected HpKeepAliveMsg.Pong process(ChannelBuffer buffer) throws MessageDecodingException {

            return new HpKeepAliveMsg.Pong(vodSrc, vodSrc, timeoutId);
        }

    }
}
