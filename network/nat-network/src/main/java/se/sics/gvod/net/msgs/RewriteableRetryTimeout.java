/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.msgs;

import se.sics.gvod.timer.Timeout;


/**
 *
 * @author jdowling
 */
public class RewriteableRetryTimeout extends Timeout {

    private final RewriteableMsg retryMessage;
    private final ScheduleRetryTimeout scheduleRetryTimeout;

    public RewriteableRetryTimeout(ScheduleRetryTimeout st, RewriteableMsg retryMessage) {
        super(st.getScheduleTimeout());
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
