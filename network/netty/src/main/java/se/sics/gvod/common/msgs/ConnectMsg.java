/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;

import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public class ConnectMsg {

    public static class Request extends DirectMsgNetty {

        private final UtilityVod utility;
        private final boolean toUtilitySet;
        private final int mtu;

        public Request(VodAddress source,
                VodAddress destination, UtilityVod utility,
                boolean toUtilitySet, int mtu) {
            super(source, destination, null);
            if(utility == null)  {
                throw new IllegalArgumentException("Utility cannot be null");
            }
            this.utility = new UtilityVod(utility.getChunk(), utility.getPiece(), utility.getOffset());
            this.toUtilitySet = toUtilitySet;
            this.mtu = mtu;
        }

        public int getMtu() {
            return mtu;
        }

        public boolean isToUtilitySet() {
            return toUtilitySet;
        }

        public UtilityVod getUtility() {
            return utility;
        }


        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.CONNECT_REQUEST;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + UserTypesEncoderFactory.UTILITY_LEN
                    + 1 /*toUtilitySet*/
                    + 2 /* MTU size */
                    ;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUtility(buffer, utility);
            UserTypesEncoderFactory.writeBoolean(buffer, toUtilitySet);
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, mtu);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            Request r = new Request(vodSrc, vodDest, utility, toUtilitySet, mtu);
            r.setTimeoutId(timeoutId);
            return r;
        }

    }

    public static enum ResponseType implements Serializable
    {

        OK, FULL, BAD_UTILITY;

        public static ResponseType create(int val) {
            if (val < 0 || val > values().length) {
                throw new IllegalArgumentException("Out-of-range ResponseType value: " + val);
            }
            return values()[val];
        }
    }

    public static class Response extends DirectMsgNetty {

        private final ResponseType response;
        private final UtilityVod utility;
        private final byte[] availableChunks;
        private final byte[][] availablePieces;
        private final boolean toUtilitySet;
        private final int mtu;

        public Response(VodAddress src, VodAddress dest, TimeoutId timeoutId, ResponseType response,
                UtilityVod utility, byte[] availableChunks, byte[][] availablePieces,
                boolean toUtilitySet, int mtu) {
            super(src, dest, timeoutId);
            if (response == null)  {
                throw new IllegalArgumentException("ResponseType cannot be null");
            }
            if (utility == null)  {
                throw new IllegalArgumentException("utility cannot be null");
            }
            this.response = response;
            this.utility = new UtilityVod(utility.getChunk(), utility.getPiece(), utility.getOffset());
            this.availableChunks = availableChunks;
            this.availablePieces = availablePieces;

            this.toUtilitySet = toUtilitySet;
            this.mtu = mtu;
        }

        
        public int getMtu() {
            return mtu;
        }

        
        public boolean isToUtilitySet() {
            return toUtilitySet;
        }

        public ResponseType getResponse() {
            return response;
        }

        public UtilityVod getUtility() {
            return utility;
        }

        public byte[] getAvailableChunks() {
            return availableChunks;
        }

        public byte[][] getAvailablePieces() {
            return availablePieces;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.CONNECT_RESPONSE;
        }

        @Override
        public int getSize() {
            int sz = getHeaderSize()
                    + 1 /* response type*/
                    + UserTypesEncoderFactory.UTILITY_LEN
                    + UserTypesEncoderFactory.getArraySize(availableChunks)
                    + UserTypesEncoderFactory.getArrayArraySize(availablePieces)
                    + 1 /* utility set */
                    + 2 /* mtu */
                    ;

            return sz;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();

            int responseTypeVal = response.ordinal();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, responseTypeVal);
            UserTypesEncoderFactory.writeUtility(buffer, utility);
            UserTypesEncoderFactory.writeArrayBytes(buffer, availableChunks);
            UserTypesEncoderFactory.writeArrayArrayBytes(buffer, availablePieces);
            UserTypesEncoderFactory.writeBoolean(buffer, toUtilitySet);
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, mtu);
            
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            return new Response(vodSrc, vodDest, timeoutId, response, utility, availableChunks, availablePieces, 
                    toUtilitySet, mtu);
        }
    }

    public static class RequestTimeout extends RewriteableRetryTimeout {

        private final VodAddress dest;
        private final boolean toUtilitySet;

        public RequestTimeout(ScheduleRetryTimeout timeout, ConnectMsg.Request request, 
                boolean toUtilitySet) {
            super(timeout, request);
            this.dest = request.getVodDestination();
            this.toUtilitySet = toUtilitySet;
        }

        public boolean isToUtilitySet() {
            return toUtilitySet;
        }

        public VodAddress getVodAddress() {
            return dest;
        }

    }
}
