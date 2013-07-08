package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class GoMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static GoMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (GoMsg.Request)
                    new GoMsgFactory.Request().decode(buffer, false);
        }

        @Override
        protected GoMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            VodAddress openedHole = UserTypesDecoderFactory.readVodAddress(buffer);
            int hpM = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HPMechanism holePunchingMechanism = HPMechanism.values()[hpM];
            int hpR = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HPRole holePunchingRole = HPRole.values()[hpR];
            int PRP_PRP_interleavedPort =
                    UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            int rtoRetries =
                    UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            boolean bindPort = UserTypesDecoderFactory.readBoolean(buffer);
            GoMsg.Request msg = new GoMsg.Request(vodSrc, vodDest, openedHole, 
                    holePunchingMechanism,
                    holePunchingRole, rtoRetries, PRP_PRP_interleavedPort, bindPort,
                    msgTimeoutId);
            return msg;
        }

    }

}
