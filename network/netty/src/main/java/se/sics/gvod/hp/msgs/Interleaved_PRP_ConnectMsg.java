package se.sics.gvod.hp.msgs;

import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 * 
 * @author salman
 */
public class Interleaved_PRP_ConnectMsg
{

    public static final class Request extends HpMsg
    {
        static final long serialVersionUID = 18766722L;

        private final Set<Integer> setOfAvailablePorts;
	
        public Request(VodAddress src, VodAddress dest,
                int remoteClientId, Set<Integer> setOfAvailablePorts, 
                TimeoutId msgTimeoutId)
        {
            super(src, dest, remoteClientId, msgTimeoutId);
            this.setOfAvailablePorts = setOfAvailablePorts;
        }

        public Request(Request request, VodAddress src)
        {
            super(src, request.getVodDestination(), request.getRemoteClientId(),
                    request.getMsgTimeoutId());
            setOfAvailablePorts = request.getSetOfAvailablePorts();
        }

        public Set<Integer> getSetOfAvailablePorts()
        {
            return setOfAvailablePorts;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + (setOfAvailablePorts.size() * 4)
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_REQUEST;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeSetUnsignedTwoByteInts(buffer, setOfAvailablePorts);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            Interleaved_PRP_ConnectMsg.Request copy = new 
                    Interleaved_PRP_ConnectMsg.Request(vodSrc, 
                    vodDest, remoteClientId, setOfAvailablePorts, msgTimeoutId);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
        

    }

    public enum ResponseType
    {
        OK, FAILED, REMOTE_ID_NOT_REGISTERED, NO_SESSION, SEND_MORE_PORTS
    };

    public final static class Response extends HpMsg
    {
        static final long serialVersionUID = 1L;
        private final ResponseType responseType;
        // TODO:  say which port is active for which session?!?
        
        
        public Response(VodAddress src, VodAddress dest, TimeoutId timeoutId, 
                ResponseType responseType,
                int remoteClientID, TimeoutId msgTimeoutId)
        {
            super(src, dest, timeoutId, remoteClientID, msgTimeoutId);
            this.responseType = responseType;
        }

       
        public ResponseType getResponseType()
        {
            return responseType;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 1
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_RESPONSE;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    responseType.ordinal());
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            return new Interleaved_PRP_ConnectMsg.Response(vodSrc, vodDest, 
                    timeoutId, responseType, remoteClientId, msgTimeoutId);
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
