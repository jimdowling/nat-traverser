package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class PRC_ServerRequestForConsecutiveMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static PRC_ServerRequestForConsecutiveMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (PRC_ServerRequestForConsecutiveMsg.Request)
                    new PRC_ServerRequestForConsecutiveMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected PRC_ServerRequestForConsecutiveMsg.Request process(ChannelBuffer buffer) throws MessageDecodingException {

            int hm = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            HPMechanism holePunchingMechanism = HPMechanism.values()[hm];
            int hr = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            HPRole holePunchingRole = HPRole.values()[hr];
            VodAddress remoteAddr = UserTypesDecoderFactory.readVodAddress(buffer);
            return new PRC_ServerRequestForConsecutiveMsg.Request(vodSrc, vodDest,
                    remoteClientId, holePunchingMechanism, holePunchingRole, remoteAddr,
                    msgTimeoutId);
        }
    }


}
