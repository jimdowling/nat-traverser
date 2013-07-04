package se.sics.gvod.hp.msgs;

import se.sics.gvod.common.msgs.*;
import org.jboss.netty.buffer.ChannelBuffer;

public class TConnectionMsgFactory {


    public static class Ping extends DirectMsgNettyFactory {

        private Ping() {
        }

        public static TConnectionMsg.Ping fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (TConnectionMsg.Ping)
                    new TConnectionMsgFactory.Ping().decode(buffer, true);
        }

        @Override
        protected TConnectionMsg.Ping process(ChannelBuffer buffer) throws MessageDecodingException {

            return new TConnectionMsg.Ping(vodSrc, vodDest, timeoutId);
        }

    }
    
    public static class Pong extends DirectMsgNettyFactory {

        private Pong() {
        }

        public static TConnectionMsg.Pong fromBuffer(ChannelBuffer buffer)
                
                throws MessageDecodingException {
            return (TConnectionMsg.Pong)
                    new TConnectionMsgFactory.Pong().decode(buffer, true);
        }

        @Override
        protected TConnectionMsg.Pong process(ChannelBuffer buffer) throws MessageDecodingException {

            return new TConnectionMsg.Pong(vodSrc, vodDest, timeoutId);
        }

    }
}
