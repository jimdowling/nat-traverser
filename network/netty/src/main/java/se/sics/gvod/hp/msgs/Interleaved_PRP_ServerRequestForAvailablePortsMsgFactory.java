package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class Interleaved_PRP_ServerRequestForAvailablePortsMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static Interleaved_PRP_ServerRequestForAvailablePortsMsg.Request fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (Interleaved_PRP_ServerRequestForAvailablePortsMsg.Request)
                    new Interleaved_PRP_ServerRequestForAvailablePortsMsgFactory.Request().decode(buffer, false);
        }

        @Override
        protected Interleaved_PRP_ServerRequestForAvailablePortsMsg.Request process(ByteBuf buffer) throws MessageDecodingException {

            int hm = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            HPMechanism holePunchingMechanism = HPMechanism.values()[hm];
            int hr = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            HPRole holePunchingRole = HPRole.values()[hr];
            return new Interleaved_PRP_ServerRequestForAvailablePortsMsg.Request(
                    vodSrc, vodDest, remoteClientId, holePunchingMechanism, holePunchingRole,
                    msgTimeoutId);
        }
    }


}
