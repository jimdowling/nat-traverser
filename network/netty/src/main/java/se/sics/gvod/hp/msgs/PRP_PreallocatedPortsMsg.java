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
 * @author jim
 * z server sends this message to the initiator of PRP when it has run out of
 * cached ports
 * in response to this message the client must send some available ports to the
 * zServer. 
 */
public class PRP_PreallocatedPortsMsg
{
    public final static class Request extends HpMsg.Request
    {
        static final long serialVersionUID = 1L;

        public Request(VodAddress src, VodAddress dest, TimeoutId msgTimeoutId)
        {
            super(src, dest, src.getId(), msgTimeoutId);
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PRP_PREALLOCATED_PORTS_REQUEST;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            PRP_PreallocatedPortsMsg.Request copy = 
                    new PRP_PreallocatedPortsMsg.Request(vodSrc, vodDest,
                    msgTimeoutId);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

    public enum ResponseType
    {
        OK, FAILED, NO_PORTS_AVAILABLE, INVALID_NOT_A_PARENT
    };

    public final static class Response extends HpMsg.Response
    {
        static final long serialVersionUID = 1L;
        private final ResponseType responseType;
        private final Set<Integer> prpPorts;    

        public Response(VodAddress src, VodAddress dest, TimeoutId timeoutId, 
                ResponseType responseType,
                Set<Integer> prpPorts, TimeoutId msgTimeoutId)
        {
            super(src, dest, timeoutId, dest.getId(), msgTimeoutId);
            this.responseType = responseType;
            this.prpPorts = prpPorts;
        }

        public Set<Integer> getPrpPorts() {
            return prpPorts;
        }

        public ResponseType getResponseType()
        {
            return responseType;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 1
                    + ((prpPorts == null) ? 0 : prpPorts.size() * 4)
                   ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PRP_PREALLOCATED_PORTS_RESPONSE;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    responseType.ordinal());
            UserTypesEncoderFactory.writeSetUnsignedTwoByteInts(buffer, prpPorts);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            return new PRP_PreallocatedPortsMsg.Response(vodSrc, vodDest, timeoutId, 
                    responseType, prpPorts, msgTimeoutId);
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
