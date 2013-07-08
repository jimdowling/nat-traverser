package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class PRC_OpenHoleMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static PRC_OpenHoleMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (PRC_OpenHoleMsg.Request)
                    new PRC_OpenHoleMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected PRC_OpenHoleMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            return new PRC_OpenHoleMsg.Request(vodSrc, vodDest, remoteClientId, msgTimeoutId);
        }
    }

    public static class Response extends HpMsgFactory {

        private Response() {
        }

        public static PRC_OpenHoleMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (PRC_OpenHoleMsg.Response)
                    new PRC_OpenHoleMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected PRC_OpenHoleMsg.Response process(ByteBuf buffer) throws MessageDecodingException {

            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            PRC_OpenHoleMsg.ResponseType responseType =
                    PRC_OpenHoleMsg.ResponseType.values()[rt];
            return new PRC_OpenHoleMsg.Response(vodSrc, vodDest, timeoutId,
                    responseType, remoteClientId, msgTimeoutId);
        }

    }
}
