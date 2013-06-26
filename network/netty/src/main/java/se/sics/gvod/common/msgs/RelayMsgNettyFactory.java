package se.sics.gvod.common.msgs;

import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public abstract class RelayMsgNettyFactory {

    static abstract class Base extends RewriteableMsgNettyFactory {

        protected VodAddress gvodDest;
        protected VodAddress gvodSrc;
        protected VodAddress nextDest;
        protected int clientId;
        protected int remoteId;

        protected Base() {
            super();
        }

        @Override
        protected void decodeHeader(ChannelBuffer buffer, boolean timeout)
                throws MessageDecodingException {
            super.decodeHeader(buffer, timeout);

            int srcOverlayId = buffer.readInt();
            int srcNatPolicy = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            Set<Address> parents = UserTypesDecoderFactory.readListAddresses(buffer);
            int destOverlayId = buffer.readInt();
            int destNatPolicy = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);

            gvodSrc = new VodAddress(src, srcOverlayId, (short) srcNatPolicy, parents);
            gvodDest = new VodAddress(dest, destOverlayId, (short) destNatPolicy, null);
            nextDest = UserTypesDecoderFactory.readVodAddress(buffer);
            clientId = buffer.readInt();
            remoteId = buffer.readInt();
        }
    }

    public abstract static class Request extends Base {
//
//        protected Request() {
//            super();
//        }
//
//        public static RelayMsgNetty.Request fromBuffer(ChannelBuffer buffer)
//                throws MessageDecodingException {
//            return (RelayMsgNetty.Request) new RelayMsgNettyFactory.Request().decode(buffer,
//                    true);
//        }
//
//        @Override
//        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
//            return new RelayMsgNetty.Request(gvodSrc, gvodDest, clientId, remoteId,
//                    nextDest, 0 /*timeoutInMs*/, 0 /*retries*/, 1.0d /*rtoScale*/,  timeoutId);
//        }
    }

    public abstract static class Response extends Base {

        protected RelayMsgNetty.Status status;

        protected Response() {
        }
        
        @Override
        protected void decodeHeader(ChannelBuffer buffer, boolean timeout)
                throws MessageDecodingException {
            super.decodeHeader(buffer, timeout);
            int statusVal = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            status = RelayMsgNetty.Status.create(statusVal);
        }        

//        public static RelayMsgNetty.Response fromBuffer(ChannelBuffer buffer)
//                throws MessageDecodingException {
//            return (RelayMsgNetty.Response) new RelayMsgNettyFactory.Response().decode(buffer,
//                    true);
//        }

//            @Override
//    protected abstract RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException;
        
//        @Override
//        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
//            int statusVal = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
//            status = RelayMsgNetty.Status.create(statusVal);
//            return new RelayMsgNetty.Response(gvodSrc, gvodDest, clientId, remoteId,
//                    nextDest, timeoutId, status);
//        }
    }

    public abstract static class Oneway extends Base {
//
//        protected Oneway() {
//            super();
//        }
//
//        public static RelayMsgNetty.Oneway fromBuffer(ChannelBuffer buffer)
//                throws MessageDecodingException {
//            return (RelayMsgNetty.Oneway) new RelayMsgNettyFactory.Oneway().decode(buffer,
//                    false);
//        }
//
//        @Override
//        protected RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException {
//            return new RelayMsgNetty.Oneway(gvodSrc, gvodDest, clientId, remoteId, nextDest);
//        }
    }
};
