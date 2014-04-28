package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class DisconnectMsgFactory {

    public static class Request extends DirectMsgNettyFactory.Request {

        private Request() {
        }

        public static DisconnectMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (DisconnectMsg.Request)
                    new DisconnectMsgFactory.Request().decode(buffer);
        }

        @Override
        protected DisconnectMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            return new DisconnectMsg.Request(vodSrc, vodDest);
        }
    }

    public static class Response extends DirectMsgNettyFactory.Response {

        private Response() {
        }

        public static DisconnectMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (DisconnectMsg.Response)
                    new DisconnectMsgFactory.Response().decode(buffer);
        }

        @Override
        protected DisconnectMsg.Response process(ByteBuf buffer) throws MessageDecodingException {
            int ref = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            return new DisconnectMsg.Response(vodSrc, vodDest, timeoutId, ref);
        }
    }
};
