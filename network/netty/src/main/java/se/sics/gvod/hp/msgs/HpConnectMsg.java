package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;

import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.hp.events.OpenConnectionResponseType;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 * 
 * @author salman
 *
 * this class is used by the client to inform the RVP that it wants to punch hole for
 * some other client. to do so the client must know the public ip (i.e. ip of the nat reponsible for
 * the other client) and the ID of the client to whome it wants to talk.
 *
 * client (lets say A) will send this message to the RVP. RVP will check the feasibility for hole punching.
 * hole punching many not be feasible due to many reasons like 
 * 1) if the remote client (lets say B)to whom we want to talk to is not registered with the RVP
 * 2) nat type of both clients are incompatible (non traversable). 
 * if B is registered and the two nats are traversable then RVP replies by sending ACK
 * response, otherwise it will send the NACK message
 */
public class HpConnectMsg implements Serializable {

    public static final class Request extends HpMsg.Request {

        static final long serialVersionUID = 187778888654L;
        private final int delta;
        private final long rtt;

        public Request(VodAddress src, VodAddress dest, int remoteClientId,
                int delta, long rtt, TimeoutId msgTimeoutId) {
            super(src, dest, remoteClientId, msgTimeoutId);
            this.delta = delta;
            this.rtt = rtt;
        }

        public int getDelta() {
            return delta;
        }

        public long getRtt() {
            return rtt;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 1 /*delta*/
                    + 2 /*rtt*/;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.HP_FEASABILITY_REQUEST;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, delta);
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, (int) rtt);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            HpConnectMsg.Request copy = new HpConnectMsg.Request(vodSrc, vodDest,
                    remoteClientId, delta, rtt, msgTimeoutId);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

    public final static class Response extends HpMsg.Response {

        static final long serialVersionUID = 1213423434L;
//        public static enum ResponseType {
//            OK, REGISTER_FIRST_THEN_CHECK_FEASIBILITY, REMOTE_PEER_NOT_REGISTERED,
//            NAT_COMBINATION_NOT_TRAVERSABLE, SESSION_ALREADY_EXISTS, BOTH_PEERS_OPEN
//        }
        private final OpenConnectionResponseType responseType;
        private final HPMechanism hpMechanism;
        private boolean newSession;

        public Response(VodAddress src, VodAddress dest, int remoteClientID,
                OpenConnectionResponseType responseType, TimeoutId timeoutId,
                HPMechanism hpMechanism,
                boolean newSession, TimeoutId msgTimeoutId) {
            super(src, dest, timeoutId, remoteClientID, msgTimeoutId);
            this.responseType = responseType;
            this.hpMechanism = hpMechanism;
            this.newSession = newSession;
        }

        public boolean isNewSession() {
            return newSession;
        }

        public HPMechanism getHpMechanism() {
            return hpMechanism;
        }

        public OpenConnectionResponseType getResponseType() {
            return responseType;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 1
                    + 1
                    + 1;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.HP_FEASABILITY_RESPONSE;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, responseType.ordinal());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, hpMechanism.ordinal());
            UserTypesEncoderFactory.writeBoolean(buffer, newSession);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
           return new HpConnectMsg.Response(vodSrc, vodDest, remoteClientId, 
                   responseType, timeoutId, hpMechanism, newSession, msgTimeoutId);
        }
    }

    public static final class RequestRetryTimeout extends RewriteableRetryTimeout {

        private final Request requestMsg;

        public RequestRetryTimeout(ScheduleRetryTimeout st, Request requestMsg) {
            super(st, requestMsg);
            this.requestMsg = requestMsg;
        }

        public Request getRequestMsg() {
            return requestMsg;
        }
    }
}
