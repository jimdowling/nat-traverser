package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;

import java.util.Set;

import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class HpRegisterMsgFactory {

    public static class Request extends DirectMsgNettyFactory.SystemRequest {

        private Request() {
        }

        public static HpRegisterMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (HpRegisterMsg.Request)
                    new HpRegisterMsgFactory.Request().decode(buffer);
        }

        @Override
        protected HpRegisterMsg.Request process(ByteBuf buffer) throws MessageDecodingException {
            int delta = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            long rtt = (long) UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            Set<Integer> prpPorts = UserTypesDecoderFactory.readSetUnsignedTwoByteInts(buffer);
            return new HpRegisterMsg.Request(vodSrc, vodDest, delta, rtt, prpPorts);
        }
    }

    public static class Response extends DirectMsgNettyFactory.SystemResponse {

        private Response() {
        }

        public static HpRegisterMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (HpRegisterMsg.Response)
                    new HpRegisterMsgFactory.Response().decode(buffer);
        }

        @Override
        protected HpRegisterMsg.Response process(ByteBuf buffer) throws MessageDecodingException {

            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HpRegisterMsg.RegisterStatus responseType =
                    HpRegisterMsg.RegisterStatus.values()[rt];
            Set<Integer> prpPorts = UserTypesDecoderFactory.readSetUnsignedTwoByteInts(buffer);
            return new HpRegisterMsg.Response(vodSrc, vodDest,
                    responseType, timeoutId, prpPorts);
        }

    }
}
