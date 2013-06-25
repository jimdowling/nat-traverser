package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;

public class DeleteConnectionMsgFactory extends HpMsgFactory {

    protected DeleteConnectionMsgFactory() {
    }

    public static DeleteConnectionMsg fromBuffer(ChannelBuffer buffer)
            throws MessageDecodingException {
        return (DeleteConnectionMsg) new DeleteConnectionMsgFactory().decode(buffer, false);
    }

    @Override
    protected HpMsg process(ChannelBuffer buffer) throws MessageDecodingException {
        return new DeleteConnectionMsg(vodSrc, vodDest, remoteClientId, msgTimeoutId);
    }

}
