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

/**
 * This Factory decodes DirectMsgNetty objects.
 * DirectMsgNetty objects are objects that are sent via direct connections
 * after hole-punching.
 * Request and Response msgs must set timeouts. Oneway msgs cannot set timeouts.
 * We also differentiate between applicatino-level msgs and system-level msgs, as
 * this prevents system msgs from being handled in NatTraverser and then explicitly
 * dropped by the application msg handler.
 * @author jdowling
 */
public abstract class DirectMsgNettyFactory {

    public static abstract class Base {

        protected VodAddress vodDest;
        protected VodAddress vodSrc;
        protected TimeoutId timeoutId;
        protected Address src, dest;
        protected static Class<? extends MsgFrameDecoder> msgFrameDecoder;

        public static void setMsgFrameDecoder(Class<? extends MsgFrameDecoder> msgFrameDecoder) {
            Base.msgFrameDecoder = msgFrameDecoder;
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
            if (Base.msgFrameDecoder == null) {
                throw new NullPointerException("VodMsgNettyFactory.setMsgFrameDecoder() must be called before decoding any messages");
            }
            decodeHeader(buffer, timeout);

            DirectMsg msg = process(buffer);
            finish(msg);
            return msg;
        }
        protected abstract void finish(DirectMsg msg);

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

    public static abstract class Request extends Base {
        protected DirectMsg decode(ByteBuf buffer) throws MessageDecodingException {
            return super.decode(buffer, true);
        }
        protected void finish(DirectMsg msg) {
            msg.setTimeoutId(timeoutId);
        }
        protected void decodeHeader(ByteBuf buffer)
                throws MessageDecodingException {
            super.decodeHeader(buffer, true);
        }
    }
    
    public static abstract class Response extends Base  {
        protected DirectMsg decode(ByteBuf buffer) throws MessageDecodingException {
            return super.decode(buffer, true);
        }
        protected void finish(DirectMsg msg) {
            msg.setTimeoutId(timeoutId);
        }
        protected void decodeHeader(ByteBuf buffer)
                throws MessageDecodingException {
            super.decodeHeader(buffer, true);
        }
    }
    
    public static abstract class Oneway extends Base  {
        protected DirectMsg decode(ByteBuf buffer) throws MessageDecodingException {
            return super.decode(buffer, false);
        }
        protected void finish(DirectMsg msg) {
        }
        protected void decodeHeader(ByteBuf buffer)
                throws MessageDecodingException {
            super.decodeHeader(buffer, false);
        }    
    }
    
    public static abstract class SystemRequest extends Base  {
        protected DirectMsg decode(ByteBuf buffer) throws MessageDecodingException {
            return super.decode(buffer, true);
        }
        protected void finish(DirectMsg msg) {
            msg.setTimeoutId(timeoutId);
        }
        protected void decodeHeader(ByteBuf buffer)
                throws MessageDecodingException {
            super.decodeHeader(buffer, true);
        }
        
    }
    
    public static abstract class SystemResponse extends Base  {
        protected DirectMsg decode(ByteBuf buffer) throws MessageDecodingException {
            return super.decode(buffer, true);
        }
        protected void finish(DirectMsg msg) {
            msg.setTimeoutId(timeoutId);
        }
        protected void decodeHeader(ByteBuf buffer)
                throws MessageDecodingException {
            super.decodeHeader(buffer, true);
        }
    }
    
    public static abstract class SystemOneway extends Base  {
        protected DirectMsg decode(ByteBuf buffer) throws MessageDecodingException {
            return super.decode(buffer, false);
        }
        protected void finish(DirectMsg msg) {
        }
        protected void decodeHeader(ByteBuf buffer)
                throws MessageDecodingException {
            super.decodeHeader(buffer, false);
        }    
    }
}