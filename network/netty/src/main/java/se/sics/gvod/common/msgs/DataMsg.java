/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.util.UserTypesEncoderFactory;

import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.UUID;

/**
 *
 * This msg is a request and response for a subpiece, with a
 * supplied ackId and the delay will be measured for the uplink
 * (Response msg). The Request sends back the delay for the previous
 * Response.
 *
 * @author gautier, jdowling
 */
public class DataMsg {

    public static class Request extends VodMsgNetty {

        private final TimeoutId ackId;
        private final int piece;
        private final int subpiece;
        private final long delay;

        public Request(VodAddress self, VodAddress destination,
                TimeoutId ackId, int piece, int subpiece,
                long delay) {
            super(self, destination);
            if (ackId == null) {
                this.ackId = new UUID(0);
            } else {
                this.ackId = ackId;
            }
            this.piece = piece;
            this.subpiece = subpiece;
            this.delay = delay;
        }

        public int getSubpiece() {
            return subpiece;
        }

        public int getPiece() {
            return piece;
        }

        public TimeoutId getAckId() {
            return ackId;
        }

        public long getDelay() {
            return delay;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.D_REQUEST;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 4 /*ackId*/
                    + 4 /*piece*/
                    + 4 /*subpiece*/
                    + 8 /*delay*/;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            buffer.writeInt(ackId.getId());
            buffer.writeInt(piece);
            buffer.writeInt(subpiece);
            buffer.writeLong(delay);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            Request r = new Request(vodSrc, vodDest, ackId, piece, subpiece, delay);
            r.setTimeoutId(timeoutId);
            return r;
        }
    }

    public static class Response extends VodMsgNetty {

        private final TimeoutId ackId;
        private final byte[] sp;
        private final int nb;
        private final int p;
        private final int cwSz;
        private final long t;

        public Response(VodAddress source, VodAddress destination,
                TimeoutId timeoutId, 
                TimeoutId ackId, byte[] subpiece, int subpieceNb, int piece,
                int comWinSize, long time) {
            super(source, destination, timeoutId);
            if (ackId == null) {
                throw new IllegalArgumentException("AckId was null");
            }
            this.ackId = ackId;
            this.sp = subpiece;
            this.nb = subpieceNb;
            this.p = piece;
            this.cwSz = comWinSize;
            this.t = time;
        }

        public long getTime() {
            return t;
        }


        public TimeoutId getAckId() {
            return ackId;
        }

        public int getComWinSize() {
            return cwSz;
        }

        public byte[] getSubpiece() {
            return sp;
        }

        public int getSubpieceNb() {
            return nb;
        }

        public int getPiece() {
            return p;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.D_RESPONSE;
        }

        @Override
        public int getSize() {
            int spSz = 0;
            if (sp != null) {
                spSz = sp.length;
            }
            return getHeaderSize()
//                    + +UserTypesEncoderFactory.TimeoutId_LEN
                    + 4 /*timeoutId*/
                    + spSz
                    + 4 /*nb*/
                    + 4 /*p*/
                    + 4 /*cwSz*/
                    + 8 /*time*/;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buf = createChannelBufferWithHeader();
//            UserTypesEncoderFactory.writeTimeoutId(buf, ackId);
            buf.writeInt(ackId.getId());
            UserTypesEncoderFactory.writeArrayBytes(buf, sp);
            buf.writeInt(nb);
            buf.writeInt(p);
            buf.writeInt(cwSz);
            buf.writeLong(t);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new Response(vodSrc, vodDest, timeoutId, ackId, sp, nb, p, cwSz, t);
        }
    }

    /**
     * No point re-sending the ackTimeout with a new DataRequest.
     */
    public static class DataRequestTimeout extends RewriteableRetryTimeout {

        private final VodAddress dest;
        private final int piece;
        private final int subpiece;

        public DataRequestTimeout(ScheduleRetryTimeout request, Request req) {
            super(request, req);
            this.dest = req.getVodDestination();
            this.piece = req.getPiece();
            this.subpiece = req.getSubpiece();
        }

        public VodAddress getDest() {
            return dest;
        }

        public int getPiece() {
            return piece;
        }

        public int getSubpiece() {
            return subpiece;
        }
    }

    /**
     * TODO - these messages are huge. Turn into BitSet.
     */
    public static class PieceNotAvailable extends VodMsgNetty {

        private final byte[] availableChunks;
        private final UtilityVod utility;
        private final int piece;
        private final byte[][] availablePieces;

        public PieceNotAvailable(VodAddress source, VodAddress destination,
                byte[] availableChunks, UtilityVod utility, int piece,
                byte[][] availablePieces) {
            super(source, destination);
            this.availableChunks = availableChunks;
            this.utility = new UtilityVod(utility.getChunk(), utility.getPiece(), utility.getOffset());
            this.piece = piece;
            this.availablePieces = availablePieces;
        }
        
        public byte[] getAvailableChunks() {
            return availableChunks;
        }

        public UtilityVod getUtility() {
            return utility;
        }

        public int getPiece() {
            return piece;
        }

        public byte[][] getAvailablePieces() {
            return availablePieces;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.PIECE_NOT_AVAILABLE;
        }

        @Override
        public int getSize() {
            int sz = getHeaderSize()
                    + UserTypesEncoderFactory.getArraySize(availableChunks)
                    + UserTypesEncoderFactory.UTILITY_LEN
                    + 4 /*piece*/
                    + UserTypesEncoderFactory.getArrayArraySize(availablePieces);
            return sz;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeArrayBytes(buffer, availableChunks);
            UserTypesEncoderFactory.writeUtility(buffer, utility);
            buffer.writeInt(piece);
            UserTypesEncoderFactory.writeArrayArrayBytes(buffer, availablePieces);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
           PieceNotAvailable p = new PieceNotAvailable(vodSrc, vodDest, availableChunks, 
                   utility, piece, availablePieces);
           p.setTimeoutId(timeoutId);
           return p;
        }
    }

    public static class Saturated extends VodMsgNetty {

        private final int subpiece;
        private final int comWinSize;

        public Saturated(VodAddress source, VodAddress destination, int subpiece,
                int comWinSize) {
            super(source, destination);
            this.subpiece = subpiece;
            this.comWinSize = comWinSize;
        }

        public int getComWinSize() {
            return comWinSize;
        }

        public int getSubpiece() {
            return subpiece;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.SATURATED;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 4 /* subpiece */
                    + 4 /* comWinSize */;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buf = createChannelBufferWithHeader();
            buf.writeInt(subpiece);
            buf.writeInt(comWinSize);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new Saturated(vodSrc, vodDest, subpiece, comWinSize);
        }
    }

    public static class Ack extends VodMsgNetty {

        private final long delay;

        public Ack(VodAddress source, VodAddress destination, TimeoutId timeoutId,
                long delay) {
            super(source, destination, timeoutId);
            this.delay = delay;
        }

        public TimeoutId getAckId() {
            return timeoutId;
        }

        public long getDelay() {
            return delay;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.ACK;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 8 /*delay*/;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buf = createChannelBufferWithHeader();
            buf.writeLong(delay);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new Ack(vodSrc, vodDest, timeoutId, delay);
        }
    }

    public static class AckTimeout extends Timeout {

        private final VodAddress peer;

        public AckTimeout(ScheduleTimeout request, VodAddress peer) {
            super(request);
            this.peer = peer;
        }

        public VodAddress getPeer() {
            return peer;
        }
    }

    public static class HashRequest extends VodMsgNetty {

        private final int chunk;
        private final int part;

        public HashRequest(VodAddress source, VodAddress destination, TimeoutId timeoutId,
                int chunk) {
            this(source, destination, timeoutId, chunk, 0);
        }

        public HashRequest(VodAddress source, VodAddress destination, TimeoutId timeoutId,
                int chunk, int part) {
            super(source, destination, timeoutId);
            this.chunk = chunk;
            this.part = part;
        }

        public int getChunk() {
            return chunk;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.HASH_REQUEST;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 2 /*chunk*/
                    + 1 /*part*/;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buf = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buf, chunk);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buf, part);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new HashRequest(vodSrc, vodDest, timeoutId, chunk, part);
        }
    }

    public static class HashResponse extends VodMsgNetty {

        private final int chunk;
        private final byte[] hashes;
        private final int part;
        private final int numParts;

        public HashResponse(VodAddress source, VodAddress destination, TimeoutId timeoutId,
                int chunk, byte[] hashes, int part, int numParts) {
            super(source, destination, timeoutId);
            this.chunk = chunk;
            this.hashes = hashes;
            this.part = part;
            this.numParts = numParts;
        }

        public int getChunk() {
            return chunk;
        }

        public byte[] getHashes() {
            return hashes;
        }

        public int getPart() {
            return part;
        }

        public int getNumParts() {
            return numParts;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.HASH_RESPONSE;
        }

        @Override
        public int getSize() {
            int size = getHeaderSize()
                    + 2 /*chunk*/
                    + UserTypesEncoderFactory.getArraySize(hashes)
                    + 1 /*part*/
                    + 1 /*num parts*/;
            return size;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buf = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buf, chunk);
            UserTypesEncoderFactory.writeArrayBytes(buf, hashes);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buf, part);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buf, numParts);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new HashResponse(vodSrc, vodDest, timeoutId, chunk, hashes, part, numParts);
        }
    }

    public static class HashTimeout extends Timeout {

        private final int chunk;
        private final VodAddress peer;
        private final int part;

        public HashTimeout(ScheduleTimeout request, int chunk, VodAddress peer,
                int part) {
            super(request);
            this.chunk = chunk;
            this.peer = peer;
            this.part = part;
        }

        public int getChunk() {
            return chunk;
        }

        public VodAddress getPeer() {
            return peer;
        }

        public int getPart() {
            return part;
        }
    }
}
