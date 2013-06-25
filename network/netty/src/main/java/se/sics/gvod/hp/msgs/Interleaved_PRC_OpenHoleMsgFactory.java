package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class Interleaved_PRC_OpenHoleMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static Interleaved_PRC_OpenHoleMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (Interleaved_PRC_OpenHoleMsg.Request)
                    new Interleaved_PRC_OpenHoleMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected Interleaved_PRC_OpenHoleMsg.Request process(ChannelBuffer buffer) throws MessageDecodingException {

            return new Interleaved_PRC_OpenHoleMsg.Request(vodSrc, vodDest, remoteClientId,
                    msgTimeoutId);
        }
    }

    public static class Response extends HpMsgFactory {

        private Response() {
        }

        public static Interleaved_PRC_OpenHoleMsg.Response fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (Interleaved_PRC_OpenHoleMsg.Response)
                    new Interleaved_PRC_OpenHoleMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected Interleaved_PRC_OpenHoleMsg.Response process(ChannelBuffer buffer) throws MessageDecodingException {
            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            Interleaved_PRC_OpenHoleMsg.ResponseType responseType =
                    Interleaved_PRC_OpenHoleMsg.ResponseType.values()[rt];
            return new Interleaved_PRC_OpenHoleMsg.Response(vodSrc, vodDest, 
                    timeoutId, responseType, remoteClientId, msgTimeoutId);
        }

    }
}
