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
    private final int numRetries;
    private final double scaleRtoAfterRetry;

    /**
     * The delay is stored locally and used to initialize the
     * RetryTimeout's ScheduleTimeout object.
     * This ScheduleTimeout is scheduled directly after all retries have failed.
     * @param delay
     * @param numRetries
     * @param scaleRtoAfterRetry
     */
    public ScheduleRetryTimeout(long delay, int numRetries, double scaleRtoAfterRetry) {
        this.st = new ScheduleTimeout(0);
        this.delay = delay;
        this.numRetries = numRetries;
        this.scaleRtoAfterRetry = scaleRtoAfterRetry;
    }

    public ScheduleRetryTimeout(long delay, int numRetries) {
        this(delay, numRetries, 1.0d);
    }


    public ScheduleTimeout getScheduleTimeout() {
        return st;
    }

    public final long getDelay() {
        return delay;
    }

    public int getNumRetries() {
        return numRetries;
    }

    public double getScaleRtoAfterRetry() {
        return scaleRtoAfterRetry;
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
