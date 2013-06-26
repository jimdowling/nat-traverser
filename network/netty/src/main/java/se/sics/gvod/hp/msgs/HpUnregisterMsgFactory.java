package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class HpUnregisterMsgFactory {

    public static class Request extends VodMsgNettyFactory {

        private Request() {
        }

        public static HpUnregisterMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (HpUnregisterMsg.Request)
                    new HpUnregisterMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected HpUnregisterMsg.Request process(ChannelBuffer buffer) throws MessageDecodingException {
            int delay = buffer.readInt();
            int s = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HpRegisterMsg.RegisterStatus status = HpRegisterMsg.RegisterStatus.values()[s];
            return new HpUnregisterMsg.Request(vodSrc, vodDest, delay, status);
        }
    }

    public static class Response extends VodMsgNettyFactory {

        private Response() {
        }

        public static HpUnregisterMsg.Response fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (HpUnregisterMsg.Response)
                    new HpUnregisterMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected HpUnregisterMsg.Response process(ChannelBuffer buffer) throws MessageDecodingException {

            int status = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HpUnregisterMsg.Response.Status statusType =
                    HpUnregisterMsg.Response.Status.values()[status];
            return new HpUnregisterMsg.Response(vodSrc, vodDest,
                    statusType, timeoutId);
        }

    }
}
