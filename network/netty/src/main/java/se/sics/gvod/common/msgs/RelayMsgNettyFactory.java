package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;

import java.util.Set;

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
        protected void decodeHeader(ByteBuf buffer, boolean timeout)
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
    }

    public abstract static class Response extends Base {

        protected RelayMsgNetty.Status status;

        protected Response() {
        }
        
        @Override
        protected void decodeHeader(ByteBuf buffer, boolean timeout)
                throws MessageDecodingException {
            super.decodeHeader(buffer, timeout);
            int statusVal = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            status = RelayMsgNetty.Status.create(statusVal);
        }        
    }

    public abstract static class Oneway extends Base {
    }
};
