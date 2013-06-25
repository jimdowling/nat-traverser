package se.sics.gvod.stun.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.msgs.VodMsgNettyFactory;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public abstract class StunResponseMsgFactory extends VodMsgNettyFactory {

    protected long transactionId;
    protected Address retryPubAddr;

    protected StunResponseMsgFactory() {
    }

    @Override
    public void decodeHeader(ChannelBuffer buffer, boolean timeout)
            throws MessageDecodingException {
        super.decodeHeader(buffer, timeout);
        transactionId = buffer.readLong();
        retryPubAddr = UserTypesDecoderFactory.readAddress(buffer);
    }

};
