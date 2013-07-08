package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 * 
 * @author jim
 */
public class SHP_OpenHoleMsg {

    public enum ResponseType {

        OK, FAILED
    };

    public final static class Initiator extends HpMsg {

        static final long serialVersionUID = 1L;
        private final VodAddress dummyAddr;
        private final ResponseType responseType;

        public Initiator(VodAddress src, VodAddress dest, VodAddress dummyAddr,
                ResponseType responseType, TimeoutId msgTimeoutId) {
            super(src, dest, dest.getId(), msgTimeoutId);
            if (dummyAddr == null) {
                throw new NullPointerException("dummyAddr was null");
            }
            this.dummyAddr = dummyAddr;
            this.responseType = responseType;
        }

        public ResponseType getResponseType() {
            return responseType;
        }

        public VodAddress getDummyAddr() {
            return dummyAddr;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 1
                    + UserTypesEncoderFactory.VOD_ADDRESS_LEN_NO_PARENTS;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.SHP_OPENHOLE_INITIATOR;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeVodAddress(buffer, dummyAddr);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, responseType.ordinal());
            return buffer;
        }

        @Override
        public RewriteableMsg copy() {
            return new SHP_OpenHoleMsg.Initiator(vodSrc, vodDest,
                    dummyAddr, responseType, msgTimeoutId);
        }
    }
}
