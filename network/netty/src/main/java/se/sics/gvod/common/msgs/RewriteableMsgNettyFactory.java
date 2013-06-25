package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.timer.NoTimeoutId;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.UUID;

public abstract class RewriteableMsgNettyFactory {

    protected TimeoutId timeoutId;
    protected Address src, dest;


    protected RewriteableMsg decode(ChannelBuffer buffer, boolean timeout) throws MessageDecodingException {
        decodeHeader(buffer, timeout);
        RewriteableMsg msg = process(buffer);
        finish(msg);
        return msg;
    }

    protected void finish(RewriteableMsg msg) {
        msg.setTimeoutId(timeoutId);
    }
    
    protected void decodeHeader(ChannelBuffer buffer, boolean timeout)
            throws MessageDecodingException {
        if (timeout) { 
            timeoutId = new UUID(buffer.readInt());
        } else {
            timeoutId = new NoTimeoutId();
        }
        int srcId = buffer.readInt();
        int destId = buffer.readInt();
        src = new Address(srcId);
        dest = new Address(destId);
    }

    protected abstract RewriteableMsg process(ChannelBuffer buffer) throws MessageDecodingException;    
    

};
