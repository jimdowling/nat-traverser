package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class PRP_ServerRequestForAvailablePortsMsgFactory {

    public static class Request extends HpMsgFactory.Oneway {

        private Request() {
        }

        public static PRP_ServerRequestForAvailablePortsMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (PRP_ServerRequestForAvailablePortsMsg.Request)
                    new PRP_ServerRequestForAvailablePortsMsgFactory.Request().decode(buffer);
        }

        @Override
        protected PRP_ServerRequestForAvailablePortsMsg.Request process(ByteBuf buffer) throws MessageDecodingException {

            int hm = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            HPMechanism holePunchingMechanism = HPMechanism.values()[hm];
            int hr = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            HPRole holePunchingRole = HPRole.values()[hr];
            return new PRP_ServerRequestForAvailablePortsMsg.Request(vodSrc, vodDest,
                    remoteClientId, holePunchingMechanism, holePunchingRole, msgTimeoutId);
        }
    }


}
