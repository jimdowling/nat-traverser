package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class PRP_ServerRequestForAvailablePortsMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static PRP_ServerRequestForAvailablePortsMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (PRP_ServerRequestForAvailablePortsMsg.Request)
                    new PRP_ServerRequestForAvailablePortsMsgFactory.Request().decode(buffer, false);
        }

        @Override
        protected PRP_ServerRequestForAvailablePortsMsg.Request process(ChannelBuffer buffer) throws MessageDecodingException {

            int hm = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            HPMechanism holePunchingMechanism = HPMechanism.values()[hm];
            int hr = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            HPRole holePunchingRole = HPRole.values()[hr];
            return new PRP_ServerRequestForAvailablePortsMsg.Request(vodSrc, vodDest,
                    remoteClientId, holePunchingMechanism, holePunchingRole, msgTimeoutId);
        }
    }


}
