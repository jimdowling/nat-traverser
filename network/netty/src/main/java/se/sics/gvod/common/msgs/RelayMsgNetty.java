/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.Serializable;

import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RelayMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.NoTimeoutId;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.UnsetTimeoutId;

/**
 *
 * @author jdowling
 */
public class RelayMsgNetty {

    static abstract class Base
            extends RelayMsg.Base implements Encodable {

        public Base(VodAddress source, VodAddress destination, int clientId, int remoteId
                , TimeoutId timeoutId) {
            this(source, destination, clientId, remoteId, null, timeoutId);
        }


        public Base(VodAddress source, VodAddress destination, int clientId, int remoteId, 
                VodAddress nextDest, TimeoutId timeoutId) {
            super(source, destination, clientId, remoteId, nextDest, timeoutId);
        }

        @Override
        public int getSize() {
            return 4 // srcId
                    + 4 // destId
                    + (hasTimeout() ? 4 : 0)
                    + 1 /*natPolicy src*/
                    + (UserTypesEncoderFactory.ADDRESS_LEN * VodConfig.PM_NUM_PARENTS)
                    + (UserTypesEncoderFactory.ADDRESS_LEN * VodConfig.PM_NUM_PARENTS)
                    + 1 /*natPolicy dest*/
                    + (4 * 2) /* overlayId of client and server */
                    + UserTypesEncoderFactory.VOD_ADDRESS_LEN_NO_PARENTS
                    + 4 /* remoteId */
                    + 4 /* clientId */
                    ;
        }

        protected ByteBuf createChannelBufferWithHeader()
                throws MessageEncodingException {
        	ByteBuf buffer =
        			Unpooled.buffer(
                    getSize()
                    + 1 /*opcode*/);
            writeHeader(buffer);
            return buffer;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            return buffer;
        }

        protected void writeHeader(ByteBuf buffer) throws MessageEncodingException {
            byte b = getOpcode();
            buffer.writeByte(b);
            if (hasTimeout()) {
                buffer.writeInt(timeoutId.getId());
            }
            buffer.writeInt(getSource().getId());
            buffer.writeInt(getDestination().getId());

            buffer.writeInt(vodSrc.getOverlayId());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, vodSrc.getNatPolicy());
            UserTypesEncoderFactory.writeListAddresses(buffer, vodSrc.getParents());
            buffer.writeInt(vodDest.getOverlayId());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, vodDest.getNatPolicy());
            // don't write parents of src
            if (nextDest == null) {
                throw new IllegalStateException("nextDest was null for RelayMsg");
            }
            UserTypesEncoderFactory.writeVodAddress(buffer, nextDest);
            buffer.writeInt(clientId);
            buffer.writeInt(remoteId);
        }

    }

    public abstract static class Request extends Base {


        public Request(VodAddress source, VodAddress destination, int clientId, int remoteId) {
            this(source, destination, clientId, remoteId, new UnsetTimeoutId());
        }

        public Request(VodAddress source, VodAddress destination, int clientId, int remoteId
                , TimeoutId timeoutId) {
            this(source, destination, clientId, remoteId, 
                    source.isOpen() ? source : destination, timeoutId);
        }

        protected Request(VodAddress source, VodAddress destination, int clientId, int remoteId,
                VodAddress nextDest, TimeoutId timeoutId) {
            super(source, destination, clientId, remoteId, nextDest, timeoutId);
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.RELAY_REQUEST;
        }

        public boolean rewriteNextDestination(VodAddress newNextDest) {
            if (this.nextDest.equals(newNextDest)) {
                return false;
            }
            this.nextDest = newNextDest;
            return true;
        }

    }

    public static enum Status implements Serializable {

        OK, FAIL, DESTINATION_NOT_REGISTERED, NO_RESPONSE_FROM_DEST;

        public static Status create(int val) {
            if (val < 0 || val > values().length) {
                throw new IllegalArgumentException("Out-of-range ResponseType value: " + val);
            }
            return values()[val];
        }
    }

    public abstract static class Response extends Base {

        private final Status status;
        private transient long rtt;

        public Response(VodAddress source, VodAddress destination, int clientId, int remoteId,
                VodAddress nextDest, TimeoutId timeoutId, Status status) {
            super(source, destination, clientId, remoteId, nextDest, timeoutId);
            this.status = status;
        }

        public Response(VodAddress self, RelayMsgNetty.Request request, Status status) {
            this(self, request.getVodSource(), request.getClientId(), request.getRemoteId(),
                    request.getNextDest(), request.getTimeoutId(), status);
        }

        public Status getStatus() {
            return status;
        }

        @Override
        public int getSize() {
            return super.getSize()
                    + 1 /* response type*/;
        }

        public long getRtt() {
            return rtt;
        }

        public void setRtt(long val) {
            this.rtt = val;
        }

        @Override
        protected void writeHeader(ByteBuf buffer) throws MessageEncodingException {
            super.writeHeader(buffer);
            int responseTypeVal = status.ordinal();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, responseTypeVal);
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.RELAY_RESPONSE;
        }

    }

    public abstract static class Oneway extends Base {

        public Oneway(VodAddress source, VodAddress destination, int clientId, int remoteId) {
            super(source, destination, clientId, remoteId, destination, new NoTimeoutId());
        }

        Oneway(VodAddress source, VodAddress destination, int clientId, int remoteId,
                VodAddress nextDest) {
            super(source, destination, clientId, remoteId, nextDest, new NoTimeoutId());
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.RELAY_ONEWAY;
        }


    }

    public static class RequestTimeout extends RewriteableRetryTimeout {

        public RequestTimeout(ScheduleRetryTimeout request, Request msg) {
            super(request, msg);
        }
    }
}
