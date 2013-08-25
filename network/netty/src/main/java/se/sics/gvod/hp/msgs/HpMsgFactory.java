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
        protected DirectMsg decode(ByteBuf buffer, boolean timeout) throws MessageDecodingException {
            throw new UnsupportedOperationException("Call decode(), not decode(.., boolean timeout)");
        }        

        
        @Override
        protected void finish(DirectMsg msg) {
            msg.setTimeoutId(timeoutId);
        }

        @Override
        protected abstract HpMsg.Request process(ByteBuf buffer) throws MessageDecodingException;
    }

    public static abstract class Response extends Base {

        protected DirectMsg decode(ByteBuf buffer) throws MessageDecodingException {
            return super.decode(buffer, true);
        }
        @Override
        protected DirectMsg decode(ByteBuf buffer, boolean timeout) throws MessageDecodingException {
            throw new UnsupportedOperationException("Call decode(), not decode(.., boolean timeout)");
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
        protected DirectMsg decode(ByteBuf buffer, boolean timeout) throws MessageDecodingException {
            throw new UnsupportedOperationException("Call decode(), not decode(.., boolean timeout)");
        }        

        @Override
        protected void finish(DirectMsg msg) {
        }

        @Override
        protected abstract HpMsg.Oneway process(ByteBuf buffer) throws MessageDecodingException;
    }
}