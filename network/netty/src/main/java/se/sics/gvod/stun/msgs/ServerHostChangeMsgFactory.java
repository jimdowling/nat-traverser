package se.sics.gvod.stun.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;
import se.sics.gvod.timer.UUID;

public abstract class ServerHostChangeMsgFactory {

    public static class Request extends StunRequestMsgFactory {

        private Request() {
        }

        public static ServerHostChangeMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (ServerHostChangeMsg.Request)
                    new ServerHostChangeMsgFactory.Request().decode(buffer);
        }

        @Override
        protected ServerHostChangeMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            Address clientPublicAddr = UserTypesDecoderFactory.readAddress(buffer);
//            UUID origId = UserTypesDecoderFactory.readUUID(buffer);
            UUID origId = new UUID(buffer.readInt());
            return new ServerHostChangeMsg.Request(vodSrc, vodDest, clientPublicAddr,
                    transactionId, timeoutId, origId);
        }
    }

    public static class Response extends StunResponseMsgFactory {

        private Response() {
        }
        

        public static ServerHostChangeMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (ServerHostChangeMsg.Response)
                    new ServerHostChangeMsgFactory.Response().decode(buffer);
        }

        @Override
        protected ServerHostChangeMsg.Response process(ByteBuf buffer) throws MessageDecodingException {
            return new ServerHostChangeMsg.Response(vodSrc, vodDest, transactionId, timeoutId);
        }
    }
};
