package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class ConnectMsgFactory  {

    public static class Request extends DirectMsgNettyFactory.Request {

        private Request() {
        }

        public static ConnectMsg.Request fromBuffer(ByteBuf buffer) 
                throws MessageDecodingException {
            return (ConnectMsg.Request)
                    new ConnectMsgFactory.Request().decode(buffer);
        }

        @Override
        protected DirectMsg process(ByteBuf buffer) throws MessageDecodingException {
            UtilityVod utility = (UtilityVod) UserTypesDecoderFactory.readUtility(buffer);
            boolean isUSet = UserTypesDecoderFactory.readBoolean(buffer);
            int mtu = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            return new ConnectMsg.Request(vodSrc, vodDest, utility, isUSet, mtu);
        }

    }

    public static class Response extends DirectMsgNettyFactory.Response {

        private Response() {
        }


        public static ConnectMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (ConnectMsg.Response)
                    new ConnectMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected DirectMsg process(ByteBuf buffer) throws MessageDecodingException {
            ConnectMsg.ResponseType responseType =
                    ConnectMsg.ResponseType.create(UserTypesDecoderFactory.readIntAsOneByte(buffer));
            UtilityVod utility = (UtilityVod) UserTypesDecoderFactory.readUtility(buffer);
            byte[] availableChunks = UserTypesDecoderFactory.readArrayBytes(buffer);
            byte[][] availablePieces = UserTypesDecoderFactory.readArrayArrayBytes(buffer);
            boolean toUtilitySet = UserTypesDecoderFactory.readBoolean(buffer);
            int mtu = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            return new ConnectMsg.Response(vodSrc, vodDest, timeoutId, responseType, 
                    utility, availableChunks, availablePieces, toUtilitySet, mtu);
        }
    }
};
