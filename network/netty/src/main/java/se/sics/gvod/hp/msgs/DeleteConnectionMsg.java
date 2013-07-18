package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.timer.TimeoutId;

/**
 * 
 * @author salman
 * z server sends this message to the initiator to start SHP
 * direction of msg: server ----> client
 */
public class DeleteConnectionMsg extends HpMsg.Oneway {

    static final long serialVersionUID = 8457363545748L;

    public DeleteConnectionMsg(VodAddress src, VodAddress dest, int remoteClientId,
            TimeoutId msgTimeoutId) {
        super(src, dest, remoteClientId, msgTimeoutId);
    }

    @Override
    public int getSize() {
        return getHeaderSize();
    }

    @Override
    public ByteBuf toByteArray() throws MessageEncodingException {
    	ByteBuf buffer = createChannelBufferWithHeader();
        return buffer;
    }

    @Override
    public byte getOpcode() {
        return BaseMsgFrameDecoder.DELETE_CONNECTION;
    }

    @Override
    public RewriteableMsg copy() {
        DeleteConnectionMsg copy = new DeleteConnectionMsg(vodSrc, vodDest, remoteClientId,
                msgTimeoutId);
        copy.setTimeoutId(timeoutId);
        return copy;
    }
}
