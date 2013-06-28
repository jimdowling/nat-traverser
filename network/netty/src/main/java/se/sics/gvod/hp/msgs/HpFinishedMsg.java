/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.hp.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author Jim
 *
 * message send by the client to zServer
 */
public class HpFinishedMsg extends HpMsg
{
    static final long serialVersionUID = 1L;
    private final boolean hpSuccessful;

    public HpFinishedMsg(VodAddress src, VodAddress dest, int remoteClientId, boolean hpSuccessful,
            TimeoutId msgTimeoutId)
    {
        super(src, dest, remoteClientId, msgTimeoutId);
        this.hpSuccessful = hpSuccessful;
    }

    public boolean isHPSuccessful()
    {
        return hpSuccessful;
    }

    @Override
    public int getSize()
    {
      return getHeaderSize()
                + 1 /* hpsuccessful */;
    }


    @Override
    public byte getOpcode() {
        return BaseMsgFrameDecoder.HP_FINISHED;
    }

    @Override
    public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeBoolean(buffer, hpSuccessful);
            return buffer;
    }

    @Override
    public RewriteableMsg copy() {
        HpFinishedMsg copy = new HpFinishedMsg(vodSrc, vodDest, remoteClientId, hpSuccessful,
                msgTimeoutId);
        copy.setTimeoutId(timeoutId);
        return copy;
    }
}