package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;

import java.util.Set;

import se.sics.gvod.address.Address;
import se.sics.gvod.net.MsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;
import se.sics.gvod.timer.NoTimeoutId;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.UUID;

public abstract class DirectMsgNettyFactory 
{

    protected VodAddress vodDest;
    protected VodAddress vodSrc;
    protected TimeoutId timeoutId;
    protected Address src, dest;
    protected static Class<? extends MsgFrameDecoder> msgFrameDecoder;

    public static void setMsgFrameDecoder(Class<? extends MsgFrameDecoder> msgFrameDecoder) {
        DirectMsgNettyFactory.msgFrameDecoder = msgFrameDecoder;
    }

    /**
     *
     * @param buffer Netty's channel buffer
     * @param timeout Does this msg include a TimeoutId? (Yes = true, No =
     * false)
     * @return
     * @throws MessageDecodingException
     */
    protected DirectMsg decode(ByteBuf buffer, boolean timeout) throws MessageDecodingException {
        if (DirectMsgNettyFactory.msgFrameDecoder == null) {
            throw new NullPointerException("VodMsgNettyFactory.setMsgFrameDecoder() must be called before decoding any messages");
        }
        decodeHeader(buffer, timeout);

        DirectMsg msg = process(buffer);
        finish(msg);
        return msg;
    }

    protected void finish(DirectMsg msg) {
        msg.setTimeoutId(timeoutId);
    }

    protected void decodeHeader(ByteBuf buffer, boolean timeout)
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

    protected abstract DirectMsg process(ByteBuf buffer) throws MessageDecodingException;
};
