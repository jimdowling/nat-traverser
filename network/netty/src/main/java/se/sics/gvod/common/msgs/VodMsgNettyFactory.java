package se.sics.gvod.common.msgs;

import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.MsgFrameDecoder;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;
import se.sics.gvod.timer.NoTimeoutId;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.UUID;

public abstract class VodMsgNettyFactory 
{

    protected VodAddress vodDest;
    protected VodAddress vodSrc;
    protected TimeoutId timeoutId;
    protected Address src, dest;
    protected static Class<? extends MsgFrameDecoder> msgFrameDecoder;

    public static void setMsgFrameDecoder(Class<? extends MsgFrameDecoder> msgFrameDecoder) {
        VodMsgNettyFactory.msgFrameDecoder = msgFrameDecoder;
    }

    /**
     *
     * @param buffer Netty's channel buffer
     * @param timeout Does this msg include a TimeoutId? (Yes = true, No =
     * false)
     * @return
     * @throws MessageDecodingException
     */
    protected VodMsg decode(ChannelBuffer buffer, boolean timeout) throws MessageDecodingException {
        if (VodMsgNettyFactory.msgFrameDecoder == null) {
            throw new NullPointerException("VodMsgNettyFactory.setMsgFrameDecoder() must be called before decoding any messages");
        }
        decodeHeader(buffer, timeout);

        VodMsg msg = process(buffer);
        finish(msg);
        return msg;
    }

    protected void finish(VodMsg msg) {
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

        int srcOverlayId = buffer.readInt();
        int srcNatPolicy = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
        Set<Address> parents = UserTypesDecoderFactory.readListAddresses(buffer);
        int destOverlayId = buffer.readInt();
        int destNatPolicy = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);

        vodSrc = new VodAddress(src, srcOverlayId, (short) srcNatPolicy, parents);
        vodDest = new VodAddress(dest, destOverlayId, (short) destNatPolicy, null);
    }

    protected abstract VodMsg process(ChannelBuffer buffer) throws MessageDecodingException;
};
