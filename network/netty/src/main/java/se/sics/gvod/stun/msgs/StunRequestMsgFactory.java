package se.sics.gvod.stun.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.VodMsgNettyFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;

public abstract class StunRequestMsgFactory extends VodMsgNettyFactory
{

    protected long transactionId;

    protected StunRequestMsgFactory() {
    }

    @Override
    protected void decodeHeader(ChannelBuffer buffer, boolean timeout) throws MessageDecodingException {
        super.decodeHeader(buffer, timeout);
        transactionId = buffer.readLong();
    }

};
