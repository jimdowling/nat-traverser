package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class Interleaved_PRC_ServersRequestForPredictionMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static Interleaved_PRC_ServersRequestForPredictionMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (Interleaved_PRC_ServersRequestForPredictionMsg.Request)
                    new Interleaved_PRC_ServersRequestForPredictionMsgFactory.Request().decode(buffer, false);
        }

        @Override
        protected Interleaved_PRC_ServersRequestForPredictionMsg.Request process(ChannelBuffer buffer) throws MessageDecodingException {
            int hm = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HPMechanism hpMech = HPMechanism.values()[hm];
            int hr = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HPRole hpRole = HPRole.values()[hr];
            VodAddress hole = UserTypesDecoderFactory.readVodAddress(buffer);
            return new Interleaved_PRC_ServersRequestForPredictionMsg.Request(vodSrc, vodDest,
                    remoteClientId, hpMech, hpRole, hole, msgTimeoutId);
        }
    }


}
