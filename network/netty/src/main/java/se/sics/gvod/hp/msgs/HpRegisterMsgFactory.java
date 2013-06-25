package se.sics.gvod.hp.msgs;

import java.util.Set;
import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class HpRegisterMsgFactory {

    public static class Request extends VodMsgNettyFactory {

        private Request() {
        }

        public static HpRegisterMsg.Request fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (HpRegisterMsg.Request)
                    new HpRegisterMsgFactory.Request().decode(buffer, true);
        }

        @Override
        protected HpRegisterMsg.Request process(ChannelBuffer buffer) throws MessageDecodingException {
            int delta = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            long rtt = (long) UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            Set<Integer> prpPorts = UserTypesDecoderFactory.readSetUnsignedTwoByteInts(buffer);
            return new HpRegisterMsg.Request(vodSrc, vodDest, delta, rtt, prpPorts);
        }
    }

    public static class Response extends VodMsgNettyFactory {

        private Response() {
        }

        public static HpRegisterMsg.Response fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (HpRegisterMsg.Response)
                    new HpRegisterMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected HpRegisterMsg.Response process(ChannelBuffer buffer) throws MessageDecodingException {

            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            HpRegisterMsg.RegisterStatus responseType =
                    HpRegisterMsg.RegisterStatus.values()[rt];
            Set<Integer> prpPorts = UserTypesDecoderFactory.readSetUnsignedTwoByteInts(buffer);
            return new HpRegisterMsg.Response(vodSrc, vodDest,
                    responseType, timeoutId, prpPorts);
        }

    }
}
