package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageDecodingException;

public class HolePunchingMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static HolePunchingMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (HolePunchingMsg.Request)
                    new HolePunchingMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected HolePunchingMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            int zServerId = buffer.readInt();
            return new HolePunchingMsg.Request(vodSrc, vodDest, zServerId,
                    msgTimeoutId);
        }
    }

    public static class Response extends HpMsgFactory {

        private Response() {
        }

        public static HolePunchingMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (HolePunchingMsg.Response)
                    new HolePunchingMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected HolePunchingMsg.Response process(ByteBuf buffer) throws MessageDecodingException {
            return new HolePunchingMsg.Response(vodSrc, vodDest,
                    msgTimeoutId);
        }

    }
    
    public static class ResponseAck extends HpMsgFactory {

        private ResponseAck() {
        }

        public static HolePunchingMsg.ResponseAck fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (HolePunchingMsg.ResponseAck)
                    new HolePunchingMsgFactory.ResponseAck().decode(buffer, true);
        }

        @Override
        protected HolePunchingMsg.ResponseAck process(ByteBuf buffer) throws MessageDecodingException {
            return new HolePunchingMsg.ResponseAck(vodSrc, vodDest, timeoutId,
                    msgTimeoutId);
        }

    }
}
