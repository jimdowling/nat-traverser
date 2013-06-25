package se.sics.gvod.stun.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class EchoChangeIpAndPortMsgFactory  {

    public static class Request extends StunRequestMsgFactory {

        private Request() {
        }

        public static EchoChangeIpAndPortMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (EchoChangeIpAndPortMsg.Request)
                    new EchoChangeIpAndPortMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            return new EchoChangeIpAndPortMsg.Request(vodSrc, vodDest, transactionId);
        }
    }

    public static class Response extends StunResponseMsgFactory {

        private Response(){
        }

        public static EchoChangeIpAndPortMsg.Response fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (EchoChangeIpAndPortMsg.Response)
                    new EchoChangeIpAndPortMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
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
