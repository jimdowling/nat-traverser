package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
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
 * z server sends this message to the initiator of PRP.
 * in response to this message the client must send some available ports to the
 * zServer. zServer will select one open port and inform the client about it
 *
 */
public class Interleaved_PRC_ServersRequestForPredictionMsg
{
    public final static class Request extends HpMsg
    {
        static final long serialVersionUID = 1L;

        private final HPMechanism holePunchingMechanism;
        private final HPRole holePunchingRole;
        private final VodAddress hole;

        public Request(VodAddress src, VodAddress dest, int remoteClientID,
                HPMechanism holePunchingMechanism,
                HPRole holePunchingRole, VodAddress hole, 
                TimeoutId msgTimeoutId)
        {
            super(src, dest, remoteClientID, msgTimeoutId);
            this.holePunchingMechanism =holePunchingMechanism;
            this.holePunchingRole=holePunchingRole;
            this.hole = hole;
        }

        public VodAddress getHole() {
            return hole;
        }
        
        public HPMechanism getHolePunchingMechanism()
        {
            return holePunchingMechanism;
        }

        public HPRole getHolePunchingRole()
        {
            return holePunchingRole;
        }

        @Override
	public int getSize()
	{
		return getHeaderSize()
                        + 1
                        + 1
                        + UserTypesEncoderFactory.VOD_ADDRESS_LEN_NO_PARENTS
                        ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.INTERLEAVED_PRC_SERVER_REQ_PRED_MSG;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, holePunchingMechanism.ordinal());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, holePunchingRole.ordinal());
            UserTypesEncoderFactory.writeVodAddress(buffer, hole);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            Interleaved_PRC_ServersRequestForPredictionMsg.Request copy = new
                    Interleaved_PRC_ServersRequestForPredictionMsg.Request(vodSrc, 
                    vodDest, remoteClientId, holePunchingMechanism, holePunchingRole, hole,
                    msgTimeoutId);
            copy.setTimeoutId(timeoutId);
            return copy;
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
