package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;

import java.util.Set;

import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class Interleaved_PRP_ConnectMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static Interleaved_PRP_ConnectMsg.Request fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (Interleaved_PRP_ConnectMsg.Request)
                    new Interleaved_PRP_ConnectMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected Interleaved_PRP_ConnectMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            Set<Integer> setOfAvailablePorts = UserTypesDecoderFactory.readSetUnsignedTwoByteInts(buffer);
            return new Interleaved_PRP_ConnectMsg.Request(vodSrc, vodDest, 
                    remoteClientId, setOfAvailablePorts, msgTimeoutId);
        }
    }

    public static class Response extends HpMsgFactory {

        private Response() {
        }

        public static Interleaved_PRP_ConnectMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (Interleaved_PRP_ConnectMsg.Response)
                    new Interleaved_PRP_ConnectMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected Interleaved_PRP_ConnectMsg.Response process(ByteBuf buffer) throws MessageDecodingException {

            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            Interleaved_PRP_ConnectMsg.ResponseType responseType =
                    Interleaved_PRP_ConnectMsg.ResponseType.values()[rt];
            return new Interleaved_PRP_ConnectMsg.Response(vodSrc, vodDest,
                    timeoutId, responseType, remoteClientId, msgTimeoutId);
        }

    }
}
