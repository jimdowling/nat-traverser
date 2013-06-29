package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class ConnectMsgFactory  {

    public static class Request extends VodMsgNettyFactory {

        private Request() {
        }

        public static ConnectMsg.Request fromBuffer(ChannelBuffer buffer) 
                throws MessageDecodingException {
            return (ConnectMsg.Request)
                    new ConnectMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            UtilityVod utility = (UtilityVod) UserTypesDecoderFactory.readUtility(buffer);
            boolean isUSet = UserTypesDecoderFactory.readBoolean(buffer);
            int mtu = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            return new ConnectMsg.Request(vodSrc, vodDest,
                    utility, isUSet, mtu);
        }

    }

    public static class Response extends VodMsgNettyFactory {

        private Response() {
        }


        public static ConnectMsg.Response fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (ConnectMsg.Response)
                    new ConnectMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
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
