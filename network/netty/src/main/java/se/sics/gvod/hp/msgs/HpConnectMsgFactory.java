package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.hp.events.OpenConnectionResponseType;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;
import se.sics.gvod.common.hp.HPMechanism;

public class HpConnectMsgFactory {

    public static class Request extends HpMsgFactory {

        private Request() {
        }

        public static HpConnectMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (HpConnectMsg.Request)
                    new HpConnectMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected HpConnectMsg.Request process(ChannelBuffer buffer) throws MessageDecodingException {
            int delta = UserTypesDecoderFactory.readIntAsOneByte(buffer);
            long rtt = (long) UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            return new HpConnectMsg.Request(vodSrc, vodDest, remoteClientId, delta, rtt,
                    msgTimeoutId);
        }
    }

    public static class Response extends HpMsgFactory {

        private Response() {
        }

        public static HpConnectMsg.Response fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (HpConnectMsg.Response)
                    new HpConnectMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected HpConnectMsg.Response process(ChannelBuffer buffer) throws MessageDecodingException {
            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            OpenConnectionResponseType responseType =
                    OpenConnectionResponseType.values()[rt];
            int hm = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HPMechanism hpMechanism =
                    HPMechanism.values()[hm];
            boolean newSession = UserTypesDecoderFactory.readBoolean(buffer);

            return new HpConnectMsg.Response(vodSrc, vodDest, remoteClientId,
                    responseType, timeoutId, hpMechanism, newSession, msgTimeoutId);
        }

    }
}
