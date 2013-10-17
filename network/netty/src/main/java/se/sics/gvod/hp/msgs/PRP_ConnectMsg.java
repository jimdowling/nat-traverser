package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;

import java.util.Set;

import se.sics.gvod.common.msgs.MessageEncodingException;
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
 */
public class PRP_ConnectMsg
{

    public static final class Request extends HpMsg.Request
    {
        static final long serialVersionUID = 1L;

        private final Set<Integer> setOfAvailablePorts;

        public Request(VodAddress src, VodAddress dest,
                int remoteClientId, Set<Integer> setOfAvailablePorts, TimeoutId msgTimeoutId)
        {
            super(src, dest, remoteClientId, msgTimeoutId);
            this.setOfAvailablePorts = setOfAvailablePorts;
        }

        public Request(Request msg, VodAddress src)
        {
            super(src, msg.getVodDestination(), msg.getRemoteClientId(), 
                    msg.getMsgTimeoutId());
            setOfAvailablePorts = msg.getSetOfAvailablePorts();
            this.timeoutId = msg.getTimeoutId();
        }

        public Set<Integer> getSetOfAvailablePorts()
        {
            return setOfAvailablePorts;
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + 1 /* size set */
                    + setOfAvailablePorts.size() * 2
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PRP_SERVER_REQ_AVAILABLE_PORTS_MSG;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeSetUnsignedTwoByteInts(buffer, setOfAvailablePorts);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            return new PRP_ConnectMsg.Request(this, vodSrc);
        }
        
    }

    public enum ResponseType
    {
        OK, FAILED, RESPONDER_ID_REMOTE_ID_DO_NOT_MATCH, CLIENT_RECORD_NOT_FOUND
    };

    public final static class Response extends HpMsg.Response
    {
        static final long serialVersionUID = 1L;
        private final ResponseType responseType;
        private final VodAddress remoteClientDummyPublicAddress;
        private final int portToUse;    // servers says to use this port to talk to other client
        private final boolean bindFirst; 

        public Response(VodAddress src, VodAddress dest,  TimeoutId timeoutId, 
                ResponseType responseType,
                int remoteClientId, VodAddress remoteClientDummyPublicAddress, 
                int portToUse, boolean bindFirst, TimeoutId msgTimeoutId)
        {
            super(src, dest, timeoutId, remoteClientId, msgTimeoutId);
            this.responseType = responseType;
            this.portToUse = portToUse;
            this.remoteClientDummyPublicAddress = remoteClientDummyPublicAddress;
            this.bindFirst = bindFirst;
        }

        public VodAddress getRemoteClientDummyPublicAddress()
        {
            return remoteClientDummyPublicAddress;
        }

       
        public ResponseType getResponseType()
        {
            return responseType;
        }

        public int getPortToUse()
        {
            return portToUse;
        }

        public boolean isBindFirst() {
            return bindFirst;
        }
        
        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + 1
                    + UserTypesEncoderFactory.VOD_ADDRESS_LEN_NO_PARENTS
                    + 2 /* portToUse */
                    + 1 /* bindFirst */
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PRP_SEND_PORTS_ZSERVER_RESPONSE;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    responseType.ordinal());
            UserTypesEncoderFactory.writeVodAddress(buffer, remoteClientDummyPublicAddress);
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer,
                    portToUse);
            UserTypesEncoderFactory.writeBoolean(buffer, bindFirst);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            return new PRP_ConnectMsg.Response(vodSrc, vodDest, timeoutId, responseType, 
                    remoteClientId, remoteClientDummyPublicAddress, portToUse, bindFirst,
                    msgTimeoutId);
        }

    }

    public static final class RequestRetryTimeout extends RewriteableRetryTimeout
    {

        private final Request requestMsg;

        public RequestRetryTimeout(ScheduleRetryTimeout st, Request requestMsg)
        {
            super(st, requestMsg);
            this.requestMsg = requestMsg;
        }

        public Request getRequestMsg()
        {
            return requestMsg;
        }

    }

}
