package se.sics.gvod.stun.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class EchoChangeIpAndPortMsgFactory  {

    public static class Request extends StunRequestMsgFactory {

        private Request() {
        }

        public static EchoChangeIpAndPortMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (EchoChangeIpAndPortMsg.Request)
                    new EchoChangeIpAndPortMsgFactory.Request().decode(buffer);
        }

        @Override
        protected EchoChangeIpAndPortMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            return new EchoChangeIpAndPortMsg.Request(vodSrc, vodDest, transactionId);
        }
    }

    public static class Response extends StunResponseMsgFactory {

        private Response(){
        }

        public static EchoChangeIpAndPortMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (EchoChangeIpAndPortMsg.Response)
                    new EchoChangeIpAndPortMsgFactory.Response().decode(buffer);
        }

        @Override
        protected EchoChangeIpAndPortMsg.Response process(ByteBuf buffer) throws MessageDecodingException {
            int statusOrdinal = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            EchoChangeIpAndPortMsg.Response.Status testType =
                    EchoChangeIpAndPortMsg.Response.Status.values()[statusOrdinal];
            EchoChangeIpAndPortMsg.Response response =
                    new EchoChangeIpAndPortMsg.Response(vodSrc, vodDest, 
                    transactionId, timeoutId, testType);
            return response;
        }

    }
};
