/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common;

import java.util.Set;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.timer.TimeoutId;

/**
 *  This class is for unit test reason 
 * @author jim
 */
public interface RetryComponentDelegator extends ComponentDelegator {

    public void doAutoSubscribe();

    public TimeoutId doMulticast(RewriteableRetryTimeout timeout, Set<Address> multicastAddrs);
    public TimeoutId doMulticast(RewriteableRetryTimeout timeout, Set<Address> multicastAddrs, 
            Object request);
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs, 
            long timeoutInMilliSecs, int rtoRetries);
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs, 
            long timeoutInMilliSecs, int rtoRetries,  Object request);
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs, 
            long timeoutInMilliSecs, int rtoRetries, double rtoScaleAfterRetry);
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs, 
            long timeoutInMilliSecs, int rtoRetries, double rtoScaleAfterRetry, Object request);
    
    /*
     * This retry method retries the message a number of times, and
     * if a response isn't received, it triggers a timeout at the sender of the
     * event. 
     * The RewriteableRetryTimeout takes both a timeout object and the actual message
     * as parameters. 
     * A timeoutId is used to discard duplicates in NatTraverser.
     * You should call cancelRetry() on the timeoutId returned by this method.
     */
    public TimeoutId doRetry(RewriteableRetryTimeout timeout);
    public TimeoutId doRetry(RewriteableRetryTimeout timeout, Object request);
    /*
     * doRetry() won't actually retry the message! We just set the timeoutId. 
     * timeoutId is then used to discard duplicates in NatTraverser.
     * You can't call cancelRetry() on the timeoutId returned by this method.
     */
    public TimeoutId doRetry(RewriteableMsg msg);
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries);
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries, 
            Object request);
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries, 
            double rtoScaleAfterRetry);
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries, 
            double rtoScaleAfterRetry, Object request);

    /**
     * 
     * @param timeoutId
     * @return true if the timeout is successfully cancelled, false otherwise
     */
    public boolean doCancelRetry(TimeoutId timeoutId);
    
    /**
     * The context object is supplied with a retry method, and 
     * retrieved using this method (typically after a response or timeout).
     * 
     * @param timeoutId
     * @return a context object associated with this timeoutId. 
     */
    public Object doGetContext(TimeoutId timeoutId);

    
    
}
