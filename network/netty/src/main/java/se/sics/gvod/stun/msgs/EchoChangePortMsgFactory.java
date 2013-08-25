package se.sics.gvod.stun.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.msgs.DirectMsg;

public class EchoChangePortMsgFactory  {

    public static class Request extends StunRequestMsgFactory {

        private Request() {
        }

        public static EchoChangePortMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (EchoChangePortMsg.Request)
                    new EchoChangePortMsgFactory.Request().decode(buffer);
        }

        @Override
        protected DirectMsg process(ByteBuf buffer) throws MessageDecodingException {
            return new EchoChangePortMsg.Request(vodSrc, vodDest, transactionId);
        }
    }

    public static class Response extends StunResponseMsgFactory {

        private Response() {
        }

        public static EchoChangePortMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (EchoChangePortMsg.Response)
                    new EchoChangePortMsgFactory.Response().decode(buffer);
        }

        @Override
        protected DirectMsg process(ByteBuf buffer) throws MessageDecodingException {
            EchoChangePortMsg.Response response = new EchoChangePortMsg.Response(
                    vodSrc, vodDest, transactionId, timeoutId);
            return response;
        }


    }
};
