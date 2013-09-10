package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;

public class TConnectionMsgFactory {

    public static class Ping extends DirectMsgNettyFactory.Request {

        private Ping() {
        }

        public static TConnectionMsg.Ping fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (TConnectionMsg.Ping) new TConnectionMsgFactory.Ping().decode(buffer);
        }

        @Override
        protected TConnectionMsg.Ping process(ByteBuf buffer) throws MessageDecodingException {

            return new TConnectionMsg.Ping(vodSrc, vodDest, timeoutId);
        }
    }

    public static class Pong extends DirectMsgNettyFactory.Response {

        private Pong() {
        }

        public static TConnectionMsg.Pong fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (TConnectionMsg.Pong) new TConnectionMsgFactory.Pong().decode(buffer);
        }

        @Override
        protected TConnectionMsg.Pong process(ByteBuf buffer) throws MessageDecodingException {

            return new TConnectionMsg.Pong(vodSrc, vodDest, timeoutId);
        }
    }
    public static class Pang extends DirectMsgNettyFactory.Oneway {

        private Pang() {
        }

        public static TConnectionMsg.Pang fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (TConnectionMsg.Pang) new TConnectionMsgFactory.Pang().decode(buffer);
        }

        @Override
        protected TConnectionMsg.Pang process(ByteBuf buffer) throws MessageDecodingException {

            return new TConnectionMsg.Pang(vodSrc, vodDest, timeoutId);
        }
    }
}
