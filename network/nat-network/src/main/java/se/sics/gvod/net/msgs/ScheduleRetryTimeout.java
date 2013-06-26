/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.msgs;

import se.sics.gvod.timer.ScheduleTimeout;


/**
 *
 * @author jdowling
 */
public class ScheduleRetryTimeout {

    private final ScheduleTimeout st;
    private final long delay;
    private final int rtoRetries;
    private final double rtoScaleAfterRetry;

    /**
     * The delay is stored locally and used to initialize the
     * RetryTimeout's ScheduleTimeout object.
     * This ScheduleTimeout is scheduled directly after all retries have failed.
     * @param delay
     * @param rtoRetries
     * @param rtoScaleAfterRetry
     */
    public ScheduleRetryTimeout(long delay, int rtoRetries, double rtoScaleAfterRetry) {
        this.st = new ScheduleTimeout(0);
        this.delay = delay;
        this.rtoRetries = rtoRetries;
        this.rtoScaleAfterRetry = rtoScaleAfterRetry;
    }

    public ScheduleRetryTimeout(long delay, int rtoRetries) {
        this(delay, rtoRetries, 1.0d);
    }


    public ScheduleTimeout getScheduleTimeout() {
        return st;
    }

    public final long getDelay() {
        return delay;
    }

    public int getRtoRetries() {
        return rtoRetries;
    }

    public double getRtoScaleAfterRetry() {
        return rtoScaleAfterRetry;
    }

    

    /**
     * Sets the timeout event.
     *
     * @param timeout
     *            the new timeout event
     */
    public final void setTimeoutEvent(RewriteableRetryTimeout timeout) {
        st.setTimeoutEvent(timeout);
    }

    /**
     * Gets the timeout event.
     *
     * @return the timeout event
     */
    public final RewriteableRetryTimeout getTimeoutEvent() {
        return (RewriteableRetryTimeout) st.getTimeoutEvent();
    }
}
