package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class HpFinishedMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static HpFinishedMsg fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (HpFinishedMsg)
                    new HpFinishedMsgFactory.Request().decode(buffer, false);
        }

        @Override
        protected HpFinishedMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            boolean success = UserTypesDecoderFactory.readBoolean(buffer);
            return new HpFinishedMsg(vodSrc, vodDest, remoteClientId, success, msgTimeoutId);
        }
    }

}
