/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.gvod.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jim
 * 
 */
public abstract class VodRetryComponentTestCase extends VodComponentTestCase
        implements RetryComponentDelegator {

    protected Map<TimeoutId, RewriteableRetryTimeout> timeouts;
    protected Map<TimeoutId, Object> requests;

    protected VodRetryComponentTestCase() {
        super();
        timeouts = new HashMap<TimeoutId, RewriteableRetryTimeout>();
        requests = new HashMap<TimeoutId, Object>();
    }
    
    @Before
    @Override
    public void setUp() {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Override
    public void doAutoSubscribe() {
        // do nothing
    }

    @Override
    public TimeoutId doRetry(RewriteableRetryTimeout timeout) {
        return doRetry(timeout, null);
    }

    @Override
    public TimeoutId doRetry(RewriteableRetryTimeout timeout, Object request) {
        TimeoutId timeoutId = timeout.getTimeoutId();
        timeouts.put(timeoutId, timeout);
        RewriteableMsg rewriteableRetryMessage = timeout.getMsg();
        rewriteableRetryMessage.setTimeoutId(timeoutId);
        eventList.add(timeout.getMsg());
        eventSemaphore.release();
        if (request != null) {
            requests.put(timeoutId, request);
        }
        return timeoutId;
    }

    @Override
    public TimeoutId doMulticast(RewriteableRetryTimeout timeout, Set<Address> multicastAddrs) {
        return doMulticast(timeout, multicastAddrs, null);
    }

    @Override
    public TimeoutId doMulticast(RewriteableRetryTimeout timeout, Set<Address> multicastAddrs,
            Object request) {
        TimeoutId timeoutId = timeout.getTimeoutId();
        timeouts.put(timeoutId, timeout);
        RewriteableMsg rewriteableRetryMessage = timeout.getMsg();
        rewriteableRetryMessage.setTimeoutId(timeoutId);
        for (Address p : multicastAddrs) {
            rewriteableRetryMessage.rewriteDestination(p);
            eventList.add(rewriteableRetryMessage);
            eventSemaphore.release();
        }
        if (request != null) {
            requests.put(timeoutId, request);
        }
        return timeoutId;
    }

    @Override
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs, int rtoRetries) {
        return doMulticast(msg, multicastAddrs, timeoutInMilliSecs, rtoRetries, null);
    }

    @Override
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs, int rtoRetries,
            Object request) {
        return doMulticast(msg, multicastAddrs, timeoutInMilliSecs, rtoRetries, 1.0d, request);
    }

    @Override
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs, int rtoRetries, double rtoScaleAfterRetry) {
        return doMulticast(msg, multicastAddrs, timeoutInMilliSecs, rtoRetries, rtoScaleAfterRetry, null);

    }

    @Override
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs, int rtoRetries, double rtoScaleAfterRetry, Object request) {
        for (Address addr : multicastAddrs) {
            msg.rewriteDestination(addr);
            eventList.add(msg);
            eventSemaphore.release();
        }
        return null;
    }

    @Override
    public TimeoutId doRetry(RewriteableMsg msg) {
        return doRetry(msg, 0, 0, null);
    }
    @Override
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries) {
        return doRetry(msg, timeoutInMilliSecs, rtoRetries, null);
    }

    @Override
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries,
            Object request) {
        return doRetry(msg, timeoutInMilliSecs, rtoRetries, 1.0d, request);
    }

    @Override
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries,
            double rtoScaleAfterRetry) {
        return doRetry(msg, timeoutInMilliSecs, rtoRetries, rtoScaleAfterRetry, null);
    }

    @Override
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries,
            double rtoScaleAfterRetry, Object request) {
        eventList.add(msg);
        eventSemaphore.release();
        return null;
    }

    @Override
    public boolean doCancelRetry(TimeoutId timeoutId) {
        return (timeouts.remove(timeoutId) != null);
    }

    @Override
    public Object doGetContext(TimeoutId timeoutId) {
        return requests.get(timeoutId);
    }


    @Override
    public boolean isPacingReqd() {
        return false;
    }
  
    @Override
    public HPMechanism getHpMechanism(VodAddress dest) {
        return getAddress().getHpMechanism(dest);
    }    

}
