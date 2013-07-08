package se.sics.gvod.hp.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNetty;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.timer.TimeoutId;

/**
 * This class uses the ObjectDecoder/Encoder and is only used for testing.
 * @author salman
 */
public class TConnectionMsg
{

    public static final class Ping extends DirectMsgNetty
    {
        static final long serialVersionUID = 1L;

        @Override
	public int getSize()
	{
		return getHeaderSize();
	}

        public Ping(VodAddress src, VodAddress dest, 
                TimeoutId timeoutId)
        {
            super(src, dest, timeoutId);
        }

        private Ping(Ping msg, VodAddress src)
        {
            super(src, msg.getVodDestination());
        }

        private Ping(VodAddress dest, Ping msg)
        {
            super(msg.getVodSource(), dest);
        }

        @Override
        public RewriteableMsg copy() {
            TConnectionMsg.Ping copy = new TConnectionMsg.Ping(vodSrc, vodDest, 
                    timeoutId);
            copy.setTimeoutId(timeoutId);
            return copy;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            return createChannelBufferWithHeader();
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PING;
        }
    }

    public final static class Pong extends DirectMsgNetty
    {
        static final long serialVersionUID = 1L;


        @Override
	public int getSize()
	{
		return getHeaderSize();
	}

        public Pong(VodAddress src, VodAddress dest, TimeoutId timeoutId)
        {
            super(src, dest);
            setTimeoutId(timeoutId);
        }

        private Pong(Pong msg, VodAddress src)
        {
            super(src, msg.getVodDestination());
            setTimeoutId(msg.getTimeoutId());
        }

        private Pong(VodAddress dest, Pong msg)
        {
            super(msg.getVodSource(), dest);
            setTimeoutId(msg.getTimeoutId());
        }

        @Override
        public RewriteableMsg copy() {
           return new TConnectionMsg.Pong(vodSrc, vodDest, timeoutId);
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
            return createChannelBufferWithHeader();
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.PONG;
        }

    }

    public static final class RequestRetryTimeout extends RewriteableRetryTimeout
    {

        private final Ping requestMsg;

        public RequestRetryTimeout(ScheduleRetryTimeout st, Ping requestMsg)
        {
            super(st, requestMsg);
            this.requestMsg = requestMsg;
        }

        public Ping getRequestMsg()
        {
            return requestMsg;
        }

    }

}
