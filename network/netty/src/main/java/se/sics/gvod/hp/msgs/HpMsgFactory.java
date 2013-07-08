package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.util.UserTypesDecoderFactory;
import se.sics.gvod.timer.TimeoutId;

public abstract class HpMsgFactory extends DirectMsgNettyFactory {

    protected int remoteClientId;
    protected TimeoutId msgTimeoutId;

    protected HpMsgFactory() {
    }

    @Override
    protected void decodeHeader(ByteBuf buffer, boolean timeout) throws MessageDecodingException {
        super.decodeHeader(buffer, timeout);
        remoteClientId = buffer.readInt();
        msgTimeoutId = UserTypesDecoderFactory.readTimeoutId(buffer);
    }

    @Override
    protected abstract HpMsg process(ByteBuf buffer) throws MessageDecodingException;
};
