package se.sics.gvod.hp.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.OpCode;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;

/**
 * 
 * z server sends this message to the initiator to start SHP
 * direction of msg: server ----> client
 */
public class GoMsg
{
    public final static class Request extends HpMsg
    {
        static final long serialVersionUID = 13245324567L;
        private final VodAddress openedHole;
        private final HPMechanism holePunchingMechanism;
        private final HPRole holePunchingRole;
        private int PRP_PRP_interleavedPort;
        private int numRetries;
        private boolean bindPort;

        public Request(VodAddress src, VodAddress dest,
                VodAddress openedHole,
                HPMechanism holePunchingMechanism,
                HPRole holePunchingRole, int numRetries,
                TimeoutId msgTimeoutId)
        {
            this(src, dest, openedHole, holePunchingMechanism,
                    holePunchingRole, numRetries, 0, false, msgTimeoutId);
        }
        
        public Request(VodAddress src, VodAddress dest,
                VodAddress openedHole,
                HPMechanism holePunchingMechanism,
                HPRole holePunchingRole, int numRetries,
                int PRP_PRP_interleavedPort,
                boolean bindPort,
                TimeoutId msgTimeoutId)
        {
            super(src, dest, openedHole.getId(), msgTimeoutId);
            this.openedHole = openedHole;
            this.holePunchingMechanism = holePunchingMechanism;
            this.holePunchingRole = holePunchingRole;
            this.numRetries = numRetries;
            this.PRP_PRP_interleavedPort = PRP_PRP_interleavedPort;
            this.bindPort = bindPort;
        }

        public boolean isBindPort() {
            return bindPort;
        }
        
        public int getNumRetries() {
            return numRetries;
        }
        
        public int getRemoteId() {
            return openedHole.getId();
        }
        
        public int get_PRP_PRP_InterleavedPort()
        {
            return PRP_PRP_interleavedPort;
        }

        public HPRole getHolePunchingRole()
        {
            return holePunchingRole;
        }

        public HPMechanism getHolePunchingMechanism()
        {
            return holePunchingMechanism;
        }

        public VodAddress getOpenedHole()
        {
            return openedHole;
        }

        @Override
	public int getSize()
	{
		return getHeaderSize()
                        + UserTypesEncoderFactory.ADDRESS_LEN
                        + 1 /* HPMechanism size */
                        + 2 /* HPRole size */
                        + 2 /* prp_prp_interleavedPort */
                        ;
	}


        @Override
        public OpCode getOpcode() {
            return OpCode.GO_MSG;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeVodAddress(buffer, openedHole);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    holePunchingMechanism.ordinal());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    holePunchingRole.ordinal());
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer,
                    PRP_PRP_interleavedPort);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    numRetries);
            UserTypesEncoderFactory.writeBoolean(buffer, bindPort);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            GoMsg.Request copy = new GoMsg.Request(vodSrc, vodDest, openedHole,
                    holePunchingMechanism, holePunchingRole, numRetries, 
                    PRP_PRP_interleavedPort, bindPort,
                    msgTimeoutId);
            copy.setTimeoutId(timeoutId);
            return copy;            
        }

    }

    public static final class RequestTimeout extends Timeout
    {

        private final Request requestMsg;

        public RequestTimeout(ScheduleTimeout st, Request requestMsg)
        {
            super(st);
            this.requestMsg = requestMsg;
        }

        public Request getRequestMsg()
        {
            return requestMsg;
        }

    }

}
