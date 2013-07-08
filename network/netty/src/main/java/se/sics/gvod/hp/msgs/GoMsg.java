package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
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
        private int rtoRetries;
        private boolean bindPort;

        public Request(VodAddress src, VodAddress dest,
                VodAddress openedHole,
                HPMechanism holePunchingMechanism,
                HPRole holePunchingRole, int rtoRetries,
                TimeoutId msgTimeoutId)
        {
            this(src, dest, openedHole, holePunchingMechanism,
                    holePunchingRole, rtoRetries, 0, false, msgTimeoutId);
        }
        
        public Request(VodAddress src, VodAddress dest,
                VodAddress openedHole,
                HPMechanism holePunchingMechanism,
                HPRole holePunchingRole, int rtoRetries,
                int PRP_PRP_interleavedPort,
                boolean bindPort,
                TimeoutId msgTimeoutId)
        {
            super(src, dest, openedHole.getId(), msgTimeoutId);
            this.openedHole = openedHole;
            this.holePunchingMechanism = holePunchingMechanism;
            this.holePunchingRole = holePunchingRole;
            this.rtoRetries = rtoRetries;
            this.PRP_PRP_interleavedPort = PRP_PRP_interleavedPort;
            this.bindPort = bindPort;
        }

        public boolean isBindPort() {
            return bindPort;
        }
        
        public int getRtoRetries() {
            return rtoRetries;
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
        public byte getOpcode() {
            return BaseMsgFrameDecoder.GO_MSG;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeVodAddress(buffer, openedHole);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    holePunchingMechanism.ordinal());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    holePunchingRole.ordinal());
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer,
                    PRP_PRP_interleavedPort);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer,
                    rtoRetries);
            UserTypesEncoderFactory.writeBoolean(buffer, bindPort);
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            GoMsg.Request copy = new GoMsg.Request(vodSrc, vodDest, openedHole,
                    holePunchingMechanism, holePunchingRole, rtoRetries, 
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
