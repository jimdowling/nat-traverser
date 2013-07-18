package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class HpFinishedMsgFactory {

    public static class Request extends HpMsgFactory.Oneway {

        private Request() {
        }

        public static HpFinishedMsg fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (HpFinishedMsg)
                    new HpFinishedMsgFactory.Request().decode(buffer);
        }

        @Override
        protected HpFinishedMsg process(ByteBuf buffer) throws MessageDecodingException {
            boolean success = UserTypesDecoderFactory.readBoolean(buffer);
            return new HpFinishedMsg(vodSrc, vodDest, remoteClientId, success, msgTimeoutId);
        }
    }

}
