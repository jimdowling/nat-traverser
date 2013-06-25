package se.sics.gvod.nat.common;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.timer.TimeoutId;

/**
 * 
 * @author salman
 * z server sends this message to the initiator to start SHP
 * direction of msg: server ----> client
 */
public class TMessage {

    public final static class RequestMsg extends VodMsg {

        public int getSize() {
            return 38;
        }
        static final long serialVersionUID = 1L;

        public RequestMsg(VodAddress src, VodAddress dest) {
            super(src, dest);

        }

        @Override
        public RewriteableMsg copy() {
            return new TMessage.RequestMsg(this.getVodSource(), this.getVodDestination());
        }
    }

    public final static class ResponseMsg extends VodMsg {

        static final long serialVersionUID = 1L;

        public int getSize() {
            return 38;
        }

        public ResponseMsg(VodAddress src, VodAddress dest, TimeoutId uuid) {
            super(src, dest);
            setTimeoutId(uuid);
        }

        @Override
        public RewriteableMsg copy() {
            return new TMessage.ResponseMsg(this.getVodSource(), this.getVodDestination(),
                    this.getTimeoutId());
        }
    }

    public static final class RequestRetryTimeout extends RewriteableRetryTimeout {

        private final RequestMsg requestMsg;

        public RequestRetryTimeout(ScheduleRetryTimeout st, RequestMsg requestMsg) {
            super(st, requestMsg);
            this.requestMsg = requestMsg;
        }

        public RequestMsg getRequestMsg() {
            return requestMsg;
        }
    }
}
