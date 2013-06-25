package se.sics.gvod.stun.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.VodMsgNetty;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.TimeoutId;

public abstract class StunRequestMsg extends VodMsgNetty {

    protected final long transactionId;

    public StunRequestMsg(VodAddress src, VodAddress dest, long transactionId) {
        super(src, dest);
        this.transactionId = transactionId;
    }
    
    public StunRequestMsg(VodAddress src, VodAddress dest, long transactionId, TimeoutId timeoutId) {
        super(src, dest, timeoutId);
        this.transactionId = transactionId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    @Override
    protected int getHeaderSize() {
        return super.getHeaderSize()
                + 8 /* transactionId */
                ;
    }

    @Override
    protected void writeHeader(ChannelBuffer buffer) throws MessageEncodingException {
        super.writeHeader(buffer);
        buffer.writeLong(transactionId);
    }
}
