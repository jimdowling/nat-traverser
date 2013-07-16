package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;

import java.util.Set;

import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class PRP_ConnectMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static PRP_ConnectMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (PRP_ConnectMsg.Request)
                    new PRP_ConnectMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected PRP_ConnectMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            Set<Integer> setOfAvailablePorts = UserTypesDecoderFactory.readSetUnsignedTwoByteInts(buffer);
            return new PRP_ConnectMsg.Request(vodSrc, vodDest,
                    remoteClientId, setOfAvailablePorts, msgTimeoutId);
        }
    }

    public static class Response extends HpMsgFactory {

        private Response() {
        }

        public static PRP_ConnectMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (PRP_ConnectMsg.Response)
                    new PRP_ConnectMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected PRP_ConnectMsg.Response process(ByteBuf buffer) throws MessageDecodingException {

            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            PRP_ConnectMsg.ResponseType responseType =
                    PRP_ConnectMsg.ResponseType.values()[rt];
            VodAddress dummyAddr = UserTypesDecoderFactory.readVodAddress(buffer);
            int portToUse = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            boolean bindFirst = UserTypesDecoderFactory.readBoolean(buffer);
            return new PRP_ConnectMsg.Response(vodSrc, vodDest, timeoutId,
                    responseType, remoteClientId, dummyAddr, portToUse, bindFirst, msgTimeoutId);
        }

    }
}
