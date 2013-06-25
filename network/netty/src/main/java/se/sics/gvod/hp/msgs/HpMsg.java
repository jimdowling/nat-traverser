package se.sics.gvod.hp.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.VodMsgNetty;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

public abstract class HpMsg extends VodMsgNetty {

    protected final int remoteClientId;
    protected final TimeoutId msgTimeoutId;

    public HpMsg(VodAddress src, VodAddress dest, int remoteClientId, TimeoutId msgTimeoutId) {
        super(src, dest);
        this.remoteClientId = remoteClientId;
        this.msgTimeoutId = msgTimeoutId;
    }
    public HpMsg(VodAddress src, VodAddress dest, TimeoutId timeoutId,
            int remoteClientId, TimeoutId msgTimeoutId) {
        super(src, dest, timeoutId);
        this.remoteClientId = remoteClientId;
        this.msgTimeoutId = msgTimeoutId;
    }

    
    public int getClientId() {
        return vodSrc.getId();
    }    
    
    public int getRemoteClientId() {
        return remoteClientId;
    }

    public TimeoutId getMsgTimeoutId() {
        return msgTimeoutId;
    }

    @Override
    protected int getHeaderSize() {
        return super.getHeaderSize()
                + 4 /* remoteClientId */
                + 4 /* msgTimeoutId*/;
    }

    @Override
    protected void writeHeader(ChannelBuffer buffer) throws MessageEncodingException {
        super.writeHeader(buffer);
        buffer.writeInt(remoteClientId);
        UserTypesEncoderFactory.writeTimeoutId(buffer, msgTimeoutId);
    }
}
