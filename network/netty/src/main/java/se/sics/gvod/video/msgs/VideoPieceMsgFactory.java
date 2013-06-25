package se.sics.gvod.video.msgs;

import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.VodMsgNettyFactory;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class VideoPieceMsgFactory {

    public static class Advertisement extends VodMsgNettyFactory {

        Advertisement() {
        }

        public static VideoPieceMsg.Advertisement fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (VideoPieceMsg.Advertisement) new VideoPieceMsgFactory.Advertisement().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            Set<Integer> piecesIds = UserTypesDecoderFactory.readIntegerSet(buffer);
            return new VideoPieceMsg.Advertisement(vodSrc, vodDest, timeoutId, piecesIds);
        }
    }

    public static class Request extends VodMsgNettyFactory {

        private Request() {
        }

        public static VideoPieceMsg.Request fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (VideoPieceMsg.Request) new VideoPieceMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            Set<Integer> piecesIds = UserTypesDecoderFactory.readIntegerSet(buffer);
            return new VideoPieceMsg.Request(vodSrc, vodDest, timeoutId, piecesIds);
        }
    }

    public static class Response extends VodMsgNettyFactory {

        private Response() {
        }

        public static VideoPieceMsg.Response fromBuffer(ChannelBuffer buffer) throws MessageDecodingException {
            return (VideoPieceMsg.Response) new VideoPieceMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            EncodedSubPiece esp = UserTypesDecoderFactory.readEncodedSubPiece(buffer);
            return new VideoPieceMsg.Response(vodSrc, vodDest, timeoutId, esp);
        }
    }
};
