package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.msgs.VodMsg;

public class LeaveMsgFactory extends VodMsgNettyFactory {

    private LeaveMsgFactory() {
    }

    public static LeaveMsg fromBuffer(ChannelBuffer buffer)
                
            throws MessageDecodingException {
        return (LeaveMsg) new LeaveMsgFactory().decode(buffer, false);
    }

    @Override
    protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {

        return new LeaveMsg(vodSrc, vodDest);
    }
};
