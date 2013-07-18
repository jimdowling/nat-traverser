package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;
import se.sics.gvod.timer.TimeoutId;

public class HpMsgFactory {

    public static abstract class Base extends DirectMsgNettyFactory.Base {

        protected int remoteClientId;
        protected TimeoutId msgTimeoutId;

        protected Base() {
        }

        @Override
        protected void decodeHeader(ByteBuf buffer, boolean timeout) throws MessageDecodingException {
            super.decodeHeader(buffer, timeout);
            remoteClientId = buffer.readInt();
            msgTimeoutId = UserTypesDecoderFactory.readTimeoutId(buffer);
        }
    };

    public static abstract class Request extends Base {

        protected DirectMsg decode(ByteBuf buffer) throws MessageDecodingException {
            return super.decode(buffer, true);
        }

        @Override
        protected void finish(DirectMsg msg) {
            msg.setTimeoutId(timeoutId);
        }

        protected void decodeHeader(ByteBuf buffer)
                throws MessageDecodingException {
            super.decodeHeader(buffer, true);
        }

        @Override
        protected abstract HpMsg.Request process(ByteBuf buffer) throws MessageDecodingException;
    }

    public static abstract class Response extends Base {

        protected DirectMsg decode(ByteBuf buffer) throws MessageDecodingException {
            return super.decode(buffer, true);
        }

        protected void decodeHeader(ByteBuf buffer)
                throws MessageDecodingException {
            super.decodeHeader(buffer, true);
        }

        @Override
        protected void finish(DirectMsg msg) {
            msg.setTimeoutId(timeoutId);
        }

        @Override
        protected abstract HpMsg.Response process(ByteBuf buffer) throws MessageDecodingException;
    }

    public static abstract class Oneway extends Base {

        protected DirectMsg decode(ByteBuf buffer) throws MessageDecodingException {
            return super.decode(buffer, false);
        }

        @Override
        protected void finish(DirectMsg msg) {
        }

        protected void decodeHeader(ByteBuf buffer)
                throws MessageDecodingException {
            super.decodeHeader(buffer, false);
        }

        @Override
        protected abstract HpMsg.Oneway process(ByteBuf buffer) throws MessageDecodingException;
    }
}