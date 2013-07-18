package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class SHP_OpenHoleMsgFactory {


    public static class Initiator extends HpMsgFactory.Oneway {

        private Initiator() {
        }

        public static SHP_OpenHoleMsg.Initiator fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (SHP_OpenHoleMsg.Initiator)
                    new SHP_OpenHoleMsgFactory.Initiator().decode(buffer);
        }

        @Override
        protected SHP_OpenHoleMsg.Initiator process(ByteBuf buffer) throws MessageDecodingException {

            VodAddress dummyAddr = UserTypesDecoderFactory.readVodAddress(buffer);
            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            SHP_OpenHoleMsg.ResponseType responseType =
                    SHP_OpenHoleMsg.ResponseType.values()[rt];
            return new SHP_OpenHoleMsg.Initiator(vodSrc, vodDest, dummyAddr,
                    responseType,  msgTimeoutId);
        }

    }
}
