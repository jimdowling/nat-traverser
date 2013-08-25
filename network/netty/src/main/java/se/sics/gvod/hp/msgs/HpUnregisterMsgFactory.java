package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class HpUnregisterMsgFactory {

    public static class Request extends DirectMsgNettyFactory.Request {

        private Request() {
        }

        public static HpUnregisterMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (HpUnregisterMsg.Request)
                    new HpUnregisterMsgFactory.Request().decode(buffer);
        }

        @Override
        protected HpUnregisterMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            int delay = buffer.readInt();
            int s = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HpRegisterMsg.RegisterStatus status = HpRegisterMsg.RegisterStatus.values()[s];
            return new HpUnregisterMsg.Request(vodSrc, vodDest, delay, status);
        }
    }

    public static class Response extends DirectMsgNettyFactory.Response {

        private Response() {
        }

        public static HpUnregisterMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (HpUnregisterMsg.Response)
                    new HpUnregisterMsgFactory.Response().decode(buffer);
        }

        @Override
        protected HpUnregisterMsg.Response process(ByteBuf buffer) throws MessageDecodingException {

            int status = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HpUnregisterMsg.Response.Status statusType =
                    HpUnregisterMsg.Response.Status.values()[status];
            return new HpUnregisterMsg.Response(vodSrc, vodDest,
                    statusType, timeoutId);
        }

    }
}
