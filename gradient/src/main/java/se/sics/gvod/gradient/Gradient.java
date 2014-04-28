package se.sics.gvod.gradient;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import se.sics.gvod.gradient.events.GradientSetsExchangeCycle;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.RTTStore;
import se.sics.gvod.common.RTTStore.RTT;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.Utility;
import se.sics.gvod.common.msgs.RelayMsgNetty;
import se.sics.gvod.croupier.PeerSamplePort;
import se.sics.gvod.croupier.events.CroupierSample;
import se.sics.gvod.gradient.events.FingersRequest;
import se.sics.gvod.gradient.events.FingersResponse;
import se.sics.gvod.gradient.events.GradientSample;
import se.sics.gvod.gradient.events.UtilityChanged;
import se.sics.gvod.gradient.msgs.GradientSetsExchangeMsg;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.gradient.msgs.GradientSetsExchangeMsg.Response;
import se.sics.gvod.gradient.msgs.GradientSearchMsg;
import se.sics.gvod.gradient.msgs.GradientSetsExchangeMsg.RequestRetryTimeout;
import se.sics.gvod.gradient.msgs.LeaderProposeMsg;
import se.sics.gvod.gradient.snapshot.GradientStats;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Stop;
import se.sics.gvod.timer.CancelPeriodicTimeout;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Positive;

/**
 *
 * @author jim
 */
public class Gradient extends MsgRetryComponent {

    private final Logger logger = LoggerFactory.getLogger(Gradient.class);
    private Negative<GradientPort> gradient = negative(GradientPort.class);
    private Positive<PeerSamplePort> croupier = positive(PeerSamplePort.class);
    SimilarSet similarSet;
    private Map<TimeoutId, Integer> outstandingSearchRequests = new HashMap<TimeoutId, Integer>();
    private TreeMap<UtilityRange, Set<VodDescriptor>> fingers = new TreeMap<UtilityRange, Set<VodDescriptor>>();
    private TimeoutId setsGossipingTimeoutId;
    private Self self;
    private long setsExchangeDelay;
    private long setsExchangePeriod;
    private long probeRequestTimeout;
    private int searchTtl;
    //Number of parallel probe messages to send.
    private int numOfProbes;
    //To measure if a utility probe is required
    private int utilityThreshold;
    //Number of best similar peers that should be sent in each sets exchange response.
    private int numberOfBestSimilarPeers;
    /**
     * Store UUIDs for the different periodic activities - used to stop/restart
     * these timer events.
     */
    private TimeoutId leaderSelectionTimeoutId;
    private double prevUtilityDiff = 0;
    private boolean converged = false;
    private boolean iAmTheLeader;
    private Set<Integer> leaderProposeAwaitingResponse = new HashSet<Integer>();
    private String compName;

    private class LeaderSelectionCheck extends Timeout {

        public LeaderSelectionCheck(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    };

    public Gradient() {
        this(null);
    }

    public Gradient(RetryComponentDelegator delegator) {
        super(delegator);
        this.delegator.doAutoSubscribe();
    }
    Handler<GradientInit> handleInit = new Handler<GradientInit>() {

        @Override
        public void handle(GradientInit init) {
            self = init.getSelf();
            compName = "(" + self.getId() + ") ";
            setsExchangeDelay = init.getConfig().getSetsExchangeDelay();
            setsExchangePeriod = init.getConfig().getSetsExchangePeriod();
            searchTtl = init.getConfig().getSearchTtl();
            numberOfBestSimilarPeers = init.getConfig().getNumBestSimilarPeers();
            probeRequestTimeout = init.getConfig().getSearchRequestTimeout();
            SchedulePeriodicTimeout periodicTimeout =
                    new SchedulePeriodicTimeout(0, setsExchangePeriod);
            periodicTimeout.setTimeoutEvent(new GradientSetsExchangeCycle(periodicTimeout));
            setsGossipingTimeoutId = periodicTimeout.getTimeoutEvent().getTimeoutId();
            delegator.doTrigger(periodicTimeout, timer);
            similarSet = new SimilarSet(self, init.getConfig().getViewSize(), 
                    init.getConfig().getTemperature());

            // Initialize the gradient fingers
            int numChunks = init.getConfig().getFingers();
            int binSize = init.getConfig().getUtilityThreshold();
            for (int i = 0; i < numChunks; i += binSize) {
                UtilityRange ur = new UtilityRange(i, i + binSize);
                fingers.put(ur, new HashSet<VodDescriptor>());
            }

            SchedulePeriodicTimeout st = new SchedulePeriodicTimeout(2000, 2000);
            st.setTimeoutEvent(new LeaderSelectionCheck(st));

            GradientStats.getStat(self.getAddress());
            // TODO - enable leader selection
//            trigger(st, timer);
        }
    };

    
    Handler<LeaderProposeMsg.Request> handleLeaderProposeRequestMsg = new Handler<LeaderProposeMsg.Request>() {

        @Override
        public void handle(LeaderProposeMsg.Request event) {
            long maxUtility = 0;
            VodDescriptor maxNeighbour = similarSet.getHighestUtilityPeer();
            if (maxNeighbour != null) {
                maxUtility = maxNeighbour.getUtility().getValue();
            }

            boolean candidateLeader = false;

            if (event.getUtility() > maxUtility) {
                candidateLeader = true;
            } else if (event.getUtility() == maxUtility) {
                // if equal utility values, then leader is node with lowest id
                if (event.getClientId() < maxNeighbour.getVodAddress().getId()) {
                    candidateLeader = true;
                }
            }

            LeaderProposeMsg.Response response = new LeaderProposeMsg.Response(
                    self.getAddress(), event.getVodSource(), event.getClientId(),
                    event.getRemoteId(), event.getNextDest(),
                    event.getTimeoutId(), candidateLeader, RelayMsgNetty.Status.OK);
            trigger(response, network);

        }
    };
    Handler<LeaderProposeMsg.Response> handleLeaderProposeResponseMsg = new Handler<LeaderProposeMsg.Response>() {

        @Override
        public void handle(LeaderProposeMsg.Response event) {
            leaderProposeAwaitingResponse.remove(event.getRemoteId());
            iAmTheLeader = (iAmTheLeader && event.isLeader());
            if (iAmTheLeader && leaderProposeAwaitingResponse.isEmpty()) {
                logger.info(compName + "I'm the leader!");
                iAmTheLeader = true;
            }

        }
    };
    Handler<LeaderSelectionCheck> handleLeaderSelectionCheckTimeout =
            new Handler<LeaderSelectionCheck>() {

                @Override
                public void handle(LeaderSelectionCheck event) {

                    int totalUtility = 0;
                    int myUtility = self.getUtility().getValue();
                    int maxUtility = Integer.MIN_VALUE;
                    int threshold = 1000; // TODO: Move to config

                    if (!converged) {
                        for (VodDescriptor node : similarSet.getAllSimilarPeers()) {
                            totalUtility += node.getUtility().getValue();
                            if (node.getUtility().getValue() > maxUtility) {
                                maxUtility = node.getUtility().getValue();
                            }
                        }
                    }

                    int utilityDiff = Math.abs(myUtility - totalUtility);
                    prevUtilityDiff = utilityDiff;

                    if ((converged == true) || (myUtility >= maxUtility
                            && (utilityDiff - prevUtilityDiff) < threshold)) {
                        converged = true;
                        VodDescriptor maxNeighbour = similarSet.getHighestUtilityPeer();
                        maxUtility = (maxNeighbour == null) ? 0
                                : maxNeighbour.getUtility().getValue();
                        if (myUtility >= maxUtility) {
                            leaderProposeAwaitingResponse.clear();
                            iAmTheLeader = true;

                            for (VodDescriptor node : similarSet.getAllSimilarPeers()) {
                                sendLeaderPropose(node.getVodAddress());
                            }
//                            for (VodDescriptor node : RandomView.getAll(self)) {
//                                sendLeaderPropose(node.getVodAddress());
//                            }
                        }
                    }
                }
            };
//    Handler<UtilityConvergeCheck> handleUtilityConvergeCheckTimeout =
//            new Handler<UtilityConvergeCheck>() {
//
//                @Override
//                public void handle(UtilityConvergeCheck event) {
//
//                    int totalUtility = 0;
//                    int myUtility = self.getUtility().getValue();
////                    double maxUtility = Double.MIN_VALUE;
//                    int maxUtility = Integer.MAX_VALUE;
//                    int threshold = 1000; // TODO: Move to config
//
//                    for (VodDescriptor node : similarSet.getAllSimilarPeers(self)) {
//                        totalUtility += node.getUtility().getValue();
////                totalUtility+=node.getUtility();
////                if(node.getUtility() > maxUtility) maxUtility = node.getUtility();
//                        if (node.getUtility().getValue() > maxUtility) {
//                            maxUtility = node.getUtility().getValue();
//                        }
//                    }
//                    for (VodDescriptor node : RandomView.getAll(self)) {
//                        totalUtility += node.getUtility().getValue();
//                        if (node.getUtility().getValue() > maxUtility) {
//                            maxUtility = node.getUtility().getValue();
//                        }
//                    }
//
//                    int utilityDiff = Math.abs(myUtility - totalUtility);
//
//                    if (myUtility >= maxUtility && (utilityDiff - prevUtilityDiff) < threshold) {
//                        startLeaderSelectionTimer();
//                        return;
//                    }
//
//                    prevUtilityDiff = utilityDiff;
//
//                    ScheduleTimeout st = new ScheduleTimeout(2000);
//                    st.setTimeoutEvent(new UtilityConvergeCheck(st));
//                    utilityConvergeTimeoutId = st.getTimeoutEvent().getTimeoutId();
//                    trigger(st, timer);
//
//                }
//            };

    private void sendLeaderPropose(VodAddress dest) {

        assert (dest != null);
        leaderProposeAwaitingResponse.add(dest.getId());

        LeaderProposeMsg.Request request =
                new LeaderProposeMsg.Request(self.getAddress(), dest,
                self.getId(), dest.getId(),
                self.getUtility().getValue());

        // No need for cleanup event to be caught, if connect fails, it fails silently
        delegator.doRetry(request, 3000, 1, 1.5, self.getOverlayId());

        logger.debug(compName + "trigger LeaderProposeMsg.Request from {} to {}", self, dest);
    }
    Handler<GradientSetsExchangeCycle> handleCycle = new Handler<GradientSetsExchangeCycle>() {

        @Override
        public void handle(GradientSetsExchangeCycle e) {

            VodAddress shuffleDest = similarSet.getSoftMaxPeerAddress();

            if (shuffleDest != null) {
                long total = 0;
                for (VodDescriptor d : similarSet.getAllSimilarPeers()) {
                    total += d.getUtility().getValue();
                }
                GradientStats.instance(self).setSumNeighbourUtilities(total);
                GradientStats.instance(self).incSentRequest();

                GradientSetsExchangeMsg.Request request =
                        new GradientSetsExchangeMsg.Request(self.getAddress(), shuffleDest,
                        self.getId(), shuffleDest.getId());
                if (self.getIp().equals(request.getSource().getIp()) == false) {
                    logger.error(compName + "WRONG IP. Addr {}. Ip: {}", self.getAddress(),
                            self.getIp());
                }

                // TODO: get setsExchangeDelay from RTT
                RTT retryTime = RTTStore.getRtt(self.getId(), shuffleDest);
                long rt = (retryTime == null) ? 2000 : retryTime.getRTO();
                ScheduleRetryTimeout schedule =
                        new ScheduleRetryTimeout(rt, 0); 
                GradientSetsExchangeMsg.RequestRetryTimeout requestTimeout =
                        new GradientSetsExchangeMsg.RequestRetryTimeout(schedule, request);
                TimeoutId id = delegator.doRetry(requestTimeout);

                logger.info(compName + "GradientSetsExchange.Request sent. "
                        + "clientId {} remoteId {} timeoutId" +
                        id + "\nsrc :" + self.getAddress() + 
                        " dest: " + shuffleDest +
                        "#parents: " + shuffleDest.getParents().size(), 
                        request.getClientId(), request.getRemoteId());            
                similarSet.incrementDescriptorAges();
            } else {
                logger.warn("No Peer available for Gradient Msg Exchange.");
            }
        }
    };
    Handler<GradientSetsExchangeMsg.Request> handleSetsExchangeRequest = new Handler<GradientSetsExchangeMsg.Request>() {

        @Override
        public void handle(GradientSetsExchangeMsg.Request request) {
            GradientStats.instance(self).incRecvdRequest();
                logger.info(compName + "GradientSetsExchange.Request is received. "
                        + "clientId {} remoteId {} timeoutId" +
                        request.getTimeoutId(), request.getClientId(), request.getRemoteId());            
                if (request.getVodSource().equals(self.getAddress())) {
                logger.warn("SetsExchangeRequest is received from itself.");
                return;
            }
            // TODO - utility is my utility for comparator. It should be the source node's utility.
            List<VodDescriptor> bestNeighbours = similarSet.getSimilarPeers(
                    numberOfBestSimilarPeers);
            GradientSetsExchangeMsg.Response response =
                    new GradientSetsExchangeMsg.Response(self.getAddress(), request,
                    bestNeighbours);
            delegator.doTrigger(response, network);
        }
    };
    Handler<GradientSetsExchangeMsg.Response> handleSetsExchangeResponse = new Handler<GradientSetsExchangeMsg.Response>() {

        @Override
        public void handle(Response event) {
            if (delegator.doCancelRetry(event.getTimeoutId())) {
                GradientStats.instance(self).incResponse();
                logger.info(compName + "GradientSetsExchange.Response is received. "
                        + "clientId {} remoteId {} timeoutId" +
                        event.getTimeoutId(), event.getClientId(), event.getRemoteId());
                if (event.getSimilarPeers() != null) {
                    similarSet.addSimilarPeers(event.getSimilarPeers());
                    delegator.doTrigger(new GradientSample(similarSet.getAllSimilarPeers()),
                            gradient);
                } else {
                    logger.warn("Simihlar peers list received in response is null.");
                }
            } else {
                logger.warn("GradientSetsExchange.Response is received late.");
            }
        }
    };
    Handler<GradientSetsExchangeMsg.RequestRetryTimeout> handleSetsExchangeRequestTimeout = new Handler<GradientSetsExchangeMsg.RequestRetryTimeout>() {

        @Override
        public void handle(RequestRetryTimeout event) {
            if (delegator.doCancelRetry(event.getTimeoutId())) {
                GradientStats.instance(self).incTimeouts();
                logger.info(compName + "GradientSetsExchangeMsg.RequestRetryTimeout "
                        + event.getRequestMsg().getVodSource().toString()
                        + "to " + event.getRequestMsg().getVodDestination().toString() +
                        " TimeoutId " + event.getTimeoutId());
                // remove from gradient neighbours
                similarSet.removeNeighbour(event.getRequestMsg().getVodDestination());
            }
        }
    };
    Handler<UtilityChanged> handleUtilityChanged = new Handler<UtilityChanged>() {

        @Override
        public void handle(UtilityChanged event) {
            GradientStats.instance(self).setUtility(self.getUtility().getValue());
            logger.debug(compName + "UtilityChanged event is received. New utility is:\r\n " + event.getNewUtility().toString());
            self.updateUtility(event.getNewUtility());
//            checkSimilarPeersAndSendProbes(event.getNewUtility());
        }
    };

    private void checkSimilarPeersAndSendProbes(Utility newUtility) {
//        if (!similarSet.isSimilarSetValid(self, newUtility, utilityThreshold)) {
        if (Math.abs(newUtility.getValue() - self.getUtility().getValue())
                > utilityThreshold) {
            List<VodDescriptor> bestPeers = similarSet.getSimilarPeers(numOfProbes, newUtility);

            ScheduleTimeout schedule = new ScheduleTimeout(probeRequestTimeout);
            schedule.setTimeoutEvent(new GradientSearchMsg.RequestRetryTimeout(schedule));
            TimeoutId timeoutId = schedule.getTimeoutEvent().getTimeoutId();
            outstandingSearchRequests.put(timeoutId, Math.min(numOfProbes, bestPeers.size()));

            for (VodDescriptor desc : bestPeers) {
                GradientSearchMsg.Request request =
                        new GradientSearchMsg.Request(self.getAddress(),
                        desc.getVodAddress(), self.getAddress(),
                        timeoutId, newUtility, searchTtl);
                request.setTimeoutId(timeoutId);
                delegator.doTrigger(request, network);
            }
            delegator.doTrigger(schedule, timer);
        }
    }
    Handler<GradientSearchMsg.Request> handleTargetUtilityRequest = new Handler<GradientSearchMsg.Request>() {

        @Override
        public void handle(GradientSearchMsg.Request request) {
//            if (!similarSet.isSimilarSetValid(request.getTargetUtility(),
//                    utilityThreshold) && request.getTtl() > 0) {
            if (request.getTargetUtility().getValue() < self.getUtility().getValue()
                    && request.getTtl() > 0) {
                List<VodDescriptor> bestPeers =
                        similarSet.getSimilarPeers(numOfProbes, self.getUtility());
                for (VodDescriptor desc : bestPeers) {
                    GradientSearchMsg.Request forwardReq =
                            new GradientSearchMsg.Request(request.getVodSource(),
                            desc.getVodAddress(), request.getOrigSrc(), request.getTimeoutId(),
                            request.getTargetUtility(), (byte) (request.getTtl() - 1));
                    delegator.doTrigger(forwardReq, network);
                }
            } else {
                List<VodDescriptor> bestPeers =
                        similarSet.getSimilarPeers(numberOfBestSimilarPeers, self.getUtility());
                GradientSearchMsg.Response response =
                        new GradientSearchMsg.Response(self.getAddress(),
                        request, bestPeers);
                delegator.doTrigger(response, network);
            }
        }
    };
    Handler<GradientSearchMsg.Response> handleTargetUtilityResponse = new Handler<GradientSearchMsg.Response>() {

        @Override
        public void handle(GradientSearchMsg.Response event) {
            TimeoutId timeoutId = event.getTimeoutId();
            if (outstandingSearchRequests.containsKey(timeoutId)) {
                similarSet.addSimilarPeers(event.getSimilarPeers());
                int numOfRequests = outstandingSearchRequests.get(timeoutId);
                numOfRequests -= 1;
                if (numOfRequests > 0) {
                    outstandingSearchRequests.put(timeoutId, numOfRequests);
                } else {
                    outstandingSearchRequests.remove(timeoutId);
                    CancelTimeout cancelTimeout = new CancelTimeout(timeoutId);
                    delegator.doTrigger(cancelTimeout, timer);
                    checkSimilarPeersAndSendProbes(self.getUtility());
                }
            } else {
                logger.warn("Probe message is received late. From node address " + event.getSource().toString());
            }
        }
    };
    Handler<GradientSearchMsg.RequestRetryTimeout> handleTargetUtilityProbeTimeout = new Handler<GradientSearchMsg.RequestRetryTimeout>() {

        @Override
        public void handle(GradientSearchMsg.RequestRetryTimeout e) {

            if (outstandingSearchRequests.containsKey(e.getTimeoutId())) {
                outstandingSearchRequests.remove(e.getTimeoutId());
                logger.warn("TargetUtilityProbe timed out.");
            }
        }
    };
    Handler<CroupierSample> handleCroupierSample = new Handler<CroupierSample>() {

        @Override
        public void handle(CroupierSample event) {
            List<VodDescriptor> newNodes = new ArrayList<VodDescriptor>();
            for (VodDescriptor d : event.getNodes()) {
                // create a copy of the VodDescriptors with the current node's overlayId
                newNodes.add(d.clone(self.getOverlayId()));
            }
            GradientStats.instance(self).incCroupierSample(newNodes.size());

            //  TODO - replace any old VodDescriptors with newer ones.
            List<VodDescriptor> removedNodes = similarSet.addSimilarPeers(newNodes);
            addFingers(removedNodes);
        }
    };

    /**
     * Static method to add a VodDescriptor to a set of VodDescriptors, where
     * you check if the VodDescriptor already exists, and only update it if the
     * age of the new VodDescriptor is lower.
     *
     * @param entry
     * @param d
     * @return
     */
    public static Set<VodDescriptor> addEntry(Set<VodDescriptor> entry, VodDescriptor d) {
        if (entry.contains(d)) {
            Iterator<VodDescriptor> iter = entry.iterator();
            VodDescriptor existing = iter.next();
            while (!existing.equals(d)) {
                existing = iter.next();
            }
            if (d.getAge() < existing.getAge()) {
                entry.add(d);
            }
        }
        return entry;
    }

    void addFingers(List<VodDescriptor> nodes) {
        for (VodDescriptor d : nodes) {
            UtilityRange ur = getUtilityRange(d.getUtility().getValue());
            Set<VodDescriptor> entries = fingers.get(ur);
            if (entries.contains(d)) {
                addEntry(entries, d);
            } else {
                entries.add(d);
            }
        }
    }

    UtilityRange getUtilityRange(int chunk) {
        UtilityRange range = null;
        for (UtilityRange ur : fingers.keySet()) {
            if (ur.inRange(chunk)) {
                range = ur;
                break;
            }
        }
        if (range == null) {
            throw new IllegalArgumentException("Chunk was invalid UtilityRange: " + chunk);
        }
        return range;
    }
    Handler<FingersRequest> handleFingersRequest = new Handler<FingersRequest>() {

        @Override
        public void handle(FingersRequest event) {
            List<VodDescriptor> nodes = new ArrayList<VodDescriptor>();
            UtilityRange range = getUtilityRange(event.getChunk());
            UtilityRange key = range;
            while (nodes.size() < event.getSize()) {
                Entry<UtilityRange, Set<VodDescriptor>> entry = fingers.ceilingEntry(key);
                if (!entry.getValue().isEmpty()) {
                    nodes.addAll(entry.getValue());
                }
                key = fingers.higherKey(range);
                if (key == null) {
                    key = fingers.firstKey();
                }
                // have checked all keys for descriptors, exit loop
                if (key.equals(range)) {
                    break;
                }
            }

            trigger(new FingersResponse(event, nodes), gradient);
        }
    };

    @Override
    public void stop(Stop stop) {
        if (setsGossipingTimeoutId != null) {
            CancelPeriodicTimeout cancelTimeout = new CancelPeriodicTimeout(setsGossipingTimeoutId);
            delegator.doTrigger(cancelTimeout, timer);
        }
        if (leaderSelectionTimeoutId != null) {
            CancelPeriodicTimeout clt = new CancelPeriodicTimeout(leaderSelectionTimeoutId);
            delegator.doTrigger(clt, timer);
        }

    }
}
