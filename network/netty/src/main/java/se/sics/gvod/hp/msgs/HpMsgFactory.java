package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.util.UserTypesDecoderFactory;
import se.sics.gvod.timer.TimeoutId;

public abstract class HpMsgFactory extends VodMsgNettyFactory {

    protected int remoteClientId;
    protected TimeoutId msgTimeoutId;

    protected HpMsgFactory() {
    }

    @Override
    protected void decodeHeader(ChannelBuffer buffer, boolean timeout) throws MessageDecodingException {
        super.decodeHeader(buffer, timeout);
        remoteClientId = buffer.readInt();
        msgTimeoutId = UserTypesDecoderFactory.readTimeoutId(buffer);
    }

    @Override
    protected abstract HpMsg process(ChannelBuffer buffer) throws MessageDecodingException;
};
