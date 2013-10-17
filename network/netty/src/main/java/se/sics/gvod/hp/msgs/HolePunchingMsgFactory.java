package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;
import se.sics.gvod.timer.TimeoutId;

public class HolePunchingMsgFactory {

    public static class Request extends HpMsgFactory.Request {

        private Request() {
        }

        public static HolePunchingMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (HolePunchingMsg.Request)
                    new HolePunchingMsgFactory.Request().decode(buffer);
        }

        @Override
        protected HolePunchingMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            return new HolePunchingMsg.Request(vodSrc, vodDest, msgTimeoutId);
        }
    }

    public static class Response extends HpMsgFactory.Response {

        private Response() {
        }

        public static HolePunchingMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (HolePunchingMsg.Response)
                    new HolePunchingMsgFactory.Response().decode(buffer);
        }

        @Override
        protected HolePunchingMsg.Response process(ByteBuf buffer) throws MessageDecodingException {
            TimeoutId srcTimeoutId = UserTypesDecoderFactory.readTimeoutId(buffer);
            return new HolePunchingMsg.Response(vodSrc, vodDest,
                    srcTimeoutId, msgTimeoutId);
        }

    }
    
    public static class ResponseAck extends HpMsgFactory.Response {

        private ResponseAck() {
        }

        public static HolePunchingMsg.ResponseAck fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (HolePunchingMsg.ResponseAck)
                    new HolePunchingMsgFactory.ResponseAck().decode(buffer);
        }

        @Override
        protected HolePunchingMsg.ResponseAck process(ByteBuf buffer) throws MessageDecodingException {
            return new HolePunchingMsg.ResponseAck(vodSrc, vodDest, timeoutId,
                    msgTimeoutId);
        }

    }
}
