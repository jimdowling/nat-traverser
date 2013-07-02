package se.sics.gvod.stun.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.msgs.DirectMsg;

public class EchoChangePortMsgFactory  {

    public static class Request extends StunRequestMsgFactory {

        private Request() {
        }

        public static EchoChangePortMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (EchoChangePortMsg.Request)
                    new EchoChangePortMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected DirectMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            return new EchoChangePortMsg.Request(vodSrc, vodDest, transactionId);
        }
    }

    public static class Response extends StunResponseMsgFactory {

        private Response() {
        }

        public static EchoChangePortMsg.Response fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (EchoChangePortMsg.Response)
                    new EchoChangePortMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected DirectMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            EchoChangePortMsg.Response response = new EchoChangePortMsg.Response(
                    vodSrc, vodDest, transactionId, timeoutId);
            return response;
        }


    }
};
