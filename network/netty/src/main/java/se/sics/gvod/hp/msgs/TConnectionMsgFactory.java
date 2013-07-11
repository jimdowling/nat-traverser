package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;

public class TConnectionMsgFactory {

	public static class Ping extends DirectMsgNettyFactory {

		private Ping() {
		}

		public static TConnectionMsg.Ping fromBuffer(ByteBuf buffer)
				throws MessageDecodingException {
			return (TConnectionMsg.Ping) new TConnectionMsgFactory.Ping().decode(buffer, true);
		}

		@Override
		protected TConnectionMsg.Ping process(ByteBuf buffer) throws MessageDecodingException {

			return new TConnectionMsg.Ping(vodSrc, vodDest, timeoutId);
		}
	}

	public static class Pong extends DirectMsgNettyFactory {

		private Pong() {
		}

		public static TConnectionMsg.Pong fromBuffer(ByteBuf buffer)
				throws MessageDecodingException {
			return (TConnectionMsg.Pong) new TConnectionMsgFactory.Pong().decode(buffer, true);
		}

		@Override
		protected TConnectionMsg.Pong process(ByteBuf buffer) throws MessageDecodingException {

			return new TConnectionMsg.Pong(vodSrc, vodDest, timeoutId);
		}
	}
}
