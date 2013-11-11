/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.msgs;

import se.sics.gvod.timer.OverlayTimeout;


/**
 *
 * @author jdowling
 */
public class RewriteableRetryTimeout extends OverlayTimeout{

    private final RewriteableMsg retryMessage;
    private final ScheduleRetryTimeout scheduleRetryTimeout;

    public RewriteableRetryTimeout(ScheduleRetryTimeout st, RewriteableMsg retryMessage,
            int overlayId) {
        super(st.getScheduleTimeout(), overlayId);
        st.getScheduleTimeout().setTimeoutEvent(this);
        this.retryMessage = retryMessage;
        this.scheduleRetryTimeout = st;
    }

    public ScheduleRetryTimeout getScheduleRewriteableRetryTimeout() {
        return scheduleRetryTimeout;
    }

    public RewriteableMsg getMsg() {
        return retryMessage;
    }
}
