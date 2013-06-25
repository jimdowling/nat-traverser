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
package se.sics.gvod.croupier;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.*;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.msgs.RelayMsgNetty;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.croupier.events.*;
import se.sics.gvod.croupier.msgs.ShuffleMsg;
import se.sics.gvod.croupier.snapshot.CroupierStats;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.timer.CancelPeriodicTimeout;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Stop;

/**
 *
 */
public class Croupier extends MsgRetryComponent {

    private static Logger logger = LoggerFactory.getLogger(Croupier.class);
    Negative<CroupierPort> croupierPort = negative(CroupierPort.class);
    Negative<PeerSamplePort> peerSamplePort = negative(PeerSamplePort.class);
    private Self self;
    View publicView;
    View privateView;
    private boolean joining;
    CroupierConfiguration config;    
    private TimeoutId shuffleTimeoutId;
    String compName;
    private Map<Integer,Long> shuffleTimes = new HashMap<Integer,Long>();

    public Croupier() {
        this(null);
    }

    public Croupier(RetryComponentDelegator delegator) {
        super(delegator);
        this.delegator.doAutoSubscribe();
    }
    Handler<CroupierInit> handleInit = new Handler<CroupierInit>() {
        @Override
        public void handle(CroupierInit init) {
            self = init.getSelf();
            compName = "(" + self.getId() + ") ";
            config = init.getConfig();
            publicView = new View(self, config.getViewSize(), config.getSeed());
            privateView = new View(self, config.getViewSize(), config.getSeed());
            self.updateUtility(new UtilityVod(0));
            CroupierStats.addNode(self.getAddress());
        }
    };

    private boolean initializeCaches(List<VodDescriptor> nodes) {
        if (nodes.isEmpty()) {
            return false;
        }
        Set<VodDescriptor> pub = new HashSet<VodDescriptor>();
        Set<VodDescriptor> priv = new HashSet<VodDescriptor>();
        for (VodDescriptor n : nodes) {
            if (self.getId() == n.getVodAddress().getId()) {
                logger.warn("Trying to add myself to self: {}", self.getId());
            } else {
                if (n.getVodAddress().isOpen()) {
                    pub.add(n);
                } else {
                    priv.add(n);
                }
            }
        }
        publicView.initialize(pub);
        privateView.initialize(priv);
        return true;
    }

    private VodAddress selectPeerToShuffleWith() {
        VodAddress node = null;
        if (!publicView.isEmpty()) {
            node = publicView.selectPeerToShuffleWith(config.getPolicy());
        } else if (!privateView.isEmpty()) {
            node = privateView.selectPeerToShuffleWith(config.getPolicy());
        }
        return node;
    }
    Handler<CroupierJoin> handleJoin = new Handler<CroupierJoin>() {
        @Override
        public void handle(CroupierJoin event) {
            List<VodDescriptor> insiders = event.getVodInsiders();

            logger.debug(compName + "JOIN {} using {} public nodes", self.getId(), insiders.size());

            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(config.getShufflePeriod(), 
                    config.getShufflePeriod());
            spt.setTimeoutEvent(new CroupierShuffleCycle(spt));
            delegator.doTrigger(spt, timer);

            if (!initializeCaches(insiders)) {
                logger.warn(compName + "No insiders, not shuffling.");
                // I am the first peer
                delegator.doTrigger(new CroupierJoinCompleted(), croupierPort);
                // schedule shuffling
                return;
            }

            logger.trace(compName + "initiateShuffle join");
            joining = true;

            // Send bootstrap descriptors to Vod Component and others
            publishSample();
        }
    };
    

    private void initiateShuffle(int shuffleSize, VodAddress node) {
        if (node == null) {
            return;
        }

        List<VodDescriptor> publicDescriptors =
                publicView.selectToSendAtActive(shuffleSize, node);

        if (self.isOpen()) {
            publicDescriptors.add(self.getDescriptor());
        }

        List<VodDescriptor> privateDescriptors =
                privateView.selectToSendAtActive(shuffleSize, node);

        if (!self.isOpen()) {
            privateDescriptors.add(self.getDescriptor());
        }

        DescriptorBuffer buffer = new DescriptorBuffer(self.getAddress(),
                publicDescriptors, privateDescriptors);


        ScheduleRetryTimeout st =
                new ScheduleRetryTimeout(config.getShuffleTimeout(), 0);
        ShuffleMsg.Request request = new ShuffleMsg.Request(self.getAddress(), node,
                buffer, self.getDescriptor());
        ShuffleMsg.RequestTimeout retryRequest = new ShuffleMsg.RequestTimeout(st, request);
        TimeoutId id = delegator.doRetry(retryRequest);
        
        
        shuffleTimes.put(id.getId(), System.currentTimeMillis());
        logger.debug(compName + "SHUFFLE SENT from {} to {} . Id=" + id, self.getId(), node.getId());
    }
    Handler<CroupierShuffleCycle> handleCycle = new Handler<CroupierShuffleCycle>() {
        @Override
        public void handle(CroupierShuffleCycle event) {
            logger.trace(compName + "shuffle: Pub({})/Priv({})", publicView.size(),
                    privateView.size());
            VodAddress peer = selectPeerToShuffleWith();

            if (peer != null) {
                if (!peer.isOpen()) {
                    logger.info(compName + "Didn't pick a public node for shuffling. Public Size {}",
                            publicView.getAll().size());
                }

                CroupierStats.instance(self).incSelectedTimes();
                initiateShuffle(config.getShuffleLength(), peer);

            }

            publicView.incrementDescriptorAges();
            privateView.incrementDescriptorAges();
        }
    };

    /**
     * handle requests to shuffle
     */
    Handler<ShuffleMsg.Request> handleShuffleRequest = new Handler<ShuffleMsg.Request>() {
        @Override
        public void handle(ShuffleMsg.Request msg) {
            logger.debug(compName + "SHUFFLE_REQ recvd by {} from {} with id" + msg.getTimeoutId(),
                    msg.getVodDestination().getId(),
                    msg.getDesc().getVodAddress().getId());

            if (msg.getVodSource().getId() == self.getId()) {
                logger.warn("Tried to shuffle with myself");
                return;
            }

            VodAddress srcAddress = msg.getDesc().getVodAddress();
            CroupierStats.instance(self).incShuffleRecvd(msg.getVodSource());
            DescriptorBuffer recBuffer = msg.getBuffer();
            List<VodDescriptor> recPublicDescs = recBuffer.getPublicDescriptors();
            List<VodDescriptor> recPrivateDescs = recBuffer.getPublicDescriptors();
            List<VodDescriptor> toSendPublicDescs = publicView.selectToSendAtPassive(
                    recPublicDescs.size(), srcAddress);
            List<VodDescriptor> toSendPrivateDescs = privateView.selectToSendAtPassive(
                    recPrivateDescs.size(), srcAddress);

            DescriptorBuffer toSendBuffer =
                    new DescriptorBuffer(self.getAddress(), toSendPublicDescs, toSendPrivateDescs);

            publicView.selectToKeep(srcAddress, recBuffer.getPublicDescriptors());
            privateView.selectToKeep(srcAddress, recBuffer.getPrivateDescriptors());

            logger.trace(compName + "SHUFFLE_REQ from {}. r={} public + {} private s={} public + {} private", new Object[]{srcAddress.getId(),
                recPublicDescs.size(), recPrivateDescs.size(), toSendPublicDescs.size(), toSendPrivateDescs.size()});

            ShuffleMsg.Response response = new ShuffleMsg.Response(self.getAddress(),
                    msg.getVodSource(), msg.getClientId(), msg.getRemoteId(),
                    msg.getNextDest(), msg.getTimeoutId(), RelayMsgNetty.Status.OK,
                    toSendBuffer, self.getDescriptor());

            logger.trace(compName + "trigger ShuffleMsg.Response");

            delegator.doTrigger(response, network);

            publishSample();

        }
    };
    /**
     * handle the response to a shuffle with the partial view of the other node
     */
    Handler<ShuffleMsg.Response> handleShuffleResponse = new Handler<ShuffleMsg.Response>() {
        @Override
        public void handle(ShuffleMsg.Response event) {
            logger.trace(compName + "SHUFFLE_RES from {} with ID {}", event.getVodSource().getId(),
                    event.getTimeoutId());
            if (delegator.doCancelRetry(event.getTimeoutId())) {


                if (self.getAddress() == null) {
                    logger.warn(compName + "SELF is null, not handling Shuffle Response");
                    return;
                }

                CroupierStats.instance(self).incShuffleResp();


                long timeStarted = shuffleTimes.remove(event.getTimeoutId().getId());
                RTTStore.addSample(self.getId(), event.getVodSource(), 
                        System.currentTimeMillis()-timeStarted);
                
                if (joining) {
                    joining = false;
                    delegator.doTrigger(new CroupierJoinCompleted(), croupierPort);
                }

                VodDescriptor srcDesc = event.getDesc();
                DescriptorBuffer recBuffer = event.getBuffer();
                List<VodDescriptor> recPublicDescs = recBuffer.getPublicDescriptors();
                List<VodDescriptor> recPrivateDescs = recBuffer.getPrivateDescriptors();

                publicView.selectToKeep(srcDesc.getVodAddress(), recPublicDescs);
                privateView.selectToKeep(srcDesc.getVodAddress(), recPrivateDescs);


                publishSample();
            }
        }
    };
    /**
     * Handle shuffle timeout when a node is too slow to shuffle
     */
    Handler<ShuffleMsg.RequestTimeout> handleShuffleTimeout = new Handler<ShuffleMsg.RequestTimeout>() {
        @Override
        public void handle(ShuffleMsg.RequestTimeout event) {
            logger.warn(compName + "SHUFFLE TIMED OUT " + event.getPeer().getId()
                    + " RS=" + getAll().size()
                    + " Nat=" + event.getPeer().getNatAsString());
            CroupierStats.instance(self).incShuffleTimeout();
            shuffleTimes.remove(event.getTimeoutId().getId());
            
            VodAddress suspected = event.getPeer();
            if (suspected.isOpen()) {
                publicView.timedOutForShuffle(suspected);
            } else {
                privateView.timedOutForShuffle(suspected);
            }
        }
    };

    List<VodDescriptor> getAll() {
        List<VodDescriptor> nodes = new ArrayList<VodDescriptor>();
        nodes.addAll(publicView.getAll());
        nodes.addAll(privateView.getAll());
        return nodes;
    }

    private void publishSample() {
        List<VodDescriptor> nodes = getAll();
        if (!nodes.isEmpty()) {
            delegator.doTrigger(new CroupierSample(nodes), peerSamplePort);
        }
    }

    @Override
    public void stop(Stop event) {
        if (shuffleTimeoutId != null) {
            CancelPeriodicTimeout cPT = new CancelPeriodicTimeout(shuffleTimeoutId);
            delegator.doTrigger(cPT, timer);
        }
    }
}
