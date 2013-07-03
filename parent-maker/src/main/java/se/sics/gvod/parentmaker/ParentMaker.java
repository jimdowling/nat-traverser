/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package se.sics.gvod.parentmaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import se.sics.gvod.timer.TimeoutId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.RTTStore;
import se.sics.gvod.common.RTTStore.RTT;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.croupier.snapshot.Stats;
import se.sics.gvod.croupier.snapshot.CroupierStats;
import se.sics.gvod.hp.msgs.HpRegisterMsg;
import se.sics.gvod.hp.msgs.HpRegisterMsg.RegisterStatus;
import se.sics.gvod.hp.msgs.HpUnregisterMsg;
import se.sics.gvod.hp.msgs.ParentKeepAliveMsg;
import se.sics.gvod.hp.msgs.PRP_PreallocatedPortsMsg;
import se.sics.gvod.hp.msgs.PRP_ConnectMsg;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortDeleteRequest;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.parentmaker.evts.PrpDeletePortsResponse;
import se.sics.gvod.parentmaker.evts.PrpMorePortsResponse;
import se.sics.gvod.parentmaker.evts.PrpPortsResponse;
import se.sics.gvod.timer.*;
import se.sics.kompics.Handler;
import se.sics.kompics.Stop;
import se.sics.kompics.Positive;

/**
 *
 * overlayId == GVodConfig.RENDEZVOUS_SERVER_ID
 */
public class ParentMaker extends MsgRetryComponent {

    private static Logger logger = LoggerFactory.getLogger(ParentMaker.class);
    private Positive<NatNetworkControl> natNetworkControl = positive(NatNetworkControl.class);
    Self self;
    private String compName;
    Map<VodAddress, Connection> connections = new HashMap<VodAddress, Connection>();
    Map<Address, Long> rejections = new HashMap<Address, Long>();
    private Map<TimeoutId, Long> requestStartTimes = new HashMap<TimeoutId, Long>();
    private ParentMakerConfiguration config;
    
//    private int numParents;
//    private long keepParentRttRange;
//    private long retryDelay;
    private long needParentsRoundTimeout, fullParentsRoundTimeout = 60 * 1000;
//    private int numParentRetries;
//    private long childRemoveTimeout;
    private TimeoutId periodicTimeoutId;
//    private double scaleRetryDelay;
    private boolean outstandingBids = false;

    class Connection {

        private RTT rtt;
        private TimeoutId timeoutId;
        private long lastSentPing;
        private long lastReceivedPong;
        /*
         * For PRP-allocation-policy nodes, pre-bind a bunch of ports and send
         * them to the parent. If the parent needs more ports, it sends this
         * component a PRP_preallocatePortMsg.Request, which allocates more
         * ports and sends them back to the parent.
         */
        private final Set<Integer> prpPorts;

        public Connection(long lastKeepAliveMsgWasSentOn, RTT rtt, TimeoutId timeoutId,
                Set<Integer> prpPorts) {
            if (rtt == null || timeoutId == null) {
                throw new NullPointerException("RTT or timeoutId cannot be null.");
            }
            this.rtt = rtt;
            this.timeoutId = timeoutId;
            this.lastSentPing = lastKeepAliveMsgWasSentOn;
            if (prpPorts != null) {
                this.prpPorts = prpPorts;
            } else {
                this.prpPorts = new HashSet<Integer>();
            }
        }

        public Set<Integer> getPrpPorts() {
            return prpPorts;
        }

        public void addPrpPorts(Set<Integer> newPorts) {
            prpPorts.addAll(newPorts);
        }

        public boolean removePrpPort(int port) {
            return prpPorts.remove(port);
        }

        public boolean hasPrpPort(int port) {
            return prpPorts.contains(port);
        }

        public int sizePrpPorts() {
            return prpPorts.size();
        }

        public long getLastReceivedPong() {
            return lastReceivedPong;
        }

        public long getLastSentPing() {
            return lastSentPing;
        }

        public void setLastReceivedPong(long lastReceivedPong) {
            this.lastReceivedPong = lastReceivedPong;
        }

        public void setLastSentPing(long lastSentPing) {
            this.lastSentPing = lastSentPing;
        }

        public TimeoutId getTimeoutId() {
            return timeoutId;
        }

        public void setTimeoutId(TimeoutId timeoutId) {
            this.timeoutId = timeoutId;
        }

        public void setRtt(RTT rtt) {
            this.rtt = rtt;
        }

        public RTT getRtt() {
            return rtt;
        }
    }

    static class KeepBindingOpenTimeout extends Timeout {

        private final VodAddress parent;

        public KeepBindingOpenTimeout(SchedulePeriodicTimeout spt, VodAddress parent) {
            super(spt);
            this.parent = parent;
        }

        public VodAddress getParent() {
            return parent;
        }
    }

    public ParentMaker() {
        this(null);
    }

    public ParentMaker(RetryComponentDelegator delegator) {
        super(delegator);
        this.delegator.doAutoSubscribe();
    }
    Handler<ParentMakerInit> handleInit = new Handler<ParentMakerInit>() {
        @Override
        public void handle(ParentMakerInit init) {
            self = init.getSelf();
            compName = "PM(" + self.getId() + ") ";
            config = init.getConfig();
            
//            numParents = init.getConfig().getParentSize();
            VodConfig.PM_PARENT_SIZE = config.getParentSize();
            
//            keepParentRttRange = init.getConfig().getKeepParentRttRange();
//            retryDelay = init.getConfig().getRto();
//            numParentRetries = init.getConfig().getRtoRetries();
//            scaleRetryDelay = init.getConfig().getRtoScale();
//            childRemoveTimeout = init.getConfig().getChildRemoveTimeout();
            if (!self.isOpen()) {
                needParentsRoundTimeout = init.getConfig().getParentUpdatePeriod();
                ScheduleTimeout spt = new ScheduleTimeout(0);
                spt.setTimeoutEvent(new ParentMakerCycle(spt));
                delegator.doTrigger(spt, timer);
                periodicTimeoutId = spt.getTimeoutEvent().getTimeoutId();
            }
            logger.info(compName + "started");
        }
    };
    int count = 0;
    Handler<ParentMakerCycle> handleCycle = new Handler<ParentMakerCycle>() {
        @Override
        public void handle(ParentMakerCycle e) {
            List<RTT> currentRtts = getCurrentRtts();

            if (count++ > 5 && self.getParents().isEmpty()) {
                Stats pi = CroupierStats.instance(self.clone(VodConfig.SYSTEM_OVERLAY_ID));
                if (pi == null) {
                    logger.warn(compName + " Cannot find peer");
                } else {
                    logger.warn(compName + " no parents! " + self.getNat()
                            + pi
                            + " rejected: " + rejections.keySet().size()
                            + " num better rtts " + RTTStore.getOnAvgBest(self.getId(), 
                            config.getParentSize(),
                            rejections.keySet()).size());
                }
            }


            List<RTT> betterRtts;
            if (currentRtts.size() < config.getParentSize()) {
                betterRtts = RTTStore.getOnAvgBest(self.getId(), config.getParentSize(),
                        rejections.keySet());
                if (betterRtts.isEmpty()) {
                    logger.warn(compName + "No better RTTS availabile ");
                }
            } else {
                RTT worstRtt = Collections.max(currentRtts, RTT.Order.ByRto);
                betterRtts = RTTStore.getAllOnAvgBetterRtts(self.getId(), worstRtt.getRTO(), 
                        config.getKeepParentRttRange());
            }

            List<RTT> allRtts = new ArrayList<RTT>();
            allRtts.addAll(currentRtts);
            allRtts.addAll(betterRtts);
            if (!allRtts.isEmpty()) {
                Collections.sort(allRtts, RTT.Order.ByRto);
            }
            Iterator<RTT> iter = allRtts.iterator();
            for (int i = 0; i < (config.getParentSize() - currentRtts.size()); i++) {
                if (!iter.hasNext()) {
                    logger.debug(compName + " no RTT samples available");
                    removeRejections(config.getParentSize());
                    break;
                }
                RTT min = iter.next();
                if (min.getAddress().isOpen() == false) {
                    logger.error("Trying to add a NAT node as a parent!");
                    throw new IllegalStateException("Error with nat parent");
                }
                if (!currentRtts.contains(min)) {
                    if (self.getAddress().getNat().preallocatePorts()) {
                        PortAllocRequest allocReq = new PortAllocRequest(self.getId(), 1);
                        PrpPortsResponse allocResp = new PrpPortsResponse(allocReq, 
                                ToVodAddr.hpServer(min.getAddress().getPeerAddress()),
                                min.getRTO());
                        allocReq.setResponse(allocResp);
                        delegator.doTrigger(allocReq, natNetworkControl);
                    } else {

                        // if i have no connections, bid for the parent's slot with RTO as '0'
                        long normalizedRtt = 
                                (connections.isEmpty() && !outstandingBids) ? 0 : min.getRTO();
                        sendRequest(min.getAddress().getPeerAddress(), normalizedRtt, new HashSet<Integer>());
                    }
                } else {
                    i--;
                }
            }

            // Cleanup all the parents that sent us rejections
            Set<Address> cleanupOldRejections = new HashSet<Address>();
            for (Entry<Address, Long> entry : rejections.entrySet()) {
                if (entry.getValue() > VodConfig.PM_PARENT_REJECTED_CLEANUP_TIMEOUT) {
                    cleanupOldRejections.add(entry.getKey());
                }
            }
            for (Address v : cleanupOldRejections) {
                rejections.remove(v);
            }

            long nextRound = needParentsRoundTimeout;
            if (config.getParentSize() == VodConfig.PM_PARENT_SIZE) {
                nextRound = fullParentsRoundTimeout;
            }

            ScheduleTimeout spt = new ScheduleTimeout(nextRound);
            spt.setTimeoutEvent(new ParentMakerCycle(spt));
            delegator.doTrigger(spt, timer);
            periodicTimeoutId = spt.getTimeoutEvent().getTimeoutId();
        }
    };

    /**
     * remove a random number of rejected server
     *
     * @param number
     */
    private void removeRejections(int number) {
        List<Address> nodes = new ArrayList<Address>();
        nodes.addAll(rejections.keySet());
        Random r = new Random(VodConfig.getSeed());
        if (number > rejections.size()) {
            number = rejections.size();
        }
        for (int i = 0; i < number; i++) {
            int n = r.nextInt(nodes.size());
            rejections.remove(nodes.get(n));
        }
    }

    private void addParent(VodAddress parent, RTT rtt, Set<Integer> prpPorts) {
        if (!connections.containsKey(parent)) {
            // Ping my parents 5 seconds before the NAT binding will timeout
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(
                    self.getNat().getBindingTimeout() - 5000, 
                    self.getNat().getBindingTimeout() - 5000
                    );
            KeepBindingOpenTimeout pt = new KeepBindingOpenTimeout(spt, parent);
            spt.setTimeoutEvent(pt);
            TimeoutId timeoutId = pt.getTimeoutId();
            delegator.doTrigger(spt, timer);
            Connection connection = new Connection(System.currentTimeMillis(), rtt, timeoutId, prpPorts);
            connections.put(parent, connection);
            self.addParent(parent.getPeerAddress());
            logger.debug(compName + "updated parents: " + printMyParents());
        } else {
            Connection c = connections.get(parent);
            c.setLastReceivedPong(System.currentTimeMillis());
            c.setRtt(rtt);
            logger.info(compName + "Re-adding an existing parent: " + parent.toString());
        }
    }

    private void removeParent(VodAddress parent, boolean failed, boolean sendUnregisterReq) {
        if (failed) {
            RTTStore.removeSamples(self.getId(), parent);
            rejections.put(parent.getPeerAddress(), System.currentTimeMillis());
        }

        Connection c = connections.get(parent);
        if (c != null) {
            // cancel periodic pinging to parent
            TimeoutId timeoutId = c.getTimeoutId();
            delegator.doTrigger(new CancelPeriodicTimeout(timeoutId), timer);
            if (sendUnregisterReq) {
                // unregister from z-server
                HpUnregisterMsg.Request req = new HpUnregisterMsg.Request(self.getAddress(),
                        parent, config.getChildRemoveTimeout(), HpRegisterMsg.RegisterStatus.BETTER_PARENT);
                delegator.doRetry(req, config.getRto(), config.getRtoRetries());
            }
            connections.remove(parent);
            if (!c.getPrpPorts().isEmpty()) { // free-up any PRP ports cached at the parent
                PortDeleteRequest pdr = new PortDeleteRequest(self.getId(), c.getPrpPorts());
                pdr.setResponse(new PrpDeletePortsResponse(pdr, null));
                delegator.doTrigger(pdr, natNetworkControl);
            }

        } else {
            logger.warn(compName + "Tried to remove non-existant parent: " + parent.getId());
            logger.warn(compName + "Existing parents: " + printMyParents());
        }
        self.removeParent(parent.getPeerAddress());
    }
    Handler<PrpDeletePortsResponse> handlePrpDeletePortsResponse = new Handler<PrpDeletePortsResponse>() {
        @Override
        public void handle(PrpDeletePortsResponse event) {
            // nothing to do
        }
    };
    Handler<KeepBindingOpenTimeout> handleKeepBindingOpenTimeout = new Handler<KeepBindingOpenTimeout>() {
        @Override
        public void handle(KeepBindingOpenTimeout event) {
            Connection c = connections.get(event.getParent());
            if (c != null) {
                long now = System.currentTimeMillis();
                c.setLastSentPing(now);
                ParentKeepAliveMsg.Ping ping = new ParentKeepAliveMsg.Ping(self.getAddress(), event.getParent());
                ScheduleRetryTimeout st = 
                        new ScheduleRetryTimeout(config.getPingRto(), 
                        config.getPingRetries(), config.getPingRtoScale());
                ParentKeepAliveMsg.PingTimeout pt = new ParentKeepAliveMsg.PingTimeout(st, ping);
                TimeoutId id = delegator.doRetry(pt);
                requestStartTimes.put(id, now);
            } else {
                logger.debug(compName + "No connection to parent to send Ping!");
            }
        }
    };
    Handler<ParentKeepAliveMsg.Pong> handleKeepAlivePong = new Handler<ParentKeepAliveMsg.Pong>() {
        @Override
        public void handle(ParentKeepAliveMsg.Pong event) {
            if (cancelRetry(event.getTimeoutId())) {
                Connection c = connections.get(event.getVodSource());
                if (c != null) {
                    long t = System.currentTimeMillis();
                    c.setLastReceivedPong(t);
                    Long startTime = requestStartTimes.remove(event.getTimeoutId());
                    if (startTime != null) {
                        long rttValue = t - startTime;
                        RTTStore.addSample(self.getId(), event.getVodSource(), rttValue);
                    } else {
                        logger.warn("Couldn't find startTime at {} from {} for: "
                                + event.getTimeoutId(), self.getAddress(),
                                event.getVodSource());
                    }
                } else {
                    logger.debug(compName + "No connection to parent to receive Pong!");
                }
            }
        }
    };
    /**
     * parent is not responding to pings. Remove if enough parents left?
     */
    Handler<ParentKeepAliveMsg.PingTimeout> handleKeepAliveMsgPingTimeout = new Handler<ParentKeepAliveMsg.PingTimeout>() {
        @Override
        public void handle(ParentKeepAliveMsg.PingTimeout event) {
            if (delegator.doCancelRetry(event.getTimeoutId())) {
                requestStartTimes.remove(event.getTimeoutId());
                VodAddress parent = event.getRequestMsg().getVodDestination();
                CroupierStats.instance(self.clone(VodConfig.SYSTEM_OVERLAY_ID)).parentChangeEvent(parent.getPeerAddress(),
                        HpRegisterMsg.RegisterStatus.DEAD_PARENT);
                removeParent(parent, true, true);
                logger.debug(compName + "Ping timeout to parent {} . Removing.", parent.getPeerAddress());
            }
        }
    };


    private void sendRequest(Address hpServer, long rtt, Set<Integer> prpPorts) {
        VodAddress dest = ToVodAddr.hpServer(hpServer);
        HpRegisterMsg.Request request = new HpRegisterMsg.Request(self.getAddress(),
                dest, self.getAddress().getDelta(), rtt, prpPorts);
        outstandingBids = true;
        ScheduleRetryTimeout st = new ScheduleRetryTimeout(config.getRto(), 
                config.getRtoRetries(), config.getRtoScale());
        HpRegisterMsg.RequestRetryTimeout requestTimeout =
                new HpRegisterMsg.RequestRetryTimeout(st, request);
        TimeoutId id = delegator.doRetry(requestTimeout);
        logger.trace(compName + "Request sent to " + hpServer);
        requestStartTimes.put(id, System.currentTimeMillis());
    }
    Handler<HpRegisterMsg.Response> handleHpRegisterMsgResponse = new Handler<HpRegisterMsg.Response>() {
        @Override
        public void handle(HpRegisterMsg.Response event) {
            // discard duplicate responses or late responses - unless I don't have any parents
            if (delegator.doCancelRetry(event.getTimeoutId()) || connections.isEmpty()) {
                CroupierStats.instance(self.clone(VodConfig.SYSTEM_OVERLAY_ID)).parentChangeEvent(event.getSource(),
                        event.getResponseType());
                outstandingBids = false;
                VodAddress peer = event.getVodSource();
                TimeoutId id = event.getTimeoutId();
                Long startTime = requestStartTimes.remove(id);
                if (event.getResponseType() == HpRegisterMsg.RegisterStatus.REJECT) {
                    rejections.put(peer.getPeerAddress(), System.currentTimeMillis());
                    logger.debug(compName + "Parent {} rejected client request",
                            peer.getPeerAddress());
                    // free-up the ports that were allocated
                    if (self.getNat().preallocatePorts()) {
                        PortDeleteRequest dReq = new PortDeleteRequest(self.getId(), event.getPrpPorts());
                        dReq.setResponse(new PrpDeletePortsResponse(dReq, null));
                        trigger(dReq, natNetworkControl);
                    }
                } else if (event.getResponseType() != HpRegisterMsg.RegisterStatus.ACCEPT) {
                    logger.warn(compName + "Parent {} client request failed due to "
                            + event.getResponseType(), peer.getPeerAddress());
                    if (self.getNat().preallocatePorts()) {
                        PortDeleteRequest dReq = new PortDeleteRequest(self.getId(), event.getPrpPorts());
                        dReq.setResponse(new PrpDeletePortsResponse(dReq, null));
                        trigger(dReq, natNetworkControl);
                    }
                } else {
                    // ACCEPT
                    long rttValue = 5000;
                    if (startTime != null) {
                        logger.debug(compName + "Parent {} accepted client request", peer.getPeerAddress());
                        rttValue = System.currentTimeMillis() - startTime;
                    } else {
                        logger.warn("Couldn't find startTime at {} from {} for: "
                                + event.getTimeoutId(), self.getAddress(),
                                event.getVodSource());
                    }
                    RTT rtt = RTTStore.addSample(self.getId(), peer, rttValue);

                    if (rtt != null) {
                        // TODO check if already added, as then I should not start another timer
                        if (connections.size() < config.getParentSize()) {
                            addParent(peer, rtt, event.getPrpPorts());
                        } else {
                            List<RTT> currentRtts = getCurrentRtts();
                            RTT worstRtt = Collections.max(currentRtts, RTT.Order.ByRto);
                            if (rttValue + config.getKeepParentRttRange() < worstRtt.getRTO()) {
                                VodAddress hpAddr = ToVodAddr.hpServer(worstRtt.getAddress().getPeerAddress());
                                removeParent(hpAddr, false, true);
                                logger.info(compName + "Found a better parent {} . Removing {}."
                                        + "Old/new RTTS = " + worstRtt.getRTO() + " vs "
                                        + rttValue,
                                        peer.getId(),
                                        hpAddr.getId());
                                addParent(peer, rtt, event.getPrpPorts());
                            }
                        }
                    } else {
                        logger.warn("Couldn't add as a parent - no RTT object: {}", peer);
                    }
                }
            } else {
                logger.warn(compName + "cancelRetry for ParentRequest failed");
                // send unregister request to parent, as I don't want it as my parent.
                HpUnregisterMsg.Request req = new HpUnregisterMsg.Request(self.getAddress(),
                        event.getVodSource(), 0, HpRegisterMsg.RegisterStatus.PARENT_REQUEST_FAILED);
                delegator.doTrigger(req, network);
                if (self.getNat().preallocatePorts()) {
                    PortDeleteRequest pdr = new PortDeleteRequest(self.getId(), event.getPrpPorts());
                    pdr.setResponse(new PrpDeletePortsResponse(pdr, null));
                    trigger(pdr, natNetworkControl);
                }
                CroupierStats.instance(self.clone(VodConfig.SYSTEM_OVERLAY_ID)).parentChangeEvent(event.getSource(),
                        HpRegisterMsg.RegisterStatus.PARENT_REQUEST_FAILED);
            }

        }
    };
    Handler<HpRegisterMsg.RequestRetryTimeout> handleHpRegisterMsgRequestTimeout =
            new Handler<HpRegisterMsg.RequestRetryTimeout>() {
        @Override
        public void handle(HpRegisterMsg.RequestRetryTimeout event) {
            if (delegator.doCancelRetry(event.getTimeoutId())) {
                CroupierStats.instance(self.clone(VodConfig.SYSTEM_OVERLAY_ID)).parentChangeEvent(event.getRequest().getDestination(),
                        RegisterStatus.PARENT_REQUEST_FAILED);
                outstandingBids = false;
                requestStartTimes.remove(event.getTimeoutId());
                rejections.put(event.getMsg().getDestination(), System.currentTimeMillis());
                logger.warn(compName + "timeout HpRegisterReq {}",
                        event.getMsg().getDestination());
                if (self.getNat().preallocatePorts()) {
                    PortDeleteRequest pdr =
                            new PortDeleteRequest(self.getId(), event.getRequest().getPrpPorts());
                    pdr.setResponse(new PrpDeletePortsResponse(pdr, null));
                    trigger(pdr, natNetworkControl);
                }
            }
        }
    };
    Handler<HpUnregisterMsg.Response> handleUnregisterParentResponse =
            new Handler<HpUnregisterMsg.Response>() {
        @Override
        public void handle(HpUnregisterMsg.Response event) {
            if (delegator.doCancelRetry(event.getTimeoutId())) {
                logger.info(compName + "Successfully unregistered from {}",
                        event.getSource().getId());
            } else {
                logger.info(compName + "Unsuccessful unregister from {}",
                        event.getSource().getId());
            }
        }
    };
    Handler<HpUnregisterMsg.Request> handleUnregisterParentRequest =
            new Handler<HpUnregisterMsg.Request>() {
        @Override
        public void handle(HpUnregisterMsg.Request event) {
            logger.info(compName + "Parent {} telling me to unregister: {}",
                    event.getVodSource().getId(), event.getStatus());
            CroupierStats.instance(self.clone(VodConfig.SYSTEM_OVERLAY_ID)).parentChangeEvent(event.getSource(),
                    HpRegisterMsg.RegisterStatus.BETTER_CHILD);
            removeParent(event.getVodSource(), false, false);
            logger.info(compName + printMyParents());
        }
    };

    private List<RTT> getCurrentRtts() {
        List<RTT> currentRtts = new ArrayList<RTT>();
        for (Connection c : connections.values()) {
            currentRtts.add(c.getRtt());
        }
        return currentRtts;
    }

    @Override
    public void stop(Stop stop) {
        if (stop == null) {
            return;
        }
        if (periodicTimeoutId != null) {
            delegator.doTrigger(new CancelPeriodicTimeout(periodicTimeoutId), timer);
            periodicTimeoutId = null;
        }
    }

    private String printMyParents() {
        StringBuilder sb = new StringBuilder();
        sb.append(" Current parents: ");
        for (VodAddress a : connections.keySet()) {
            sb.append(a.getId()).append(";");
        }
        return sb.toString();
    }
    Handler<PRP_PreallocatedPortsMsg.Request> handlePRP_PreallocatedPortsMsg = new Handler<PRP_PreallocatedPortsMsg.Request>() {
        @Override
        public void handle(PRP_PreallocatedPortsMsg.Request request) {

            if (!connections.containsKey(request.getVodSource())) {
                PRP_PreallocatedPortsMsg.Response resp = 
                        new PRP_PreallocatedPortsMsg.Response(self.getAddress(),
                        request.getVodSource(), request.getTimeoutId(),
                        PRP_PreallocatedPortsMsg.ResponseType.INVALID_NOT_A_PARENT,
                        null, request.getMsgTimeoutId());
                delegator.doTrigger(resp, network);
            } else {
                Long rto = 2000l;
                if (RTTStore.getRtt(self.getId(),
                        request.getVodSource()) != null) {
                    rto = RTTStore.getRtt(self.getId(),
                            request.getVodSource()).getRTO();
                }
                allocPorts(request.getVodSource(), rto, request.getTimeoutId(),
                        request.getMsgTimeoutId());
            }
        }
    };

    private void allocPorts(VodAddress server, Long rto, TimeoutId timeoutId, 
            TimeoutId msgTimeoutId) {
        PortAllocRequest allocReq = new PortAllocRequest(self.getId(), 10);
        PrpMorePortsResponse allocResp = new PrpMorePortsResponse(allocReq, server, 
                timeoutId, rto, msgTimeoutId);
        allocReq.setResponse(allocResp);
        delegator.doTrigger(allocReq, natNetworkControl);
    }
    Handler<PrpMorePortsResponse> handlePrpMorePortsResponse =
            new Handler<PrpMorePortsResponse>() {
        @Override
        public void handle(PrpMorePortsResponse response) {
            PRP_PreallocatedPortsMsg.Response resp = new PRP_PreallocatedPortsMsg.Response(
                    self.getAddress(), (VodAddress) response.getKey(), 
                    response.getTimeoutId(), 
                    PRP_PreallocatedPortsMsg.ResponseType.OK,
                    response.getAllocatedPorts(), response.getMsgTimeoutId());
            delegator.doTrigger(resp, network);
        }
    };
    Handler<PrpPortsResponse> handlePrpPortsResponse =
            new Handler<PrpPortsResponse>() {
        @Override
        public void handle(PrpPortsResponse response) {
            long rtt = (connections.isEmpty() && !outstandingBids) ? 0
                    : response.getRto();
            sendRequest(response.getServer().getPeerAddress(), rtt, response.getAllocatedPorts());
        }
    };
    // zServer tells me that it is using a PRP port. Delete it locally from my connections.
    // use this info when changing parent.
    Handler<PRP_ConnectMsg.Response> handlePRP_Response = new Handler<PRP_ConnectMsg.Response>() {
        @Override
        public void handle(PRP_ConnectMsg.Response response) {
            int port = response.getPortToUse();
            for (Connection c : connections.values()) {
                if (c.hasPrpPort(port)) {
                    c.removePrpPort(port);
                }
            }
        }
    };
    Handler<Stop> handleStop = new Handler<Stop>() {
        @Override
        public void handle(Stop stop) {
            for (VodAddress parent : connections.keySet()) {
                removeParent(parent, false, true);
            }
        }
    };
}
