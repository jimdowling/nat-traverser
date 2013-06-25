package se.sics.gvod.hp.msgs;

import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.timer.TimeoutId;

/**
 * This class uses the ObjectDecoder/Encoder and is only used for testing.
 * @author salman
 */
public class TConnectionMessage
{

    public static final class Ping extends VodMsg
    {
        static final long serialVersionUID = 1L;
        private final String message;

	public int getSize()
	{
		return 10 + 20 + 38;
	}

        public Ping(VodAddress src, VodAddress dest,
                 String message)
        {
            super(src, dest);
            this.message = message;
        }

        private Ping(Ping msg, VodAddress src)
        {
            super(src, msg.getVodDestination());
            message = msg.getMessage();
        }

        private Ping(VodAddress dest, Ping msg)
        {
            super(msg.getVodSource(), dest);
            message = msg.getMessage();
        }

        public String getMessage()
        {
            return message;
        }

        @Override
        public RewriteableMsg copy() {
            TConnectionMessage.Ping copy = new TConnectionMessage.Ping(vodSrc, vodDest, message);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

    public final static class Pong extends VodMsg
    {
        static final long serialVersionUID = 1L;
        private final String message;


	public int getSize()
	{
		return 10 + 20 + 38;
	}

        public Pong(VodAddress src, VodAddress dest,
                 String message, TimeoutId timeoutID)
        {
            super(src, dest);
            this.message = message;
            setTimeoutId(timeoutID);
        }

        private Pong(Pong msg, VodAddress src)
        {
            super(src, msg.getVodDestination());
            message = msg.getMessage();
            setTimeoutId(msg.getTimeoutId());
        }

        private Pong(VodAddress dest, Pong msg)
        {
            super(msg.getVodSource(), dest);
            message = msg.getMessage();
            setTimeoutId(msg.getTimeoutId());
        }

        public String getMessage()
        {
            return message;
        }

        @Override
        public RewriteableMsg copy() {
           return new TConnectionMessage.Pong(vodSrc, vodDest, message, timeoutId);
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
