package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class SHP_InitiateSimpleHolePunchingMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static SHP_InitiateSimpleHolePunchingMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (SHP_InitiateSimpleHolePunchingMsg.Request)
                    new SHP_InitiateSimpleHolePunchingMsgFactory.Request().decode(buffer, false);
        }

        @Override
        protected SHP_InitiateSimpleHolePunchingMsg.Request process(ChannelBuffer buffer) throws MessageDecodingException {

            int hm = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            HPMechanism holePunchingMechanism = HPMechanism.values()[hm];
            int hr = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            HPRole holePunchingRole = HPRole.values()[hr];
            return new SHP_InitiateSimpleHolePunchingMsg.Request(vodSrc, vodDest,
                    remoteClientId, holePunchingMechanism, holePunchingRole, msgTimeoutId);
        }
    }


}
