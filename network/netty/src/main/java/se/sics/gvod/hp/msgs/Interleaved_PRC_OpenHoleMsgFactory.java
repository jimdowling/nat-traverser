package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class Interleaved_PRC_OpenHoleMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static Interleaved_PRC_OpenHoleMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (Interleaved_PRC_OpenHoleMsg.Request)
                    new Interleaved_PRC_OpenHoleMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected Interleaved_PRC_OpenHoleMsg.Request process(ByteBuf buffer) throws MessageDecodingException {

            return new Interleaved_PRC_OpenHoleMsg.Request(vodSrc, vodDest, remoteClientId,
                    msgTimeoutId);
        }
    }

    public static class Response extends HpMsgFactory {

        private Response() {
        }

        public static Interleaved_PRC_OpenHoleMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (Interleaved_PRC_OpenHoleMsg.Response)
                    new Interleaved_PRC_OpenHoleMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected Interleaved_PRC_OpenHoleMsg.Response process(ByteBuf buffer) throws MessageDecodingException {
            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            Interleaved_PRC_OpenHoleMsg.ResponseType responseType =
                    Interleaved_PRC_OpenHoleMsg.ResponseType.values()[rt];
            return new Interleaved_PRC_OpenHoleMsg.Response(vodSrc, vodDest, 
                    timeoutId, responseType, remoteClientId, msgTimeoutId);
        }

    }
}
