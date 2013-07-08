package se.sics.gvod.stun.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public abstract class StunResponseMsgFactory extends DirectMsgNettyFactory {

    protected long transactionId;
    protected Address retryPubAddr;

    protected StunResponseMsgFactory() {
    }

    @Override
    public void decodeHeader(ByteBuf buffer, boolean timeout)
            throws MessageDecodingException {
        super.decodeHeader(buffer, timeout);
        transactionId = buffer.readLong();
        retryPubAddr = UserTypesDecoderFactory.readAddress(buffer);
    }

};
