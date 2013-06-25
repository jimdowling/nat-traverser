package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class SHP_OpenHoleMsgFactory {


    public static class Initiator extends HpMsgFactory {

        private Initiator() {
        }

        public static SHP_OpenHoleMsg.Initiator fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (SHP_OpenHoleMsg.Initiator)
                    new SHP_OpenHoleMsgFactory.Initiator().decode(buffer, false);
        }

        @Override
        protected SHP_OpenHoleMsg.Initiator process(ChannelBuffer buffer) throws MessageDecodingException {

            VodAddress dummyAddr = UserTypesDecoderFactory.readVodAddress(buffer);
            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            SHP_OpenHoleMsg.ResponseType responseType =
                    SHP_OpenHoleMsg.ResponseType.values()[rt];
            return new SHP_OpenHoleMsg.Initiator(vodSrc, vodDest, dummyAddr,
                    responseType,  msgTimeoutId);
        }

    }
}
