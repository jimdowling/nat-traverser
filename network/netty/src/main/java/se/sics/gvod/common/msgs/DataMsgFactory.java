package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.UUID;

public class DataMsgFactory {

    public static class Request extends VodMsgNettyFactory {

        private Request() {
        }

        public static DataMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (DataMsg.Request)
                    new DataMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            TimeoutId ackId = new UUID(buffer.readInt());
            int piece = buffer.readInt();
            int subpiece = buffer.readInt();
            long delay = buffer.readLong();
            return new DataMsg.Request(vodSrc, vodDest, ackId, piece,
                    subpiece,  delay);
        }
    }

    public static class Response extends VodMsgNettyFactory {

        private Response() {
        }


        public static DataMsg.Response fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (DataMsg.Response)
                    new DataMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
//            TimeoutId ack = UserTypesDecoderFactory.readTimeoutId(buffer);
            TimeoutId ack = new UUID(buffer.readInt());
            byte[] sp = UserTypesDecoderFactory.readArrayBytes(buffer);
            int nb = buffer.readInt();
            int p = buffer.readInt();
            int cwSz = buffer.readInt();
            long t = buffer.readLong();
            return new DataMsg.Response(vodSrc, vodDest, timeoutId, ack, sp,
                    nb, p, cwSz, t);
        }
    }

    public static class PieceNotAvailable extends VodMsgNettyFactory {

        private PieceNotAvailable() {
        }

        public static DataMsg.PieceNotAvailable fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (DataMsg.PieceNotAvailable) 
                    new DataMsgFactory.PieceNotAvailable().decode(buffer, false);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            byte[] availableChunks = UserTypesDecoderFactory.readArrayBytes(buffer);
            UtilityVod utility = (UtilityVod) UserTypesDecoderFactory.readUtility(buffer);
            int piece = buffer.readInt();
            byte[][] availablePieces = UserTypesDecoderFactory.readArrayArrayBytes(buffer);
            return new DataMsg.PieceNotAvailable(vodSrc, vodDest, 
                    availableChunks, utility, piece, availablePieces);
        }
    }

    public static class Saturated extends VodMsgNettyFactory {

        private Saturated() {
        }

        public static DataMsg.Saturated fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (DataMsg.Saturated)
                    new DataMsgFactory.Saturated().decode(buffer, false);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            int subpiece = buffer.readInt();
            int comWindowSize = buffer.readInt();
            return new DataMsg.Saturated(vodSrc, vodDest,
                    subpiece, comWindowSize);
        }
    }

    public static class HashRequest extends VodMsgNettyFactory {

        private HashRequest() {
        }

        public static DataMsg.HashRequest fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (DataMsg.HashRequest)
                    new DataMsgFactory.HashRequest().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            int chunk = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            int part = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            return new DataMsg.HashRequest(vodSrc, vodDest, timeoutId,
                    chunk, part);
        }
    }

    public static class HashResponse extends VodMsgNettyFactory {

        private HashResponse() {
        }

        public static DataMsg.HashResponse fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (DataMsg.HashResponse)
                    new DataMsgFactory.HashResponse().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            int chunk = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            byte[] hashes = UserTypesDecoderFactory.readArrayBytes(buffer);
            int part = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            int numParts = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            return new DataMsg.HashResponse(vodSrc, vodDest, timeoutId,
                    chunk, hashes, part, numParts);
        }
    }

    public static class Ack extends VodMsgNettyFactory {

        private Ack() {
        }

        public static DataMsg.Ack fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (DataMsg.Ack) new DataMsgFactory.Ack().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            long delay = buffer.readLong();
            return new DataMsg.Ack(vodSrc, vodDest, timeoutId, delay);
        }
    }
};
