package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class DisconnectMsgFactory {

    public static class Request extends DirectMsgNettyFactory {

        private Request() {
        }

        public static DisconnectMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (DisconnectMsg.Request)
                    new DisconnectMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected DirectMsg process(ByteBuf buffer) throws MessageDecodingException {
            return new DisconnectMsg.Request(vodSrc, vodDest);
        }
    }

    public static class Response extends DirectMsgNettyFactory {

        private Response() {
        }

        public static DisconnectMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (DisconnectMsg.Response)
                    new DisconnectMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected DirectMsg process(ByteBuf buffer) throws MessageDecodingException {
            int ref = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            return new DisconnectMsg.Response(vodSrc, vodDest, timeoutId, ref);
        }
    }
};
