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
 * z server sends this message to the initiator to start SHP
 * direction of msg: server ----> client
 */
public class SHP_InitiateSimpleHolePunchingMsg
{
    public final static class Request extends HpMsg
    {
        static final long serialVersionUID = 1L;
        private final HPMechanism holePunchingMechanism;
        private final HPRole holePunchingRole;

        public Request(VodAddress src, VodAddress dest, int remoteClientId,
                HPMechanism holePunchingMechanism, HPRole holePunchingRole,
                TimeoutId msgTimeoutId)
        {
            super(src, dest, remoteClientId, msgTimeoutId);
            this.holePunchingMechanism =holePunchingMechanism;
            this.holePunchingRole=holePunchingRole;
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
        public int getSize() {
            return getHeaderSize()
                    + 1
                    + 1
                    ;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.SHP_INITIATE_SHP;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
        	UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, 
        			holePunchingMechanism.ordinal());
        	UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, 
        			holePunchingRole.ordinal());
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            SHP_InitiateSimpleHolePunchingMsg.Request copy = new 
                    SHP_InitiateSimpleHolePunchingMsg.Request(vodSrc, vodDest, 
                    remoteClientId, holePunchingMechanism, holePunchingRole,
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
