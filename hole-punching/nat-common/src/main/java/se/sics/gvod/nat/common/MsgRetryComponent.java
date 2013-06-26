/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.common;

import se.sics.gvod.common.RetryComponentDelegator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.AutoSubscribeComponent;
import se.sics.kompics.Channel;
import se.sics.kompics.ChannelFilter;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Event;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Port;
import se.sics.kompics.PortType;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;

/**
 *
 * @author jdowling
 */
public abstract class MsgRetryComponent extends AutoSubscribeComponent
        implements RetryComponentDelegator {

    private Logger logger = LoggerFactory.getLogger(MsgRetryComponent.class);
    protected Positive<VodNetwork> network = positive(VodNetwork.class);
    protected Positive<Timer> timer = positive(Timer.class);
    protected RetryComponentDelegator delegator;
    /**
     * Map of Message Types that each contain a map of timeoutId/retry objects.
     * Map is static, so that timeouts can be created in one component, but
     * cancelled in another component.
     */
    protected static ConcurrentHashMap<TimeoutId, Retry> mapMessageRetry =
            new ConcurrentHashMap<TimeoutId, Retry>();

    /**
     * This is the timeout event that is recvd by MsgRetryComp if the timer isn't
     * cancelled before the timeout expires.
     */
    public static class RequestTimeoutEvent extends Timeout {

        private final RewriteableMsg msg;

        public RequestTimeoutEvent(ScheduleTimeout timeout, RewriteableMsg msg) {
            super(timeout);
            this.msg = msg;
        }

        public RewriteableMsg getMsg() {
            return msg;
        }

    }

    /**
     * The Retry object is stored with each timeout event in a map, so that if a
     * timeout is triggered, the retry object contains state required to retry
     * the message.
     */
    protected static class Retry {

        private final Object context;
        private final RewriteableMsg message;
        private final ScheduleRetryTimeout retryScheduleTimeout;
        private long retransmissionTimeout;
        private int retriesLeft;
        private final double rtoScaleAfterRetry;
        private int rtoRetries;
        private int numReplies;
        private Set<Address> multicastAddrs;

        public Retry(RewriteableMsg message, long retransmissionTimeout, int rtoRetries,
                double rtoScaleAfterRetry, Object context, Set<Address> multicastAddrs) {
            this(message, retransmissionTimeout, rtoRetries, rtoScaleAfterRetry,
                    null, context, multicastAddrs);
        }

        public Retry(RewriteableMsg message, long retransmissionTimeout, int rtoRetries,
                double rtoScaleAfterRetry, ScheduleRetryTimeout retryTimeout,
                Object context, Set<Address> multicastAddrs) {
            this(message, retransmissionTimeout, rtoRetries, rtoScaleAfterRetry,
                    retryTimeout, 1, context, multicastAddrs);
        }

        public Retry(RewriteableMsg message, long retransmissionTimeout, int rtoRetries,
                double rtoScaleAfterRetry, ScheduleRetryTimeout retryTimeout, int numReplies,
                Object context, Set<Address> multicastAddrs) {
            super();
            if (retransmissionTimeout < 0) {
                throw new IllegalArgumentException("Retransmission timeout must be zero or greater");
            }
            if (rtoRetries < 0) {
                throw new IllegalArgumentException("Number of Retries must be zero or greater");
            }
            if (numReplies < 0) {
                throw new IllegalArgumentException("Number of Replies must be zero or greater");
            }
            if (rtoScaleAfterRetry == 0 || rtoScaleAfterRetry < 0) {
                throw new IllegalArgumentException("RtoScaleAfterRetry must be greater than zero.");
            }
            this.message = message;
            this.retransmissionTimeout = retransmissionTimeout;
            this.retriesLeft = rtoRetries;
            this.rtoRetries = rtoRetries;
            this.rtoScaleAfterRetry = rtoScaleAfterRetry;
            this.retryScheduleTimeout = retryTimeout;
            this.numReplies = numReplies;
            this.context = context;
            this.multicastAddrs = multicastAddrs;
        }

        public Object getContext() {
            return context;
        }

        public Set<Address> getMulticastAddrs() {
            return multicastAddrs;
        }

        public RewriteableMsg getMessage() {
            return message;
        }

        public long getRetransmissionTimeout() {
            return retransmissionTimeout;
        }

        public int getRetriesLeft() {
            return retriesLeft;
        }

        public int getRtoRetries() {
            return rtoRetries;
        }

        public double getRtoScaleAfterRetry() {
            return rtoScaleAfterRetry;
        }

        public void rtoScale() {
            this.retransmissionTimeout = (long) (rtoScaleAfterRetry * retransmissionTimeout);
        }

        public void decRetriesLeft() {
            retriesLeft--;
        }

        public TimeoutId getTimeoutId() {
            return getMessage().getTimeoutId();
        }

        public ScheduleTimeout getScheduleTimeout() {
            if (retryScheduleTimeout != null) {
                return retryScheduleTimeout.getScheduleTimeout();
            }
            return null;
        }

        public int decNumReplies() {
            numReplies--;
            if (numReplies < 0) {
                throw new IllegalStateException("Num of replies expected is less than 0.");
            }
            return numReplies;
        }

        public int getNumReplies() {
            return numReplies;
        }
    }

    /**
     * The base class should call autosubscribe().
     * If it doesn't, then we have to also subscribe the timeoutHadler to the timer port.
     * @param delegator 
     */
    public MsgRetryComponent(RetryComponentDelegator delegator) {
        this.delegator = (delegator == null) ? this : delegator;
    }

    protected TimeoutId multicast(RewriteableRetryTimeout timeout, Set<Address> multicastAddrs) {
        return retry(timeout, multicastAddrs, null);
    }

    protected TimeoutId multicast(RewriteableRetryTimeout timeout, Set<Address> multicastAddrs,
            Object request) {
        return retry(timeout, multicastAddrs, request);
    }

    protected TimeoutId multicast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs,
            int rtoRetries) {
        return multicast(msg, multicastAddrs, timeoutInMilliSecs, rtoRetries, 1.0d, null);
    }

    protected TimeoutId multicast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs,
            int rtoRetries, Object request) {
        return multicast(msg, multicastAddrs, timeoutInMilliSecs, rtoRetries, 1.0d, request);
    }

    protected TimeoutId multicast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs,
            int rtoRetries, double rtoScaleAfterRetry) {
        return multicast(msg, multicastAddrs, timeoutInMilliSecs, rtoRetries, rtoScaleAfterRetry, null);
    }

    protected TimeoutId multicast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs,
            int rtoRetries, double rtoScaleAfterRetry, Object request) {
        return retry(msg, timeoutInMilliSecs, rtoRetries, rtoScaleAfterRetry,
                request, multicastAddrs, null);
    }

    protected TimeoutId retry(RewriteableRetryTimeout timeout) {
        return retry(timeout, null);
    }

    /**
     * Invoke this method to send a message.
     *
     * @param timeout object including msg to be sent
     * @param request request object (or any context) that will be availble to
     * be retrieved using getContext() when either the response or timeout is
     * received.
     * @return timeoutId
     */
    protected TimeoutId retry(RewriteableRetryTimeout timeout, Object request) {
        return retry(timeout, null, request);
    }

    private TimeoutId retry(RewriteableRetryTimeout timeout, Set<Address> multicastAddrs,
            Object request) {
        ScheduleRetryTimeout st = timeout.getScheduleRewriteableRetryTimeout();
        int rtoRetries = st.getRtoRetries();
        double rtoScaleAfterRetry = st.getRtoScaleAfterRetry();
        TimeoutId timeoutId = timeout.getTimeoutId();
        timeout.getMsg().setTimeoutId(timeoutId);
        RewriteableMsg msg = timeout.getMsg();
        return scheduleMessageRetry(new Retry(msg, st.getDelay(), rtoRetries, rtoScaleAfterRetry, st,
                request, multicastAddrs), st.getDelay(), null);
    }

    protected TimeoutId retry(RewriteableMsg msg, long timeoutInMilliSecs,
            int rtoRetries) {
        return retry(msg, timeoutInMilliSecs, rtoRetries, 1.0d, null);
    }

    protected TimeoutId retry(RewriteableMsg msg, long timeoutInMilliSecs,
            int rtoRetries, Object request) {
        return retry(msg, timeoutInMilliSecs, rtoRetries, 1.0d, request);
    }

    protected TimeoutId retry(RewriteableMsg msg, long timeoutInMilliSecs,
            int rtoRetries, double rtoScaleAfterRetry) {
        return retry(msg, timeoutInMilliSecs, rtoRetries, rtoScaleAfterRetry, null);
    }

    protected TimeoutId retry(RewriteableMsg msg, long timeoutInMilliSecs,
            int rtoRetries, double rtoScaleAfterRetry, Object request) {
        return retry(msg, timeoutInMilliSecs, rtoRetries, rtoScaleAfterRetry,
                request, null, null);
    }

    private TimeoutId retry(RewriteableMsg msg, long timeoutInMilliSecs,
            int rtoRetries, double rtoScaleAfterRetry, Object request,
            Set<Address> multicastAddrs, TimeoutId oldTimeoutId) {

        return scheduleMessageRetry(new Retry(msg, timeoutInMilliSecs,
                rtoRetries, rtoScaleAfterRetry, request, multicastAddrs),
                timeoutInMilliSecs, oldTimeoutId);
    }
    
    /**
     * 
     * @param retry
     * @param timeoutInMilliSecs
     * @param oldTimeoutId leave this as null, normally. To replace the timeoutId 
     * that would be generated with an oldTimeoutId, set this variable.
     * @return 
     */
    private TimeoutId scheduleMessageRetry(Retry retry, 
            long timeoutInMilliSecs, TimeoutId oldTimeoutId) {
        RewriteableMsg msg = retry.getMessage();

        ScheduleTimeout st = new ScheduleTimeout(timeoutInMilliSecs);
        RequestTimeoutEvent requestTimeoutEvent = new RequestTimeoutEvent(st, msg);
        st.setTimeoutEvent(requestTimeoutEvent);
        if (oldTimeoutId != null) {
            requestTimeoutEvent.setTimeoutId(oldTimeoutId);
        }
        TimeoutId timeoutId = requestTimeoutEvent.getTimeoutId();
        msg.setTimeoutId(timeoutId);


        // retransmissionTimeout is '0' if we just execute retry(msg) with no
        // parameters. In this case, we won't retry the message. We just set the
        // timeoutId. timeoutId is then used to discard duplicates in NatTraverser.
        if (st.getDelay() != 0) {
            logger.trace("Storing timer {} for {} .", timeoutId, msg.getClass().getName());
            mapMessageRetry.put(timeoutId, retry);
            trigger(st, timer);
        }

        if (retry.getMulticastAddrs() != null) {
            Set<Address> multicastAddrs = retry.getMulticastAddrs();
            for (Address addr : multicastAddrs) {
                msg.rewriteDestination(addr);
                trigger(msg, network);
            }
        } else {
            trigger(msg, network);
        }
        return timeoutId;
    }

    protected Object getContext(TimeoutId timeoutId) {
        Object request = null;
        logger.trace("Cancelling timer " + timeoutId);
        if (this.mapMessageRetry.containsKey(timeoutId)) {
            Retry r = this.mapMessageRetry.get(timeoutId);
            request = r.getContext();
        }
        return request;
    }

    /**
     * Called by handler in base class
     *
     * @param msgType class of the corresponding Request message sent.
     * @param timeoutId TimeoutId included in the Request message.
     * @return true if it finds and successfully cancels the timeout for the
     * msg, false otherwise.
     */
//    protected boolean cancelRetry(Class<? extends RewriteableMsg> msgType, TimeoutId timeoutId) {
    protected boolean cancelRetry(TimeoutId timeoutId) {
        if (timeoutId == null) {
            throw new IllegalArgumentException("timeoutId was null when cancelling retry");
        }
        if (timeoutId.isSupported()) {
            logger.trace("Cancelling timer: " + timeoutId);

            if (mapMessageRetry.remove(timeoutId) != null) {
                CancelTimeout ct = new CancelTimeout(timeoutId);
                trigger(ct, timer);
                return true;
            } else {
                logger.trace("Cancelling timer failed: " + timeoutId.getId() + " . Couldn't find timeoutId.");
            }
        } else {
            logger.debug("TimeoutId not used by this instance");
        }
        return false;
    }

    /**
     * Cannot be called from subclass.
     *
     * @param requestClass
     * @param timeoutId
     * @return
     */
    private Retry getRetryObj(TimeoutId timeoutId) {
        return mapMessageRetry.get(timeoutId);
    }
    protected Handler<RequestTimeoutEvent> handleRTO = new Handler<RequestTimeoutEvent>() {
        @Override
        public void handle(RequestTimeoutEvent timeout) {
            TimeoutId timeoutId = timeout.getTimeoutId();
            Retry retryData = getRetryObj(timeoutId);

            if (retryData == null) {
                logger.warn(getClass() + " couldn't find Retry object for {} and {}",
                        timeout.getTimeoutId(),
                        timeout.getMsg());
                return;
            }

            cancelRetry(timeoutId);
            
            if (retryData.getRetriesLeft() > 0) {
                retryData.decRetriesLeft();
                retryData.rtoScale();
                RewriteableMsg msg = retryData.getMessage();
                if (msg instanceof VodMsg) {
                    VodMsg m = (VodMsg) msg;

                    logger.info("Message Retry Comp (" + m.getSource().getId() + ")"
                            + " : Retrying Src: " + m.getVodSource().getId()
                            + " dest: " + m.getVodDestination().getId()
                            + msg.getClass().toString() + " retries=" + retryData.getRetriesLeft());
                } else {
                    logger.info("Message Retry Comp (" + msg.getSource().getId() + ")"
                            + " : Retrying Src: " + msg.getSource().getId()
                            + " dest: " + msg.getDestination().getId()
                            + msg.getClass().toString() + " retries=" + retryData.getRetriesLeft());
                }
                scheduleMessageRetry(retryData, retryData.getRetransmissionTimeout(),
                        timeoutId);
            } else if (retryData.getRetriesLeft() == 0) {
                // if there's a client-supplied timeout, send it back to the client
                if (retryData.getScheduleTimeout() != null) {
//                    TimeoutId callbackTimeoutId = retryData.getScheduleTimeout().getTimeoutEvent().getTimeoutId();
                    TimeoutId callbackTimeoutId = retryData.getTimeoutId();
                    mapMessageRetry.put(callbackTimeoutId, retryData);
                    ScheduleTimeout st = retryData.getScheduleTimeout();
                    st.getTimeoutEvent().setTimeoutId(callbackTimeoutId);
                    trigger(st, timer);
                } else {
                    logger.warn("MsgRetry: timeout obj was null with no retries left: {} ",
                            retryData.getMessage().getClass().getName());
                }
            } else {
                // shouldn't get here
                throw new IllegalStateException("Message retry component retry count < 0, shouldn't have happened.");
            }
        }
    };
    public Handler<Stop> handleStop = new Handler<Stop>() {
        @Override
        public void handle(Stop event) {
            // cancel all the schedule retry timers

            Set<TimeoutId> timerIds = mapMessageRetry.keySet();
            for (TimeoutId id : timerIds) {
                trigger(new CancelTimeout(id), timer);
            }
//            }
            // Call stop handler in subclass
            stop(event);
        }
    };

    abstract public void stop(Stop event);

    @Override
    public void doAutoSubscribe() {
        autoSubscribe();
    }

    @Override
    public <P extends PortType> void doTrigger(Event event, Port<P> port) {
        if (event == null || port == null) {
            throw new NullPointerException("Null event or null port when calling trigger.");
        }
        trigger(event, port);
//        logger.trace(event.getClass().getCanonicalName());
    }

    @Override
    public TimeoutId doMulticast(RewriteableRetryTimeout timeout, Set<Address> multicastAddrs) {
        return multicast(timeout, multicastAddrs, null);
    }

    @Override
    public TimeoutId doMulticast(RewriteableRetryTimeout timeout, Set<Address> multicastAddrs,
            Object request) {
        return multicast(timeout, multicastAddrs, request);
    }

    @Override
    public TimeoutId doRetry(RewriteableRetryTimeout timeout) {
        return retry(timeout);
    }

    @Override
    public TimeoutId doRetry(RewriteableRetryTimeout timeout, Object request) {
        return retry(timeout, request);
    }

    @Override
    public boolean doCancelRetry(
            TimeoutId timeoutId) {
        return cancelRetry(timeoutId);
    }

    @Override
    public <E extends Event, P extends PortType> void doSubscribe(Handler<E> handler, Port<P> port) {
        subscribe(handler, port);
    }

    @Override
    public <P extends PortType> Negative<P> getNegative(Class<P> portType) {
        return negative(portType);
    }

    @Override
    public <P extends PortType> Positive<P> getPositive(Class<P> portType) {
        return positive(portType);
    }

    @Override
    public TimeoutId doRetry(RewriteableMsg msg) {
        return retry(msg, 0, 0);
    }

    @Override
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries) {
        return retry(msg, timeoutInMilliSecs, rtoRetries);
    }

    @Override
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries,
            Object request) {
        return retry(msg, timeoutInMilliSecs, rtoRetries, request);
    }

    @Override
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries,
            double rtoScaleAfterRetry) {
        return retry(msg, timeoutInMilliSecs, rtoRetries, rtoScaleAfterRetry);
    }

    @Override
    public TimeoutId doRetry(RewriteableMsg msg, long timeoutInMilliSecs, int rtoRetries,
            double rtoScaleAfterRetry, Object request) {
        return retry(msg, timeoutInMilliSecs, rtoRetries, rtoScaleAfterRetry, request);
    }

    @Override
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs, int rtoRetries) {
        return multicast(msg, multicastAddrs, timeoutInMilliSecs, rtoRetries);
    }

    @Override
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs, int rtoRetries, Object request) {
        return multicast(msg, multicastAddrs, timeoutInMilliSecs, rtoRetries, request);
    }

    @Override
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs, int rtoRetries, double rtoScaleAfterRetry) {
        return multicast(msg, multicastAddrs, timeoutInMilliSecs, rtoRetries, rtoScaleAfterRetry);
    }

    @Override
    public TimeoutId doMulticast(RewriteableMsg msg, Set<Address> multicastAddrs,
            long timeoutInMilliSecs, int rtoRetries, double rtoScaleAfterRetry, Object request) {
        return retry(msg, timeoutInMilliSecs, rtoRetries, rtoScaleAfterRetry, request, 
                multicastAddrs, null);
    }

    @Override
    public Object doGetContext(
            TimeoutId timeoutId) {
        return getContext(timeoutId);
    }

    @Override
    public <P extends PortType> Channel<P> doConnect(
            Positive<P> positive, Negative<P> negative) {
        return connect(positive, negative);
    }

    @Override
    public <P extends PortType> Channel<P> doConnect(
            Negative<P> negative, Positive<P> positive) {
        return connect(negative, positive);
    }

    @Override
    public <P extends PortType> Channel<P> doConnect(Positive<P> positive,
            Negative<P> negative, ChannelFilter<?, ?> filter) {
        return connect(positive, negative, filter);
    }

    @Override
    public <P extends PortType> Channel<P> doConnect(Negative<P> negative,
            Positive<P> positive, ChannelFilter<?, ?> filter) {
        return connect(negative, positive, filter);
    }

    @Override
    public <P extends PortType> void doDisconnect(Negative<P> negative,
            Positive<P> positive) {
        // TODO - tell cosmin that disconnect should return a portType
        disconnect(negative, positive);
    }

    @Override
    public <P extends PortType> void doDisconnect(Positive<P> positive,
            Negative<P> negative) {
        // TODO - tell cosmin that disconnect should return a portType
        disconnect(positive, negative);
    }

    @Override
    public Component doCreate(Class<? extends ComponentDefinition> definition) {
        // TODO: implement doCreate in kompics core.
//        return create(definition);
        throw new NotImplementedException("delegator.doCreate() not supported yet in kompics");
    }

    @Override
    public boolean isUnitTest() {
        return false;
    }
}
