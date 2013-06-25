package se.sics.gvod.common.msgs;

import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class ReferencesMsgFactory {

    public static class Request extends VodMsgNettyFactory {

        private Request() {
        }

        public static ReferencesMsg.Request fromBuffer(ChannelBuffer buffer)
                 throws MessageDecodingException {
            return (ReferencesMsg.Request) new ReferencesMsgFactory.Request().decode(buffer, 
                    true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            int ref = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            UtilityVod utility = (UtilityVod) UserTypesDecoderFactory.readUtility(buffer);
            List<VodAddress> children = UserTypesDecoderFactory.readListVodAddresses(buffer);
            ReferencesMsg.Request msg = new ReferencesMsg.Request(vodSrc, vodDest, 
                    timeoutId, ref, utility, children);
            return msg;
        }
    }

    public static class Response extends VodMsgNettyFactory {

        private Response() {
        }

        public static ReferencesMsg.Response fromBuffer(ChannelBuffer buffer)
                 throws MessageDecodingException {
            return (ReferencesMsg.Response) 
                    new ReferencesMsgFactory.Response().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            int ref = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
            UtilityVod utility = (UtilityVod) UserTypesDecoderFactory.readUtility(buffer);
            List<VodAddress> children = UserTypesDecoderFactory.readListVodAddresses(buffer);
            return new ReferencesMsg.Response(vodSrc, vodDest,
                    timeoutId, ref, utility, children);
        }
    }
};
