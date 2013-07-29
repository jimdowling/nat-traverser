package se.sics.gvod.nat.traversal;

import se.sics.gvod.config.NatTraverserConfiguration;
import se.sics.gvod.nat.traversal.events.NatTraverserInit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import se.sics.gvod.timer.TimeoutId;
import java.util.concurrent.ConcurrentHashMap;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.gvod.timer.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.RTTStore;
import se.sics.gvod.common.RTTStore.RTT;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.evts.GarbageCleanupTimeout;
import se.sics.gvod.common.evts.Join;
import se.sics.gvod.common.msgs.RelayMsgNetty;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.filters.MsgDestFilterOverlayId;
import se.sics.gvod.nat.hp.client.HpClient;
import se.sics.gvod.config.HpClientConfiguration;
import se.sics.gvod.nat.hp.client.HpClientInit;
import se.sics.gvod.nat.hp.client.HpClientPort;
import se.sics.gvod.nat.hp.client.OpenedConnection;
import se.sics.gvod.nat.hp.client.events.DeleteConnectionRequest;
import se.sics.gvod.nat.hp.client.events.OpenConnectionResponse;
import se.sics.gvod.hp.events.OpenConnectionResponseType;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.nat.hp.client.events.OpenConnectionRequest;
import se.sics.gvod.nat.hp.rs.RendezvousServer;
import se.sics.gvod.nat.hp.rs.RendezvousServer.RegisteredClientRecord;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.nat.hp.rs.RendezvousServerInit;
import se.sics.gvod.nat.traversal.events.ConnectionEstablishmentTimeout;
import se.sics.gvod.nat.traversal.events.DeleteOpenConnection;
import se.sics.gvod.nat.traversal.events.StartServices;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.msgs.RelayMsg;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.parentmaker.ParentMaker;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.parentmaker.ParentMakerInit;
import se.sics.gvod.stun.client.StunClient;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.croupier.PeerSamplePort;
import se.sics.gvod.croupier.events.CroupierSample;
import se.sics.gvod.nat.traversal.events.HpFailed;
import se.sics.gvod.stun.client.StunPort;
import se.sics.gvod.stun.client.events.GetNatTypeRequest;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.gvod.stun.client.events.GetNatTypeResponseRuleExpirationTime;
import se.sics.gvod.stun.client.events.StunClientInit;
import se.sics.gvod.stun.server.StunServer;
import se.sics.gvod.stun.server.events.StunServerInit;
import se.sics.kompics.Component;
import se.sics.kompics.Handler;
import se.sics.kompics.Stop;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.common.hp.HPSessionKey;
import se.sics.gvod.common.msgs.DirectMsgNetty;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.config.StunServerConfiguration;
import se.sics.gvod.parentmaker.ParentMakerPort;

/**
 *
 * @author Jim
 */
public class NatTraverser extends MsgRetryComponent {

    private final Logger logger = LoggerFactory.getLogger(NatTraverser.class);
    private Negative<VodNetwork> upperNet = negative(VodNetwork.class);
    private Positive<NatNetworkControl> lowerNetControl = positive(NatNetworkControl.class);
    private Negative<NatTraverserPort> natTraverserPort = negative(NatTraverserPort.class);
    Positive<PeerSamplePort> croupier = positive(PeerSamplePort.class);
    private Component hpClient;
    private Component stunClient;
    private Component parentMaker;
    private Component stunServer; // initialized, if the peer has an open ip
    private Component zServer; // initialized, if the peer has an open ip
    private Self self;
    private String compName; // only for debugging
    // Registered private nodes, shared with RendezvousServer
    private ConcurrentHashMap<Integer, RegisteredClientRecord> registeredClients = new ConcurrentHashMap<Integer, RegisteredClientRecord>();
    // Registered open connections to private nodes, shared with HpClient
    private ConcurrentHashMap<Integer, OpenedConnection> openedConnections = new ConcurrentHashMap<Integer, OpenedConnection>();
    // map to store the messages before the hole in the nat is created. 
    // here the Integer is the id of the destination peer
    // TODO: bound list in size, priority?
    private HashMap<Integer, LinkedList<DirectMsg>> pendingMsgs = new HashMap<Integer, LinkedList<DirectMsg>>();
    // for each destination we have to do hole punching
    // hole punching consists of multiple stages. to store which
    // state the hole punching is going through following map is used
    private HashMap<Integer, NatTraverser.HpProcess> onGoingHP = new HashMap<Integer, NatTraverser.HpProcess>();
    private double scaleStunRTO = 1.5;
    private int stunRTO = 2000;
    private int stunRetries = 0;
    private int maxOpenedConnections = 0;
    private int connectionEstablishmentWaitTime; // maximum time to wait for HP to finish
    private boolean initializedServerComponents = false;
    private NatTraverserConfiguration natTraverserConfig;
    private HpClientConfiguration hpClientConfig;
    private RendezvousServerConfiguration rendezvousServerConfig;
    private StunServerConfiguration stunServerConfiguration;
    private StunClientConfiguration stunClientConfiguration;
    private ParentMakerConfiguration parentMakerConfig;
    private Set<HPSessionKey> receivedRelays = new HashSet<HPSessionKey>();
    // TODO: tell Cosmin, i got a concurrentModificationException, even though this map
    // is only accessed within this component. It is accessed by 3 handlers, one
    // from the timer port, 2 from network port. Is the timer port a problem?
    private ConcurrentHashMap<Long, HPSessionKey> receivedRelaysIndex = new ConcurrentHashMap<Long, HPSessionKey>();
    private Map<Integer, Long> outstandingTimestamps = new HashMap<Integer, Long>();
    private boolean stunTypeDetermined = false;
    private Set<Address> failedStunServers = new HashSet<Address>();
    private List<VodAddress> croupierSamples = new ArrayList<VodAddress>();

    class ServersInitTimeout extends Timeout {

        public ServersInitTimeout(ScheduleTimeout st) {
            super(st);
        }
    }

    private class StunRetryTimeout extends Timeout {
        private final int nodeId;
        public StunRetryTimeout(ScheduleTimeout st, int nodeId) {
            super(st);
            this.nodeId = nodeId;
        }

        public int getNodeId() {
            return nodeId;
        }
        
    }

    private static class HpProcess {

        private TimeoutId timeoutId;
        private int remainingConnectionRetries;
        private VodAddress destAddress;
        private final long hpStartTime;

        public HpProcess(VodAddress destAddress, TimeoutId timeoutId, int remainingConnectionRetries) {
            this.timeoutId = timeoutId;
            this.remainingConnectionRetries = remainingConnectionRetries;
            this.destAddress = destAddress;
            this.hpStartTime = System.currentTimeMillis();
        }

        public long getHpStartTime() {
            return this.hpStartTime;
        }

        public VodAddress getDestAddress() {
            return destAddress;
        }

        public int getRemainingConnectionRetries() {
            return remainingConnectionRetries;
        }

        public TimeoutId getTimeoutId() {
            return timeoutId;
        }

        public void setRemainingConnectionRetries(int remainingConnectionRetries) {
            this.remainingConnectionRetries = remainingConnectionRetries;
        }
    }

    public NatTraverser() {
        this(null);
    }

    public NatTraverser(RetryComponentDelegator delegator) {
        super(delegator);

        hpClient = create(HpClient.class);

        this.delegator.doSubscribe(handleInit, control);
        this.delegator.doSubscribe(handleStop, control);

        this.delegator.doSubscribe(handleDeleteOpenConnection, natTraverserPort);
        this.delegator.doSubscribe(handleStartServices, natTraverserPort);
        this.delegator.doSubscribe(handleConnectionEstablishmentTimeout, timer);
        this.delegator.doSubscribe(handleGarbageCleanupTimeout, timer);

        this.delegator.doSubscribe(handleUpperDirectMsgNettyRequest, upperNet);
        this.delegator.doSubscribe(handleUpperDirectMsgNettyResponse, upperNet);
        this.delegator.doSubscribe(handleUpperDirectMsgNettyOneway, upperNet);

        this.delegator.doSubscribe(handleRelayRequestDown, upperNet);
        this.delegator.doSubscribe(handleRelayOnewayDown, upperNet);
        this.delegator.doSubscribe(handleRelayResponseDown, upperNet);

        this.delegator.doSubscribe(handleLowerDirectMsgRequest, network);
        this.delegator.doSubscribe(handleLowerDirectMsgResponse, network);
        this.delegator.doSubscribe(handleLowerDirectMsgOneway, network);
        this.delegator.doSubscribe(handleRelayRequestUp, network);
        this.delegator.doSubscribe(handleRelayOnewayUp, network);
        this.delegator.doSubscribe(handleRelayResponseUp, network);

        this.delegator.doSubscribe(handleGarbageCleanupTimeout, timer);
        this.delegator.doSubscribe(handleStunRetryTimeout, timer);
        this.delegator.doSubscribe(handleServersInitTimeout, timer);

        this.delegator.doSubscribe(handleCroupierSample, croupier);

        this.delegator.doSubscribe(handleOpenConnectionResponse, hpClient.getPositive(HpClientPort.class));

        this.delegator.doSubscribe(handleRTO, timer);

        this.delegator.doConnect(hpClient.getNegative(Timer.class), timer);
        this.delegator.doConnect(hpClient.getNegative(VodNetwork.class), network, new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));
        this.delegator.doConnect(hpClient.getNegative(NatNetworkControl.class), lowerNetControl);
    }
    Handler<NatTraverserInit> handleInit = new Handler<NatTraverserInit>() {
        @Override
        public void handle(NatTraverserInit init) {
            self = init.getSelf();
            compName = "NT(" + self.getId() + ") ";

            logger.info(compName + " Starting  " + self.getAddress());

            natTraverserConfig = init.getNatTraverserConfig();
            hpClientConfig = init.getHpClientConfig();
            rendezvousServerConfig = init.getRendezvousServerConfig();
            parentMakerConfig = init.getParentMakerConfig();
            stunClientConfiguration = init.getStunClientConfig();
            stunServerConfiguration = init.getStunServerConfig();
            connectionEstablishmentWaitTime = natTraverserConfig.getConnectionEstablishmentWaitTime();
            maxOpenedConnections = natTraverserConfig.getMaxOpenedConnections();
            stunRetries = natTraverserConfig.getStunRetries();

            // initialization is complete start a periodic timer for controlling the
            // number of connections
            if (maxOpenedConnections > 0) {
                SchedulePeriodicTimeout st = new SchedulePeriodicTimeout(VodConfig.NT_GARBAGE_COLLECT_STALE_CONNS_PERIOD, VodConfig.NT_GARBAGE_COLLECT_STALE_CONNS_PERIOD);
                GarbageCleanupTimeout msgTimeout = new GarbageCleanupTimeout(st);
                st.setTimeoutEvent(msgTimeout);
                delegator.doTrigger(st, timer);
            }

            for (Address addr : init.getPublicNodes()) {
                RTTStore.addSample(self.getId(), ToVodAddr.hpServer(addr), 5000);
                RTTStore.addSample(self.getId(), ToVodAddr.stunServer(addr), 5000);
            }

            if (init.isOpenServer()) {
                retryStartServerComponents();
            } else {
                stunClient = create(StunClient.class);

                delegator.doSubscribe(handleGetNatTypeResponse, stunClient.getPositive(StunPort.class));
                delegator.doSubscribe(handleNatTypeResponseRuleTimeout, stunClient.getPositive(StunPort.class));

                connect(stunClient.getNegative(Timer.class), timer);
                connect(stunClient.getNegative(VodNetwork.class), network,
                        new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));
                connect(stunClient.getNegative(NatNetworkControl.class), lowerNetControl);

                delegator.doTrigger(new StunClientInit(self, VodConfig.getSeed(), stunClientConfiguration), stunClient.getControl());

                Set<Address> stunServers = new HashSet<Address>();
                if (!init.getPublicNodes().isEmpty()) {
                    Address a = init.getPublicNodes().iterator().next();
                    stunServers.add(new Address(a.getIp(), BaseCommandLineConfig.DEFAULT_STUN_PORT, a.getId()));
                }

                delegator.doTrigger(new GetNatTypeRequest(stunServers,
                        0 /*timeout before starting stun*/,
                        init.getStunClientConfig().isMeasureNatBindingTimeout(),
                        init.getStunClientConfig().getRto(),
                        init.getStunClientConfig().getRtoRetries(),
                        init.getStunClientConfig().getRtoScale()),
                        stunClient.getPositive(StunPort.class));
            }

            //initialize the hole punching client
            delegator.doTrigger(new HpClientInit(
                    self.clone(VodConfig.SYSTEM_OVERLAY_ID), openedConnections,
                    hpClientConfig), hpClient.getControl());

        }
    };
    Handler<OpenConnectionResponse> handleOpenConnectionResponse = new Handler<OpenConnectionResponse>() {
        @Override
        public void handle(OpenConnectionResponse response) {
            logger.debug(compName + "connection response recvd flag." + response.getResponseType()
                    + " destination id ("
                    + response.getOpenConnectionRequest().getRemoteClientId() + ") - "
                    + response.getMsgTimeoutId());

            int destId = response.getOpenConnectionRequest().getRemoteClientId();

            if (onGoingHP.containsKey(destId)) {
                OpenConnectionResponseType flag = response.getResponseType();
                if (flag.equals(OpenConnectionResponseType.OK)) {
                    holePunchingSuccessful(destId);
                } else if (flag.equals(OpenConnectionResponseType.NAT_COMBINATION_NOT_TRAVERSABLE)
                        || flag.equals(OpenConnectionResponseType.REMOTE_PEER_FAILED)) {
                    // cleanup, set the retry counter to zero
                    NatTraverser.HpProcess session = onGoingHP.get(destId);
                    session.setRemainingConnectionRetries(0);
                    holePunchingFailed(true, destId, flag, response.getHpMechanismUsed(), response.getMsgTimeoutId());

                } else {
                    logger.warn(compName + "cant establish connection with remote client ID (" + destId
                            + ") flag: " + response.getResponseType() + " hp mechanism: " + response.getHpMechanismUsed()
                            + " - " + response.getMsgTimeoutId());
                    holePunchingFailed(true, destId, flag, response.getHpMechanismUsed(), response.getMsgTimeoutId());
                }
            }
        }
    };

    private void sendDownDirectMsg(DirectMsg msg) {
        if (msg.getVodDestination().isOpen()) { 
            // simply send the packet down
            delegator.doTrigger(msg, network);
        } else {
            int remoteId = msg.getDestination().getId();
            if (!sendMsgUsingConnection(msg, remoteId)) {
                // No open connection to dest. Start hole-punching.
                // if hole punching for the destion peer is going
                // on then save the mesage; otherwise start hp
                LinkedList<DirectMsg> msgList;
                if (onGoingHP.containsKey(remoteId)) {
                    msgList = pendingMsgs.get(remoteId);
                } else {
                    // start the hole punching process
                    msgList = new LinkedList<DirectMsg>();
                    pendingMsgs.put(remoteId, msgList);
                    // create a session
                    // TODO - take heartbeat connection (true or false) from the msgs, instead of hard-coding.
                    startHolePunchingProcess(msg.getVodDestination(), false, 0,
                            msg.getTimeoutId());
                }
                msgList.add(msg);
            }
        }
    }
    Handler<DirectMsgNetty.Request> handleUpperDirectMsgNettyRequest = new Handler<DirectMsgNetty.Request>() {
        @Override
        public void handle(DirectMsgNetty.Request msg) {
            logger.trace("{} handleUpperMessageRequest dest ID (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName(), msg.getTimeoutId());
            sendDownDirectMsg(msg);
        }
    };
    Handler<DirectMsgNetty.Response> handleUpperDirectMsgNettyResponse = new Handler<DirectMsgNetty.Response>() {
        @Override
        public void handle(DirectMsgNetty.Response msg) {
            logger.trace("{} handleUpperMessageResponse dest ID (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName(), msg.getTimeoutId());
            sendDownDirectMsg(msg);
        }
    };
    Handler<DirectMsgNetty.Oneway> handleUpperDirectMsgNettyOneway = new Handler<DirectMsgNetty.Oneway>() {
        @Override
        public void handle(DirectMsgNetty.Oneway msg) {
            logger.trace("{} handleUpperMessageOneway dest ID (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName(), msg.getTimeoutId());
            sendDownDirectMsg(msg);
        }
    };
    Handler<DeleteOpenConnection> handleDeleteOpenConnection = new Handler<DeleteOpenConnection>() {
        @Override
        public void handle(DeleteOpenConnection event) {
            openedConnections.remove(event.getRemoteId());
        }
    };
    Handler<StartServices> handleStartServices = new Handler<StartServices>() {
        @Override
        public void handle(StartServices event) {
            initializeServerComponents(event.getNodes());
        }
    };
    Handler<DirectMsgNetty.Request> handleLowerDirectMsgRequest = new Handler<DirectMsgNetty.Request>() {
        @Override
        public void handle(DirectMsgNetty.Request msg) {
            logger.trace("handleLowerMessage dest ID (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName());
            delegator.doTrigger(msg, upperNet);
        }
    };
    Handler<DirectMsgNetty.Response> handleLowerDirectMsgResponse = new Handler<DirectMsgNetty.Response>() {
        @Override
        public void handle(DirectMsgNetty.Response msg) {
            logger.trace("handleLowerMessage dest ID (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName());
            delegator.doTrigger(msg, upperNet);
        }
    };
    Handler<DirectMsgNetty.Oneway> handleLowerDirectMsgOneway = new Handler<DirectMsgNetty.Oneway>() {
        @Override
        public void handle(DirectMsgNetty.Oneway msg) {
            logger.trace("handleLowerMessage dest ID (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName());
            delegator.doTrigger(msg, upperNet);
        }
    };
    Handler<ConnectionEstablishmentTimeout> handleConnectionEstablishmentTimeout = new Handler<ConnectionEstablishmentTimeout>() {
        @Override
        public void handle(ConnectionEstablishmentTimeout timeout) {
            logger.debug(compName + "Connection establishment timeout. Remote client ID is (" + timeout.getDestAddress().getId() + ")");
            int destId = timeout.getDestAddress().getId();
            holePunchingFailed(false, destId, OpenConnectionResponseType.LOOKUP_FAILED,
                    null, timeout.getMsgTimeoutId());
            //   why lookup failed response type?
            // if lookup fails then i do noting and wait for this timeout to expire coz
            // this wait will give OPMP some time to stablelize. when this timer
            // expires HP will be repeated and hopefully lookup will succeede.
        }
    };

    private void retryStartServerComponents() {
        ScheduleTimeout st = new ScheduleTimeout(VodConfig.NT_SERVER_INIT_RETRY_PERIOD);
        ServersInitTimeout t = new ServersInitTimeout(st);
        st.setTimeoutEvent(t);
        delegator.doTrigger(st, timer);
    }
    Handler<ServersInitTimeout> handleServersInitTimeout = new Handler<ServersInitTimeout>() {
        @Override
        public void handle(ServersInitTimeout timeout) {
            List<RTT> rtts = RTTStore.getOnAvgBest(self.getId(), 5);

            // I need to have a reference to another public node to
            // start the stunserver, if i don't have one, start a timeout
            // and wait until i get one and call this code again.
            // I can get a public node sample using either RTTStore or
            // Croupier
            List<VodAddress> nodes = new ArrayList<VodAddress>();
            for (RTT rtt : rtts) {
                nodes.add(rtt.getAddress());
            }

            if (nodes.isEmpty()) {
                for (VodAddress va : croupierSamples) {
                    if (va.isOpen()) {
                        nodes.add(va);
                    }
                }
            }

            if (!initializeServerComponents(nodes)) {
                retryStartServerComponents();
            }
        }
    };
    Handler<RelayMsgNetty.Request> handleRelayRequestDown = new Handler<RelayMsgNetty.Request>() {
        @Override
        public void handle(RelayMsgNetty.Request msg) {
            logger.debug("{} handleRelayRequestDown (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName(), msg.getTimeoutId());

            if (msg.getVodDestination().isOpen()) {
                delegator.doTrigger(msg, network);
                TimeoutId id = msg.getTimeoutId();
                if (id.isSupported()) {
                    outstandingTimestamps.put(msg.getTimeoutId().getId(), System.currentTimeMillis());
                }
            } else {
                if (!sendMsgUsingConnection(msg, msg.getRemoteId())) {
                    // No open connection to dest. 
                    // Send by relaying msg via all parents of destination.
                    Set<Address> parents = new HashSet<Address>(msg.getVodDestination().getParents());
                    if (!parents.isEmpty()) {
                        for (Address p : parents) {
                            if (p.getId() != self.getId()) {
                                msg.rewriteDestination(p);
                                delegator.doTrigger(msg, network);
                            }
                        }

                        if (msg.getTimeoutId().isSupported()) {
                            outstandingTimestamps.put(msg.getTimeoutId().getId(),
                                    System.currentTimeMillis());
                        }
                    } else {
                        logger.debug(compName + msg.getClass() + " No parents. No relay msg sent from {} to relay {} ", msg.getVodSource(), msg.getVodDestination());
                        // TODO send a FAULT msg, indicating that the dest has no parents to relay to.
                    }
                } else {
                    logger.debug(compName + msg.getClass() + " relay msg sent from {} directly using"
                            + "open connection to {} ", msg.getVodSource(), msg.getVodDestination());
                }
            }
        }
    };
    Handler<RelayMsgNetty.Request> handleRelayRequestUp = new Handler<RelayMsgNetty.Request>() {
        @Override
        public void handle(RelayMsgNetty.Request msg) {
            logger.debug("{} handleRelayRequestUp (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName(), msg.getTimeoutId());

            // If the destination is open, then it is the final destination
            if (self.getId() != msg.getRemoteId()) { // I am a parent
                logger.debug("Relaying msg from {} to {}", self.getId(), msg.getRemoteId());
                relayMsg(msg);
            } else { // I am the destination
                int timeoutId = 0;
                if (msg.getTimeoutId().isSupported()) {
                    timeoutId = msg.getTimeoutId().getId();
                }

                HPSessionKey sk = new HPSessionKey(msg.getClientId(), timeoutId);
                if (!receivedRelays.contains(sk)) {
                    // only discard duplicates if TimeoutId is supported
                    if (msg.getTimeoutId().isSupported()) {
                        receivedRelays.add(sk);
                        receivedRelaysIndex.put(System.currentTimeMillis(), sk);
                    }
                    delegator.doTrigger(msg, upperNet);
                } else {
                    // silently discard duplicates as many requests will arrive via multiple relay servers
                    logger.debug(compName + msg.getClass()
                            + " relay msg discarded at {} from {} "
                            + " with timestamp " + msg.getTimeoutId(),
                            msg.getVodDestination().getId(), msg.getVodSource().getId());
                }
            }
        }
    };
    Handler<RelayMsgNetty.Response> handleRelayResponseUp = new Handler<RelayMsgNetty.Response>() {
        @Override
        public void handle(RelayMsgNetty.Response msg) {
            logger.debug("{} handleRelayResponseUp (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName(), msg.getTimeoutId());

            if (msg.getClientId() != self.getId()) {
                relayMsg(msg);
            } else {
                if (msg.getTimeoutId().isSupported()) {
                    Long startTime = outstandingTimestamps.get(msg.getTimeoutId().getId());
                    if (startTime != null) {
                        long rtt = System.currentTimeMillis() - startTime;
                        msg.setRtt(rtt);
                        RTTStore.addSample(msg.getDestination().getId(), msg.getVodSource(), rtt);
                        outstandingTimestamps.remove(msg.getTimeoutId().getId());
                    } else {
                        logger.debug(compName + msg.getClass() + " Couldn't find startTime for node {} with timer {} - " + self.getNat(),
                                self.getId(), msg.getTimeoutId());
                        StringBuilder sb = new StringBuilder();
                        for (Integer t : outstandingTimestamps.keySet()) {
                            sb.append(t).append(", ");
                        }

                        logger.debug(compName + " Existing Timestamps: " + sb.toString());
                    }
                }

                delegator.doTrigger(msg, upperNet);
            }
        }
    };
    Handler<RelayMsgNetty.Response> handleRelayResponseDown = new Handler<RelayMsgNetty.Response>() {
        @Override
        public void handle(RelayMsgNetty.Response msg) {
            logger.debug("{} ClientId({}) handleRelayResponseDown (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName(),
                    msg.getTimeoutId(), msg.getClientId());

            // Send the response to the parent that I received the message from.
            // It should have an open NAT connection to the client.
            if (!self.isOpen() && msg.getNextDest().isOpen()) {
                msg.rewriteDestination(msg.getNextDest().getPeerAddress());
            }

            delegator.doTrigger(msg, network);
        }
    };

    // Used by both Relay.RequestMsg and Relay.ResponseMsg
    // Do *not* use clientId and remoteId here.
    private void relayMsg(RelayMsg.Base msg) {
        // relayMsg is used both for requests and responses, therefore we have to 
        // use NextDest (not clientId or remoteId) to identify the node to where
        // the msg is relayed. 
        int nextDestId;

        if (msg instanceof RelayMsgNetty.Oneway) {
            nextDestId = msg.getRemoteId();
        } else if (msg instanceof RelayMsgNetty.Request) {
            nextDestId = msg.getRemoteId();
        } else {
            //if (msg instanceof RelayMsgNetty.Response) {
            nextDestId = msg.getClientId();
        }

        if (registeredClients.containsKey(nextDestId)) {
            logger.debug(compName + " Relaying msg. remoteId {} clientId {} . TimeoutId="
                    + msg.getTimeoutId()
                    + " Dest " + msg.getVodDestination().getId()
                    + " - Type= " + msg.getClass().toString(),
                    msg.getRemoteId(), msg.getClientId());

            // Add the client as a temporary registration record, so that responses
            // can be sent to the client
            if (!registeredClients.containsKey(msg.getSource().getId())) {
                RegisteredClientRecord tempClient = new RegisteredClientRecord(msg.getVodSource(),
                        System.currentTimeMillis(), Nat.DEFAULT_RULE_EXPIRATION_TIME, 0, 5000, null, true);
                registeredClients.put(msg.getSource().getId(), tempClient);
            } else {
                RegisteredClientRecord client = registeredClients.get(msg.getSource().getId());
                client.setLastHeardFrom(System.currentTimeMillis());
            }

            RegisteredClientRecord rcr = registeredClients.get(nextDestId);
            VodAddress nattedDest = rcr.getClient();
            VodAddress finalDest = new VodAddress(nattedDest.getPeerAddress(),
                    nattedDest.getOverlayId(), nattedDest.getNat());
            // forward the message, as it's from my child
            msg.rewriteDestinationAtRelay(self.getAddress(), finalDest);
            delegator.doTrigger(msg, network);

        } else {
            logger.warn(compName + " Not relaying msg. {} not a parent for {} . Self "
                    + self.getAddress()
                    + ", clientId: " + msg.getClientId()
                    + ", remoteId: " + msg.getRemoteId()
                    + ", children: "
                    + registeredClients.size()
                    + ", timeoutId: "
                    + msg.getTimeoutId()
                    + " " + msg.getClass().toString(), self.getId(), nextDestId);
        }
    }
    Handler<RelayMsgNetty.Oneway> handleRelayOnewayUp = new Handler<RelayMsgNetty.Oneway>() {
        @Override
        public void handle(RelayMsgNetty.Oneway msg) {
            logger.debug("handleOneWayUp (" + msg.getDestination().getId() + ") message class :" + msg.getClass().getName());

            if (msg.getRemoteId() != self.getId()) {
                relayMsg(msg);
            } else {
                // Ignore duplicates
                HPSessionKey sk = new HPSessionKey(msg.getClientId(), msg.getTimeoutId().getId());
                if (!receivedRelays.contains(sk)) {
                    receivedRelays.add(sk);
                    receivedRelaysIndex.put(System.currentTimeMillis(), sk);
                    delegator.doTrigger(msg, upperNet);
                } else {
                    // silently discard duplicates                
                }
            }
        }
    };
    Handler<RelayMsgNetty.Oneway> handleRelayOnewayDown = new Handler<RelayMsgNetty.Oneway>() {
        @Override
        public void handle(RelayMsgNetty.Oneway msg) {
            logger.debug("handleOneWayUp (" + msg.getDestination().getId()
                    + ") message class :" + msg.getClass().getName());
            delegator.doMulticast(msg, msg.getVodDestination().getParents(), 1000, 0);
        }
    };

    private Set<Integer> oldestConnections(int number/**
             * number of old connections
             */
            ) {
        List<Integer> keysList = new ArrayList<Integer>(openedConnections.keySet());
        List<OpenedConnection> valuesList = new ArrayList<OpenedConnection>(openedConnections.values());
        TreeSet<OpenedConnection> sortedSet = new TreeSet<OpenedConnection>(new NatTraverser.ConnectionTSComparator());


        for (int i = 0; i < valuesList.size(); i++) {
            sortedSet.add(valuesList.get(i));
        }

        Object[] sortedArray = sortedSet.toArray();

        Set<Integer> oldConnections = new HashSet<Integer>();
        String str = ""; // only for debug msgs

        for (int i = 0; i < number; i++) {
            Integer oldConnectionKey = keysList.get(valuesList.indexOf(sortedArray[i]));
            oldConnections.add(oldConnectionKey);
            str += "{ " + oldConnectionKey.toString() + " - " + ((OpenedConnection) sortedArray[i]).getLastUsed() + " }";
        }
        logger.trace(compName + " To be deleted " + str);
        return oldConnections;
    }

    class ConnectionTSComparator implements Comparator<Object> {

        @Override
        public int compare(Object obj1, Object obj2) {
            OpenedConnection c1 = (OpenedConnection) obj1;
            OpenedConnection c2 = (OpenedConnection) obj2;
            int ret = 1;

            if (c1.getLastUsed() == c2.getLastUsed()) {
                ret = 0;
            }

            if (c1.getLastUsed() < c2.getLastUsed()) {
                ret = -1;
            }

            return ret;
        }
    }

    public void sendOpenConnectionRequest(VodAddress destAddress, boolean keepConnectionOpenWithHeartbeat, boolean skipPacing, TimeoutId id) {
        OpenConnectionRequest request = new OpenConnectionRequest(destAddress, keepConnectionOpenWithHeartbeat, skipPacing, id);
        delegator.doTrigger(request, hpClient.getPositive(HpClientPort.class));
    }

    private boolean initializeServerComponents(List<VodAddress> nodes) {
        if (nodes.isEmpty() || initializedServerComponents == true) {
            return false;
        }

        stunServer = create(StunServer.class);
        zServer = create(RendezvousServer.class);

        connect(zServer.getNegative(Timer.class), timer);
        connect(zServer.getNegative(VodNetwork.class), network, new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));
        delegator.doTrigger(new RendezvousServerInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID), registeredClients, rendezvousServerConfig), zServer.getControl());

        connect(stunServer.getNegative(Timer.class), timer);
        connect(stunServer.getNegative(VodNetwork.class), network, new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));
        connect(stunServer.getNegative(NatNetworkControl.class), lowerNetControl);

        List<VodAddress> partners = new ArrayList<VodAddress>();
        for (VodAddress a : nodes) {
            partners.add(ToVodAddr.stunServer(a.getPeerAddress()));
        }

        StunServerInit stunServerInit = new StunServerInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID), partners, stunServerConfiguration);
        delegator.doTrigger(stunServerInit, stunServer.getControl());
        initializedServerComponents = true;

        return true;
    }

    /**
     * 
     * @param msg
     * @param remoteId This may be either the destination node Id OR the id of the Relay msg destination (not next hop's id).
     * @return 
     */
    public boolean sendMsgUsingConnection(RewriteableMsg msg, int remoteId) {
        if (openedConnections.containsKey(remoteId)) {
            // if I already have an open hole-punched connection to remotedId node.
            OpenedConnection connection = openedConnections.get(remoteId);
            // update connection timestamp
            connection.setLastUsed(System.currentTimeMillis());

            // change the src and dest addresses and send the message of the network
            int portInUse = connection.getPortInUse();
            Address newSourceAddress = new Address(self.getIp(), portInUse, self.getId());

            // now change the source and destination addresses
            msg.rewritePublicSource(newSourceAddress);
            msg.rewriteDestination(connection.getHoleOpened());

            logger.debug("Sending {} from {} to " + msg.getDestination(), msg.getClass(), msg.getSource());
            //send the message on the network
            delegator.doTrigger(msg, network);

            return true;
        } else if (registeredClients.containsKey(remoteId)) {
            // if I am the Rendezvous server for this client...
            RegisteredClientRecord record = registeredClients.get(remoteId);
            VodAddress actualClientAddr = record.getClient();
            msg.rewriteDestination(actualClientAddr.getPeerAddress());
            //send the message on the network
            delegator.doTrigger(msg, network);

            return true;
        }

        return false;
    }

    public void startHolePunchingProcess(VodAddress destAddress, boolean keepConnectionOpenWithHeartbeat, int connRetries, TimeoutId msgTimeoutId) {
        logger.debug(compName + "Starting hole punching for [" + self.getId() + ", " + destAddress.getId() + "]");
        // starting timer
        ScheduleTimeout st = new ScheduleTimeout(connectionEstablishmentWaitTime);
        ConnectionEstablishmentTimeout timeoutEvent = new ConnectionEstablishmentTimeout(st, destAddress, msgTimeoutId);
        st.setTimeoutEvent(timeoutEvent);

        NatTraverser.HpProcess hpProcess = new NatTraverser.HpProcess(destAddress, timeoutEvent.getTimeoutId(), --connRetries);

        // save the session - if the session already exists then overwrite it
        onGoingHP.put(destAddress.getId(), hpProcess);
        // send hp request
        sendOpenConnectionRequest(destAddress, keepConnectionOpenWithHeartbeat, self.isPacingReqd(), msgTimeoutId);
        delegator.doTrigger(st, timer);
    }

    public void holePunchingSuccessful(int destId) {
        NatTraverser.HpProcess session = onGoingHP.get(destId);

        //first cancel timer
        CancelTimeout ct = new CancelTimeout(session.getTimeoutId());
        delegator.doTrigger(ct, timer);

        // send all pending messages
        logger.trace(compName + " HP for " + destId + " took " + ((System.currentTimeMillis() - session.getHpStartTime()) / 1000) + " secs");

        List<DirectMsg> msgs = pendingMsgs.get(destId);
        Iterator<DirectMsg> itr = msgs.iterator();
        while (itr.hasNext()) {
            DirectMsg msg = itr.next();
            sendMsgUsingConnection(msg, destId);
        }

        onGoingHP.remove(destId);
    }

    public void holePunchingFailed(boolean cancelTimer, int destId, OpenConnectionResponseType flag, HPMechanism hpMechanism, TimeoutId msgTimeoutId) {
        NatTraverser.HpProcess session = onGoingHP.get(destId);
        onGoingHP.remove(destId);

        if (cancelTimer) {
            CancelTimeout ct = new CancelTimeout(session.getTimeoutId());
            delegator.doTrigger(ct, timer);
        }

        int remaniningRetries = session.getRemainingConnectionRetries();
        VodAddress destAddress = session.getDestAddress();

        if (remaniningRetries > 0) {
            logger.trace(compName + "retrying HP for (" + destId + "). Failure reason: " + flag + " hp mechanism: " + hpMechanism);
            startHolePunchingProcess(destAddress, true, remaniningRetries, msgTimeoutId);
        } else {
            // hp failed. discard all messages
            pendingMsgs.remove(destId);
            // TODO - can i use reflection to create a response msg, if we follow the 
            // request/response idiom?
            trigger(new HpFailed(msgTimeoutId, flag, destAddress), natTraverserPort);
        }
    }

    void printAllOpenedConnections() {
        String str = compName + " All " + openedConnections.size() + " opened Connections are: ";
        for (Integer key : openedConnections.keySet()) {
            str += key.toString() + " ";
        }

        logger.debug(str);
    }
    Handler<GarbageCleanupTimeout> handleGarbageCleanupTimeout = new Handler<GarbageCleanupTimeout>() {
        @Override
        public void handle(GarbageCleanupTimeout timeout) {
            // logger.debug(compName + "Connection Cleanup Timeout");
            if (openedConnections.size() > maxOpenedConnections) {
                //TODO: Jim. Do you mean by oldestthe last connection to have been used?
                // remove the oldest connections
                int connectionsToRemove = openedConnections.size() - maxOpenedConnections;
                logger.trace(compName + " connection queue has " + openedConnections.size()
                        + " opned connections. " + maxOpenedConnections + " are allowed "
                        + "removing " + connectionsToRemove);

                Set<Integer> oldConnections = oldestConnections(connectionsToRemove);
                for (Integer connectionKey : oldConnections) {
                    if (connectionKey != null) {
                        // connections are created and maintained by the
                        // hole punching client
                        // send message to the hole punching client to
                        // remove the connection

                        logger.trace(compName + " deleting the connection " + connectionKey);
                        DeleteConnectionRequest request =
                                new DeleteConnectionRequest(connectionKey);
                        delegator.doTrigger(request, hpClient.getPositive(HpClientPort.class));
                    }
                }
            }

            // Clean up old references to relay msgs, that are stored for duplicate checking
            long t = System.currentTimeMillis();
            HashSet<Long> keysToBeRemoved = new HashSet<Long>();

            for (Long l : receivedRelaysIndex.keySet()) {
                if (t - l > VodConfig.NT_STALE_RELAY_MSG_TIME) {
                    keysToBeRemoved.add(l);
                }
            }

            for (Long l : keysToBeRemoved) {
                HPSessionKey sk = receivedRelaysIndex.remove(l);
                receivedRelays.remove(sk);
            }

            // Clean up any old timestamps for relay msgs where the response wasn't received
            HashSet<Integer> tsToBeRemoved = new HashSet<Integer>();
            for (Integer i : outstandingTimestamps.keySet()) {
                Long l = outstandingTimestamps.get(i);
                if (t - l > VodConfig.NT_STALE_RELAY_MSG_TIME) {
                    tsToBeRemoved.add(i);
                }
            }
            for (Integer i : tsToBeRemoved) {
                outstandingTimestamps.remove(i);
            }
        }
    };

    @Override
    public void stop(Stop event) {
        delegator.doTrigger(new Stop(), hpClient.getControl());
        delegator.doTrigger(new Stop(), parentMaker.getControl());
        if (initializedServerComponents) {
            delegator.doTrigger(new Stop(), stunServer.getControl());
            delegator.doTrigger(new Stop(), zServer.getControl());
        }

    }
    private Handler<GetNatTypeResponse> handleGetNatTypeResponse = new Handler<GetNatTypeResponse>() {
        @Override
        public void handle(GetNatTypeResponse event) {
            logger.info(compName + " Nat type is " + event.getStatus() + " - " + event.getNat());
            self.setNat(event.getNat());
            if (event.getStatus() == GetNatTypeResponse.Status.SUCCEED) {
                stunTypeDetermined = true;
                failedStunServers.clear();
                // Only start RendezvousServer if we can run it on the default port
                if (event.getNat().isOpen()) {
                    // start the server components, when we have some partner for the stun server
                    retryStartServerComponents();
                } else if (event.getNat().isUpnp()) {
                    logger.info("UPnP is supported.");
                    if (parentMaker != null) {
                        trigger(new Stop(), parentMaker.getControl());
                    }
                } else { // behind a NAT
                    parentMaker = create(ParentMaker.class);
                    // TODO - do i need a filter for timer msgs too?
                    connect(parentMaker.getNegative(Timer.class), timer);
                    connect(parentMaker.getNegative(VodNetwork.class), network, new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));
                    connect(parentMaker.getNegative(NatNetworkControl.class), lowerNetControl);
                    delegator.doTrigger(
                            new ParentMakerInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID),
                            parentMakerConfig), parentMaker.control());
                    List<VodAddress> bootstrappers = new ArrayList<VodAddress>();
                    bootstrappers.add(ToVodAddr.hpServer(event.getStunServer()));
                    delegator.doTrigger(new Join(bootstrappers),
                            parentMaker.getPositive(ParentMakerPort.class));
                }
            } else {
                retryStun(event.getStunServer());
            }

            delegator.doTrigger(event, natTraverserPort);
        }
    };

    private void retryStun(Address failedStunServer) {
        if (stunRetries > 0) {
            logger.warn(compName + "Could not determine Nat type. Retrying. Temporariliy setting it to: " + self.getNat() + "\n");
            ScheduleTimeout st = new ScheduleTimeout(stunRTO);
            StunRetryTimeout srt = new StunRetryTimeout(st, self.getId());
            st.setTimeoutEvent(srt);
            trigger(st, timer);
            stunRTO *= scaleStunRTO;
            stunRetries--;
            if (failedStunServer != null) {
                failedStunServers.add(failedStunServer);
            }
        } else {
            System.err.println("Could not run stun protocol. ");
            logger.error(compName + " could not run stun protocol. ");
        }
    }
    Handler<StunRetryTimeout> handleStunRetryTimeout = new Handler<StunRetryTimeout>() {
        @Override
        public void handle(StunRetryTimeout timeout) {
            if (!stunTypeDetermined) {
                Set<Address> stunServers = new HashSet<Address>();
                List<RTT> rtts = RTTStore.getOnAvgBest(self.getId(), 1, failedStunServers);
                if (rtts.isEmpty()) { // couldn't find any new samples
                    retryStun(null);
                } else {
                    stunServers.add(rtts.get(0).getAddress().getPeerAddress());
                    delegator.doTrigger(new GetNatTypeRequest(stunServers,
                            0 /*timeout before starting stun*/,
                            stunClientConfiguration.isMeasureNatBindingTimeout(),
                            stunClientConfiguration.getRto(),
                            stunClientConfiguration.getRtoRetries(),
                            stunClientConfiguration.getRtoScale()),
                            stunClient.getPositive(StunPort.class));
                }
            }
        }
    };
    public Handler<GetNatTypeResponseRuleExpirationTime> handleNatTypeResponseRuleTimeout = new Handler<GetNatTypeResponseRuleExpirationTime>() {
        @Override
        public void handle(GetNatTypeResponseRuleExpirationTime event) {
            if (self.getNat() != null) {
                self.getNat().setBindingTimeout(event.getRuleLifeTime());
            } else {
                logger.warn("Trying to set binding time on a Null Nat!");
            }
        }
    };
    Handler<CroupierSample> handleCroupierSample = new Handler<CroupierSample>() {
        @Override
        public void handle(CroupierSample event) {
            logger.trace("Received {} samples from Croupier.", event.getNodes().size());
            // add samples to list that can be used to bootstrap STUN..
            croupierSamples.clear();
            StringBuilder sb = new StringBuilder();
            sb.append(compName).append("Received samples from Croupier: ");
            for (VodDescriptor vd : event.getNodes()) {
                croupierSamples.add(vd.getVodAddress());
                if (!vd.getVodAddress().isOpen()) {
                    sb.append(vd.getVodAddress()).append(" parents: \n");
                    for (Address a : vd.getVodAddress().getParents()) {
                        sb.append(a).append(" - ");
                    }
                    sb.append("\n");
                }
            }

            logger.trace(sb.toString());
        }
    };
}
