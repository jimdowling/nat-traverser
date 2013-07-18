package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNetty;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

public class HpMsg {

    public interface Hp {

        public int getClientId();

        public int getRemoteClientId();

        public TimeoutId getMsgTimeoutId();
    }

    public static abstract class Request extends DirectMsgNetty.SystemRequest implements Hp 
    {

        protected final int remoteClientId;
        protected final TimeoutId msgTimeoutId;

        public Request(VodAddress src, VodAddress dest, int remoteClientId, TimeoutId msgTimeoutId) {
            super(src, dest);
            this.remoteClientId = remoteClientId;
            this.msgTimeoutId = msgTimeoutId;
        }

        public Request(VodAddress src, VodAddress dest, TimeoutId timeoutId,
                int remoteClientId, TimeoutId msgTimeoutId) {
            super(src, dest, timeoutId);
            this.remoteClientId = remoteClientId;
            this.msgTimeoutId = msgTimeoutId;
        }

        @Override
        public int getClientId() {
            return vodSrc.getId();
        }

        @Override
        public int getRemoteClientId() {
            return remoteClientId;
        }

        @Override
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
        protected void writeHeader(ByteBuf buffer) throws MessageEncodingException {
            super.writeHeader(buffer);
            buffer.writeInt(remoteClientId);
            UserTypesEncoderFactory.writeTimeoutId(buffer, msgTimeoutId);
        }
    }

    public static abstract class Response extends DirectMsgNetty.SystemResponse implements Hp{

        protected final int remoteClientId;
        protected final TimeoutId msgTimeoutId;

        public Response(VodAddress src, VodAddress dest, int remoteClientId, TimeoutId msgTimeoutId) {
            super(src, dest);
            this.remoteClientId = remoteClientId;
            this.msgTimeoutId = msgTimeoutId;
        }

        public Response(VodAddress src, VodAddress dest, TimeoutId timeoutId,
                int remoteClientId, TimeoutId msgTimeoutId) {
            super(src, dest, timeoutId);
            this.remoteClientId = remoteClientId;
            this.msgTimeoutId = msgTimeoutId;
        }

        @Override
        public int getClientId() {
            return vodSrc.getId();
        }

        @Override
        public int getRemoteClientId() {
            return remoteClientId;
        }

        @Override
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
        protected void writeHeader(ByteBuf buffer) throws MessageEncodingException {
            super.writeHeader(buffer);
            buffer.writeInt(remoteClientId);
            UserTypesEncoderFactory.writeTimeoutId(buffer, msgTimeoutId);
        }
    }

    public static abstract class Oneway extends DirectMsgNetty.SystemOneway implements Hp{

        protected final int remoteClientId;
        protected final TimeoutId msgTimeoutId;

        public Oneway(VodAddress src, VodAddress dest, int remoteClientId, TimeoutId msgTimeoutId) {
            super(src, dest);
            this.remoteClientId = remoteClientId;
            this.msgTimeoutId = msgTimeoutId;
        }

        @Override
        public int getClientId() {
            return vodSrc.getId();
        }

        @Override
        public int getRemoteClientId() {
            return remoteClientId;
        }

        @Override
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
        protected void writeHeader(ByteBuf buffer) throws MessageEncodingException {
            super.writeHeader(buffer);
            buffer.writeInt(remoteClientId);
            UserTypesEncoderFactory.writeTimeoutId(buffer, msgTimeoutId);
        }
    }
}