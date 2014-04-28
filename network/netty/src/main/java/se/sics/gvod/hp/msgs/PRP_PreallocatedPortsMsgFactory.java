package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;

import java.util.Set;

import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class PRP_PreallocatedPortsMsgFactory {

    public static class Request extends HpMsgFactory.Request {

        private Request() {
        }

        public static PRP_PreallocatedPortsMsg.Request fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (PRP_PreallocatedPortsMsg.Request)
                    new PRP_PreallocatedPortsMsgFactory.Request().decode(buffer);
        }

        @Override
        protected PRP_PreallocatedPortsMsg.Request process(ByteBuf buffer) throws MessageDecodingException {

            return new PRP_PreallocatedPortsMsg.Request(vodSrc, vodDest, msgTimeoutId);
        }
    }

    public static class Response extends HpMsgFactory.Response {

        private Response() {
        }

        public static PRP_PreallocatedPortsMsg.Response fromBuffer(ByteBuf buffer)
                
                throws MessageDecodingException {
            return (PRP_PreallocatedPortsMsg.Response)
                    new PRP_PreallocatedPortsMsgFactory.Response().decode(buffer);
        }

        @Override
        protected PRP_PreallocatedPortsMsg.Response process(ByteBuf buffer) 
                throws MessageDecodingException 
        {
            int rt = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            PRP_PreallocatedPortsMsg.ResponseType responseType =
                    PRP_PreallocatedPortsMsg.ResponseType.values()[rt];
            Set<Integer> prpPorts = UserTypesDecoderFactory.readSetUnsignedTwoByteInts(buffer);
            return new PRP_PreallocatedPortsMsg.Response(vodSrc, vodDest, timeoutId,
                    responseType, prpPorts, msgTimeoutId);
        }

    }
}
