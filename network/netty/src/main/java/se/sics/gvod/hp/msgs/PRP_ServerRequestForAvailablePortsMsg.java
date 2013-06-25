package se.sics.gvod.hp.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.OpCode;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 * 
 * @author salman
 * z server sends this message to the initiator of PRP.
 * in response to this message the client must send some available ports to the
 * zServer. zServer will select one open port and inofrm the client abt it
 *
 */
public class PRP_ServerRequestForAvailablePortsMsg
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
                    +1
                    +1
                    ;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.PRP_SEND_PORTS_ZSERVER_REQUEST;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    holePunchingMechanism.ordinal());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    holePunchingRole.ordinal());
            return buffer;

        }

        @Override
        public RewriteableMsg copy() {
            PRP_ServerRequestForAvailablePortsMsg.Request copy = new 
                    PRP_ServerRequestForAvailablePortsMsg.Request(vodSrc, vodDest, 
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
