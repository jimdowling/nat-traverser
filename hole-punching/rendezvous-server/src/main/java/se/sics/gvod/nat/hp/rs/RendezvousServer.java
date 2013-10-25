/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.hp.rs;

import se.sics.gvod.timer.UUID;
import se.sics.gvod.common.hp.HolePunching;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HpFeasability;
import se.sics.gvod.common.hp.HPSessionKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.RTTStore;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.evts.GarbageCleanupTimeout;
import se.sics.gvod.common.util.PortSelector;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.hp.events.OpenConnectionResponseType;
import se.sics.gvod.hp.msgs.*;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.nat.hp.rs.events.UnregisterTimeout;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.timer.*;
import se.sics.kompics.Handler;
import se.sics.kompics.Stop;

/**
 * overlayId == GVodConfig.RENDEZVOUS_SERVER_ID
 *
 * @author Salman, Jim In the comments Rendezvous server is referred as z/Z
 * server or just z/Z
 */
// TODO delete sessions
public class RendezvousServer extends MsgRetryComponent {

    private Logger logger = LoggerFactory.getLogger(RendezvousServer.class);
    private final static int NUM_RETRIES_CONNECTION_REVERSAL = 1;
    private final static int NUM_RETRIES_SHP = 1;
    private final static int NUM_RETRIES_PRP = 1;
    private final static int NUM_RETRIES_PRP_PRP = 1;
    private final static int NUM_RETRIES_PRC = 1;
    private final static int NUM_RETRIES_PRC_PRC = 1;
    private final static int CLEANUP_TIMEOUT = 30 * 1000;
    private final static int THRESHOLD_SWAP_PARENT = 50;
    ConcurrentHashMap<Integer, RegisteredClientRecord> registeredClients;
    private HashMap<HPSessionKey, HolePunching> hpSessions = new HashMap<HPSessionKey, HolePunching>();
    /**
     * <identifier of nat, ports>
     */
//    private HashMap<InetAddress, Set<Integer>> allocatedPorts =
//            new HashMap<InetAddress, Set<Integer>>();
    private Self self;   // z server address
    int dummyPort = 49997; // used to create dummy public addresses. used in prc and  prp etc
    String compName;
    int sessionExpirationTime;
    boolean throwException = false;
    private TimeoutId garbageCleanupTimeoutId;
    RendezvousServerConfiguration config;

    public static class NoPortsException extends Exception {

        public NoPortsException(String message) {
            super(message);
        }
    }

    public static class RegisteredClientRecord {

        // i.e. the address of the nat and the port opened on the nat.
        VodAddress client;
        // registration expires if you dont recv the ping message from the client
        // timestamp is when the last ping was recvd
        long lastHeardFromTimestamp;
        // when the registration expires. expiration time is directly dependent on
        // nat rule timeout value, which is different for different nats. This variable
        // is set to ruleexpiration timeout value recvd from the nat
        long timeBeforeExpiration;
        // for port-contiguity, port prediction
        final int delta;
        // used when a new RegisterClient request is received, whether to accept the
        // new child and remove and existing child. 
        long rtt;
        // used for clients who don't call HpRegisterClient, but try to connect to
        // a client of this zServer
        final boolean tempRecord;
        // 
        boolean removing = false;
        private final Set<Integer> prpPorts;

        public RegisteredClientRecord(VodAddress clientPublicAddress,
                long rtt, Set<Integer> prpPorts, boolean tempRecord) {
            if (clientPublicAddress == null) {
                throw new NullPointerException("Registered client address cannot be null");
            }
            this.client = clientPublicAddress;
            this.lastHeardFromTimestamp = System.currentTimeMillis();
            this.timeBeforeExpiration = clientPublicAddress.getNatBindingTimeout();
            this.delta = clientPublicAddress.getNat().getDelta();
            this.rtt = rtt;
            this.prpPorts = (prpPorts == null) ? new HashSet<Integer>() : prpPorts;
            this.tempRecord = tempRecord;
        }

        public boolean isTempRecord() {
            return tempRecord;
        }

        public boolean isRemoving() {
            return removing;
        }

        public void addPrpPorts(Set<Integer> newPorts) {
            if (newPorts != null) {
                prpPorts.addAll(newPorts);
            }
        }

        public Integer popPrpPort() throws NoPortsException {
            Integer port = null;
            if (prpPorts.isEmpty()) {
                throw new NoPortsException("No ports for PRP");
            }
            port = prpPorts.iterator().next();
            prpPorts.remove(port);
            return port;
        }

        public void pushPrpPort(int port) {
            prpPorts.add(port);
        }

        public int sizePrpPorts() {
            return prpPorts.size();
        }

        public void setRemoving(boolean removing) {
            this.removing = removing;
        }

        public long getRtt() {
            return rtt;
        }

        public int getDelta() {
            return delta;
        }

        public void setLastHeardFrom(long timeStamp) {
            this.lastHeardFromTimestamp = timeStamp;
        }

        public VodAddress getClient() {
            return client;
        }

        public Set<Integer> getPrpPorts() {
            return prpPorts;
        }

        public void setClient(VodAddress client) {
            if (client == null) {
                throw new NullPointerException("Null client.");
            }
            this.client = client;
        }

        public long getExpirationTime() {
            return timeBeforeExpiration;
        }

        public void setExpirationTime(long expirationTime) {
            this.timeBeforeExpiration = expirationTime;
        }

        public long getLastHeardFrom() {
            return lastHeardFromTimestamp;
        }

        public int getNumParents() {
            return (client == null) ? 0 : client.getParents().size();
        }

        @Override
        public String toString() {
            return " Pub Addr: " + client
                    + " Exp Time:" + timeBeforeExpiration
                    + " TS: " + lastHeardFromTimestamp
                    + " Delta: " + delta
                    + " rtt: " + rtt;
        }
    }

    public RendezvousServer() {
        this(null);
    }

    public RendezvousServer(RetryComponentDelegator delegator) {
        super(delegator);
        this.delegator.doAutoSubscribe();
    }
    Handler<RendezvousServerInit> handleInit = new Handler<RendezvousServerInit>() {
        @Override
        public void handle(RendezvousServerInit init) {
            self = init.getSelf();
            registeredClients = init.getRegisteredClients();

            logger.info("Starting RendezvousServer with support for {} children", 
                    init.getConfig().getNumChildren());
            config = init.getConfig();
            compName = "(" + self.getId() + ") ";

            // session epiration time
            sessionExpirationTime = init.getConfig().getSessionExpirationTime();
            // initialize grabage collection
            SchedulePeriodicTimeout st = new SchedulePeriodicTimeout(CLEANUP_TIMEOUT, CLEANUP_TIMEOUT);
            GarbageCleanupTimeout msgTimeout = new GarbageCleanupTimeout(st);
            st.setTimeoutEvent(msgTimeout);
            garbageCleanupTimeoutId = st.getTimeoutEvent().getTimeoutId();
            delegator.doTrigger(st, timer);
        }
    };

    private void sendRegisterResponse(VodAddress dest, TimeoutId timeoutId,
            HpRegisterMsg.RegisterStatus res, Set<Integer> prpPorts) {

        logger.debug(compName + res + " sent to " + dest);
        delegator.doTrigger(new HpRegisterMsg.Response(self.getAddress(), dest,
                res, timeoutId, prpPorts), network);
    }

    private VodAddress getTheWorstChild() {
        long childRtt;
        long worstRtt = -1;
        VodAddress worstChild = null;

        for (RegisteredClientRecord r : registeredClients.values()) {
            childRtt = r.getRtt() * r.getNumParents();
            if (childRtt > worstRtt && r.isTempRecord() == false) {
                worstRtt = childRtt;
                worstChild = r.getClient();
            }
        }
        return worstChild;
    }
    Handler<HpRegisterMsg.Request> handleHpRegisterRequest = new Handler<HpRegisterMsg.Request>() {
        @Override
        public void handle(HpRegisterMsg.Request request) {
            logger.debug(compName + "Recvd Register Request from Client:" + request.getVodSource());
            VodAddress peer = request.getVodSource();
            long rtt = request.getRtt();
            TimeoutId timeoutId = request.getTimeoutId();
            int currentSize = registeredClients.size();
            for (RegisteredClientRecord r : registeredClients.values()) {
                if (r.isTempRecord()) {
                    currentSize--;
                }
            }

            if (currentSize < config.getNumChildren()) {
                RTTStore.addSample(self.getId(), peer, rtt);
                boolean alreadyRegistered = registerClientRecord(peer, 
                        request.getSource().getId(), rtt,
                        request.getPrpPorts(), false);
                if (alreadyRegistered) {
                    sendRegisterResponse(peer, timeoutId, HpRegisterMsg.RegisterStatus.ALREADY_REGISTERED,
                            request.getPrpPorts());
                } else {
                    sendRegisterResponse(peer, timeoutId, HpRegisterMsg.RegisterStatus.ACCEPT,
                            request.getPrpPorts());
                }
            } else {
                VodAddress worstChild = getTheWorstChild();
                if (worstChild != null) {
                    long worstChildRtt = registeredClients.get(worstChild.getId()).getRtt()
                            * worstChild.getParents().size();

                    rtt *= request.getVodSource().getParents().size();
                    if (rtt + THRESHOLD_SWAP_PARENT < worstChildRtt) {
                        registeredClients.remove(worstChild.getId());
                        delegator.doRetry(new HpUnregisterMsg.Request(self.getAddress(),
                                worstChild, 0, HpRegisterMsg.RegisterStatus.BETTER_CHILD));
                        
                        registerClientRecord(peer, request.getSource().getId(), rtt,
                                request.getPrpPorts(), false);
                        sendRegisterResponse(peer, timeoutId, HpRegisterMsg.RegisterStatus.ACCEPT,
                                request.getPrpPorts());
                    } else {
                        sendRegisterResponse(peer, timeoutId, HpRegisterMsg.RegisterStatus.REJECT,
                                request.getPrpPorts());
                    }
                } else {
                    logger.warn(compName + " Worst child was null. Num children {} ; Max children {}",
                            currentSize, config.getNumChildren());
                }
            }

        }
    };

    private String getChildrenAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(compName).append(" Children: ");
        for (Integer id : registeredClients.keySet()) {
            sb.append(id).append(" = ").append(registeredClients.get(id)).append("\n");
        }
        return sb.toString();
    }

    private boolean removeClient(int id) {
        if (registeredClients.remove(id) == null) {
            logger.warn(compName + "Couldn't find client to remove: {}", id);
            logger.trace(compName + getChildrenAsString());
            return false;
        }
        logger.debug(compName + "Found client and removed: {}", id);
        return true;
    }
    /**
     * Clients are unregistered after a timeout. This is because, in Usurp, we
     * want to keep them as a parent until references to this old parent have
     * expired in the system. Normally, when we switch parents, we unregister
     * the old one with a timeout of around 30 seconds.
     */
    Handler<UnregisterTimeout> handleUnregisterTimeout = new Handler<UnregisterTimeout>() {
        @Override
        public void handle(UnregisterTimeout event) {
            removeClient(event.getSource().getId());
        }
    };
    Handler<HpUnregisterMsg.Request> handleHpUnregisterRequestMsg = new Handler<HpUnregisterMsg.Request>() {
        @Override
        public void handle(HpUnregisterMsg.Request msg) {

            HpUnregisterMsg.Response responseMsg = null;
            if (registeredClients.containsKey(msg.getVodSource().getId())) {
                RegisteredClientRecord r = registeredClients.get(msg.getVodSource().getId());
                if (msg.getTimeoutId() == null) {
                    registeredClients.remove(msg.getSource().getId());
                    return;
                }

                if (!r.isRemoving()) {
                    if (msg.getDelay() >= 0) {
                        ScheduleTimeout st = new ScheduleTimeout(msg.getDelay());
                        UnregisterTimeout timeout = new UnregisterTimeout(st, msg.getSource(),
                                msg.getTimeoutId(), msg.getSource().getId());
                        st.setTimeoutEvent(timeout);
                        delegator.doTrigger(st, timer);
                        responseMsg = new HpUnregisterMsg.Response(self.getAddress(), msg.getVodSource(),
                                HpUnregisterMsg.Response.Status.SUCCESS,
                                msg.getTimeoutId());
                    } else {
                        responseMsg = new HpUnregisterMsg.Response(self.getAddress(), msg.getVodSource(),
                                HpUnregisterMsg.Response.Status.DELAY_LESS_THAN_ZERO,
                                msg.getTimeoutId());
                    }
                } else {
                    responseMsg = new HpUnregisterMsg.Response(self.getAddress(), msg.getVodSource(),
                            HpUnregisterMsg.Response.Status.ALREADY_MOVING,
                            msg.getTimeoutId());
                }
            } else {
                responseMsg = new HpUnregisterMsg.Response(self.getAddress(), msg.getVodSource(),
                        HpUnregisterMsg.Response.Status.NOT_REGISTERED,
                        msg.getTimeoutId());
            }
            logger.debug(compName + responseMsg.getStatus() + " unregister - to " + msg.getVodSource().getId());
            delegator.doTrigger(responseMsg, network);
        }
    };

    /**
     *
     * @param client
     * @param clientId
     * @param delta
     * @param rtt
     */
    private boolean registerClientRecord(VodAddress client, int clientId, long rtt,
            Set<Integer> prpPorts, boolean tempRecord) {
        boolean alreadyRegistered = false;
        if (prpPorts == null) {
            prpPorts = new HashSet<Integer>();
        }
        if (registeredClients.containsKey(clientId) != true) {

            RegisteredClientRecord clientData = new RegisteredClientRecord(
                    client, rtt, prpPorts, tempRecord);
            registeredClients.put(clientId, clientData);
            logger.debug(compName + " " + client + " REGISTERING");
            logger.trace(getChildrenAsString());
        } else {
            // Don't update RTTs here, as some clients may not have supplied a correct RTT
            // don't update tempRecord either, as both nodes may have the same zServer,
            RegisteredClientRecord clientData = registeredClients.get(clientId);
            // update the client, as it may be of type NAT and have new parents
            clientData.setClient(client);
            clientData.setLastHeardFrom(System.currentTimeMillis());
            clientData.setExpirationTime(client.getNatBindingTimeout());
            logger.debug(compName + " " + client + " RE-REGISTERING on Z: ");
            logger.trace(getChildrenAsString());
            // add the new prp ports to the already registered ports...
            Set<Integer> ports = clientData.getPrpPorts();
            ports.addAll(prpPorts);
            alreadyRegistered = true;
        }
        // TODO - is it ok to add me as a parent for the temporary client record client?
        client.addParent(self.getAddress().getPeerAddress());

        return alreadyRegistered;
    }
    Handler<HpConnectMsg.Request> handleHpConnect = new Handler<HpConnectMsg.Request>() {
        @Override
        public void handle(HpConnectMsg.Request request) {
            printMsg(request);

            OpenConnectionResponseType responseType;
            HolePunching session = null;
            // if HP is possible then this variable will have information like which stratagy to use etc etc
            HPMechanism hpMechanism = null;
            VodAddress src = request.getVodSource();

            // first check that both the clients that want to punch holes are registered
            int client_A_ID = request.getClientId();
            int client_B_ID = request.getRemoteClientId();


            registerClientRecord(src, client_A_ID, request.getRtt(),
                    null, true);


            if (registeredClients.containsKey(client_B_ID) != true) {
                // send negative response
                responseType = OpenConnectionResponseType.REMOTE_PEER_NOT_REGISTERED;
            } else {
                // check the nat compatibility
                RegisteredClientRecord record = registeredClients.get(client_B_ID);
                session = HpFeasability.isPossible(src, record.getClient());
                if (session == null) {
                    // TODO  - should I relay the msg?
                    responseType = OpenConnectionResponseType.NAT_COMBINATION_NOT_TRAVERSABLE;
                } else {
                    // traversable clients
                    responseType = OpenConnectionResponseType.HP_WILL_START;
                    hpMechanism = session.getHolePunchingMechanism();
                }
            }

            logger.trace(compName + "sending back the feasibility response to (" + request.getClientId()
                    + "). "
                    //                    + " public addr:" + msg.getSource()
                    + ". HP for [" + client_A_ID + ", " + client_B_ID + "]. resp type: " + responseType);

            HPSessionKey hpSessionKey = new HPSessionKey(client_A_ID, client_B_ID);

            HpConnectMsg.Response responseMsg =
                    new HpConnectMsg.Response(self.getAddress(),
                    request.getVodSource(), request.getRemoteClientId(), responseType,
                    request.getTimeoutId(), hpMechanism, !hpSessions.containsKey(hpSessionKey),
                    request.getMsgTimeoutId());
            delegator.doTrigger(responseMsg, network);

            if (responseType == OpenConnectionResponseType.HP_WILL_START) {
                // HP is possible. lets start HP

                // JIM - should I not create a duplicate session here???
                // first store the session
                logger.trace(compName + "Total Sessions:" + hpSessions.size() + " Creating Session between ID: " + hpSessionKey
                        + " Starting " + session.getHolePunchingMechanism());
                // save the session start time
                session.setSesssionStartTime(System.currentTimeMillis());
                hpSessions.put(hpSessionKey, session);
                // start hole punching
                startHolePunching(hpSessionKey, request.getMsgTimeoutId());
            }
        }
    };

    private boolean isRemoteClientStillAlive(int remoteClientID) {
        RegisteredClientRecord remoteClient = registeredClients.get(remoteClientID);
        if (remoteClient == null) {
            return false;
        } else {
            return true;
        }
    }
//    Handler<SHP_OpenHoleMsg.Request> handle_SHP_OpenHoleMsg = new Handler<SHP_OpenHoleMsg.Request>() {
//
//        @Override
//        public void handle(SHP_OpenHoleMsg.Request msg) {
//            logger.debug(compName + "Open SHP Open Hole Message recvd from Client id: ("
//                    + msg.getClientId() + ") remote client id (" + msg.getRemoteClientId() + ")");
//
//            if (isRemoteClientStillAlive(msg.getRemoteClientId())) {
//                // first send OK response
//                SHP_OpenHoleMsg.Response responseMsg = new SHP_OpenHoleMsg.Response(self.getAddress(),
//                        msg.getVodSource(), SHP_OpenHoleMsg.ResponseType.OK,
//                        msg.getRemoteClientId(), msg.getTimeoutId());
//                delegator.doTrigger(responseMsg, network);
//
//                // Hole has been opened on the nat. now inform the other peer about the hole
//                HPSessionKey key = new HPSessionKey(msg.getClientId(), msg.getRemoteClientId());
//
//                // Add a session for client A, if it doesn't exist
//                HolePunching session = hpSessions.get(key);
//                if (session == null) {
//                    session = new HolePunching(msg.getClientId(),
//                            msg.getRemoteClientId(),
//                            HPMechanism.SHP, /*Overall mechanism used*/
//                            HPRole.SHP_INITIATOR, /*client A will initiate SHP*/
//                            HPRole.SHP_RESPONDER); /*client B will only repond to messages from A*/
//                    hpSessions.put(key, session);
//                }
//
//                // only if initiator is A, is this important.
//                registerClientRecord(msg.getVodSource(), msg.getClientId(), 1, 500 /* rtt */,
//                        null, true);
//
//                if (session == null) {
//                    logger.error(compName + "session not found. key is " + key);
//                    if (throwException) {
//                        throw new NullPointerException(compName + "session not found. key is " + key);
//                    } else {
//                        return;
//                    }
//                }
//
//                if (session.getResponderID() != msg.getRemoteClientId()) {
//                    logger.error(compName + "remote client {} must be responder for SHP instead of {}",
//                            msg.getRemoteClientId(), session.getResponderID());
//                    if (throwException) {
//                        throw new UnsupportedOperationException(compName
//                                + "remote client must be responder for SHP ");
//                    } else {
//                        return;
//                    }
//                }
//
//                // getting other client address
//                RegisteredClientRecord remoteClientRegRecord = registeredClients.get(msg.getRemoteClientId());
//                if (remoteClientRegRecord == null) {
//                    String exception = compName + "SHP Open hole msg from ("
//                            + msg.getClientId() + "). Unable to find remote client registration record. "
//                            + "remote client id (" + msg.getRemoteClientId() + ")";
//                    logger.error(exception);
//                    if (throwException) {
//                        throw new NullPointerException(exception);
//                    } else {
//                        return;
//                    }
//                }
//
//                RegisteredClientRecord clientRegRecord = registeredClients.get(session.getInitiatorID());
//                if (clientRegRecord == null) {
//                    String exception = compName + "SHP Open hole msg from ("
//                            + msg.getClientId() + "). Unable to find client registration record. "
//                            + "remote client id (" + msg.getRemoteClientId() + ")";
//                    logger.error(exception);
//                    if (throwException) {
//                        throw new NullPointerException(exception);
//                    } else {
//                        return;
//                    }
//                }
//
//                logger.debug(printChildren());
//                GoMsg.Request goMessage = new GoMsg.Request(self.getAddress(),
//                        remoteClientRegRecord.getClient(), // public address of the other client
//                        msg.getVodSource(), // hole opened on the nat
//                        // id of the client who opened the hole on his nat
//                        session.getHolePunchingMechanism(),/*hole punching mechanism */
//                        session.getHolePunchingRoleOf(session.getResponderID()),
//                        NUM_RETRIES_SHP);
//
//                delegator.doTrigger(goMessage, network);
//            } else {
//                logger.warn(compName + "handleOpenHoleMessage HP Failed coz remote client ("
//                        + msg.getRemoteClientId() + ") is dead");
//
//                // send nack to the the cleint
//                SHP_OpenHoleMsg.Response responseMsg = new SHP_OpenHoleMsg.Response(self.getAddress(),
//                        msg.getVodSource(), SHP_OpenHoleMsg.ResponseType.FAILED,
//                        msg.getRemoteClientId(), msg.getTimeoutId());
//                delegator.doTrigger(responseMsg, network);
//
//                //delete the hp session
//                HPSessionKey key = new HPSessionKey(msg.getClientId(), msg.getRemoteClientId());
//                hpSessions.remove(key);
//            }
//
//        }
//    };
    Handler<ParentKeepAliveMsg.Ping> handleParentKeepAliveMsgPing =
            new Handler<ParentKeepAliveMsg.Ping>() {
        @Override
        public void handle(ParentKeepAliveMsg.Ping ping) {
            // every open peer is running hole punching client and server
            // if someone sends a ping message to the open peer then
            // both the client and server component will recv the ping message
            // solution: hole punching server only listens on 3478 so
            // ignore the ping if its destination port is not 3478

            logger.trace(compName + " received connection keep alive ping message from ("
                    + ping.getVodSource() + ") ");

            // update the client record time stamp
            RegisteredClientRecord record = registeredClients.get(ping.getClientId());
            if (record != null) {
                // If a node is behind a NAT that changes the IP address of the client
                // or its port dynamically, this can potentially happen. Or if the client
                // changes from WiFi to 3G or something like that.
                // Or if it changes its set of parents, its VodAddress will change.
                logger.trace(compName + " Sending Pong back to : " 
                        + record.getClient().getPeerAddress());
                logger.trace(compName + " Sending Pong really back to : " 
                        + ping.getVodSource());
                record.setClient(ping.getVodSource());
                record.setLastHeardFrom(System.currentTimeMillis());

                // send back pong
                ParentKeepAliveMsg.Pong pong = new ParentKeepAliveMsg.Pong(
                        self.getAddress(), ping.getVodSource(),
                        ping.getTimeoutId());
                delegator.doTrigger(pong, network);
            } else {
                logger.debug(compName + " Ping recvd from a non-child: : " + ping.getVodSource());
                // Ping'ing node is not a child, tell it to remove itself as a child
                HpUnregisterMsg.Request msg =
                        new HpUnregisterMsg.Request(self.getAddress(),
                        ping.getVodSource(),
                        0, HpRegisterMsg.RegisterStatus.NOT_CHILD);
                delegator.doRetry(msg);
            }
        }
    };
    /**
     */
    Handler<PRP_ConnectMsg.Request> handlePRP_ConnectMsgRequest = new Handler<PRP_ConnectMsg.Request>() {
        @Override
        public void handle(PRP_ConnectMsg.Request request) {
            printMsg(request);
            if (isRemoteClientStillAlive(request.getRemoteClientId())) {
                HPSessionKey key = new HPSessionKey(request.getClientId(), request.getRemoteClientId());
                HolePunching session = hpSessions.get(key);

                HPRole myRole = HPRole.PRP_INITIATOR;
                HPRole yourRole = HPRole.PRP_RESPONDER;
                registerClientRecord(request.getVodSource(), request.getClientId(),
                        500 , request.getSetOfAvailablePorts(), true);

                if (session == null) {
                    // create a new session
                    session = new HolePunching(request.getClientId(),
                            request.getRemoteClientId(),
                            HPMechanism.PRP, /*
                             * Overall mechanism used
                             */
                            myRole, /*
                             * client A will initiate SHP
                             */
                            yourRole); /*
                     * client B will only repond to messages from A
                     */
                    hpSessions.put(key, session);
                }


                if (session.getResponderID() != request.getRemoteClientId()) {
                    PRP_ConnectMsg.Response responseMsg =
                            new PRP_ConnectMsg.Response(self.getAddress(),
                            request.getVodSource(),
                            request.getTimeoutId(),
                            PRP_ConnectMsg.ResponseType.RESPONDER_ID_REMOTE_ID_DO_NOT_MATCH,
                            request.getRemoteClientId(),
                            null, 0, false,
                            request.getMsgTimeoutId());
                    delegator.doTrigger(responseMsg, network);
                    return;
                }

                // selecting an available port
                int selectedPort = selectAvailablePortForPRP(
                        request.getSetOfAvailablePorts());

                // making remote client dummy public address
                RegisteredClientRecord responderClient = registeredClients.get(request.getRemoteClientId());
                Address remoteClientDummyPublicAddr = new Address(responderClient.getClient().getIp(),
                        dummyPort, responderClient.getClient().getId());

                VodAddress remoteClientDummyPublicAddress = new VodAddress(remoteClientDummyPublicAddr,
                        responderClient.getClient().getOverlayId(),
                        responderClient.getClient().getNatPolicy(),
                        responderClient.getClient().getParents()); // null


                boolean bindFirst = false;
                if (selectedPort == 0) {
                    bindFirst = true;
                    // ask for more ports from child
                    PRP_PreallocatedPortsMsg.Request r =
                            new PRP_PreallocatedPortsMsg.Request(self.getAddress(),
                            responderClient.getClient(),
                            UUID.nextUUID());
                    ScheduleRetryTimeout st = new ScheduleRetryTimeout(3000, 1, 2d);
                    PRP_PreallocatedPortsMsg.RequestRetryTimeout t =
                            new PRP_PreallocatedPortsMsg.RequestRetryTimeout(st, r);
                    delegator.doRetry(t);
                }
                PRP_ConnectMsg.Response responseMsg =
                        new PRP_ConnectMsg.Response(self.getAddress(),
                        request.getVodSource(),
                        request.getTimeoutId(),
                        PRP_ConnectMsg.ResponseType.OK,
                        request.getRemoteClientId(),
                        remoteClientDummyPublicAddress,
                        selectedPort, bindFirst,
                        request.getMsgTimeoutId());
                delegator.doTrigger(responseMsg, network);

                // inform the other client about the hole that will be opened on the initiators nat
                // making a go message
                Address openInitHole = new Address(request.getSource().getIp(),
                        selectedPort, request.getSource().getId());
                VodAddress openedHoleOnInitiatorsNat = new VodAddress(openInitHole,
                        VodConfig.SYSTEM_OVERLAY_ID, request.getVodSource().getNatPolicy(),
                        request.getVodSource().getParents());

                GoMsg.Request goMessage = new GoMsg.Request(self.getAddress(),
                        responderClient.getClient(),
                        openedHoleOnInitiatorsNat,
                        HPMechanism.PRP, HPRole.PRP_RESPONDER,
                        NUM_RETRIES_PRP,
                        request.getMsgTimeoutId());
                // Oneway msg, so it's ok to trigger and not call doRetry
                delegator.doTrigger(goMessage, network);
            } else {
                logger.warn(compName + "handlePRP_SenAvailablePortsTozServer HP Failed "
                        + "coz remote client ("
                        + request.getRemoteClientId() + ") is dead");

                // send nack to the the client
                PRP_ConnectMsg.Response responseMsg =
                        new PRP_ConnectMsg.Response(self.getAddress(), request.getVodSource(),
                        request.getTimeoutId(),
                        PRP_ConnectMsg.ResponseType.FAILED, request.getRemoteClientId(),
                        null, 0, false, request.getMsgTimeoutId());
                delegator.doTrigger(responseMsg, network);

                //delete the hp session
                HPSessionKey key = new HPSessionKey(request.getClientId(), request.getRemoteClientId());
                hpSessions.remove(key);
            }

        }
    };
    /**
     * This handler is where the client has PRP, and has allocated a port
     * locally and now wants to connect to another PRP or PRC node.
     * 
     * This ZServer should give out the first port supplied by the initiatingClient
     * to the remoteClient. The zServer may also have a pre-allocated port from the
     * remoteClient that it immediately returns to the initiatingClient. If the
     * zServer doesn't have a pre-allocated port for the initiatingClient, it picks
     * a random port (50000+) and hopes that the initiatingClient is not already using
     * it. It then tells the remoteClient to use that when connecting, and it tells
     * the remoteClient to bind to it and try and connect to the initiatingClient with
     * it. The port may already be bound at the remoteClient, in which case it should
     * bind a new port, and tell the zServer to tell the initiatingClient to use the
     * new port.
     * 
     */
    Handler<Interleaved_PRP_ConnectMsg.Request> handle_Interleaved_PRP_ConnectRequest = new Handler<Interleaved_PRP_ConnectMsg.Request>() {
        /**
         * Used by both PRP_PRP and PRP_PRC clients (PRP from PRP_PRC sends this
         * msg).
         */
        @Override
        public void handle(Interleaved_PRP_ConnectMsg.Request request) {
            printMsg(request);
            String compName = RendezvousServer.this.compName
                    + " - " + request.getMsgTimeoutId() + " ";
            HPSessionKey key = new HPSessionKey(request.getClientId(), request.getRemoteClientId());
            int remoteId = request.getRemoteClientId();
            boolean requestNewRemotePorts = false;

            if (isRemoteClientStillAlive(remoteId)) {
                RegisteredClientRecord remoteRecord = registeredClients.get(remoteId);
                boolean isPrc = remoteRecord.getClient().getNat().getAllocationPolicy()
                        == Nat.AllocationPolicy.PORT_CONTIGUITY;

                HolePunching session = hpSessions.get(key);
                int clientInterleavedPort = 0;
                boolean bindPort = false;
                if (session == null) {
                    logger.debug(compName + "Creating a new session for: " + key);
                    if (!isPrc) {
                        session = new HolePunching(request.getClientId(),
                                remoteId,
                                HPMechanism.PRP_PRP, /*
                                 * Overall mechanism used
                                 */
                                HPRole.PRP_INTERLEAVED, /*
                                 * client A will do PRP
                                 */
                                HPRole.PRP_INTERLEAVED); /*
                         * client B will do PRP
                         */
                        Integer prpPort = null;
                        try {
                            prpPort = remoteRecord.popPrpPort();
                            if (remoteRecord.sizePrpPorts() < 2) {
                                requestNewRemotePorts = true;
                            }
                        } catch (NoPortsException ex) {
                            requestNewRemotePorts = true;
                            // Tell the client to allocate this random port
                            prpPort = PortSelector.selectRandomPortOver50000();
                            bindPort = true;
                        }
                        session.set_Interleaved_PRP_Port(remoteId, prpPort);

                        Set<Integer> ports = request.getSetOfAvailablePorts();
                        if (ports.isEmpty()) {
                            logger.warn(compName + " no PP ports sent for PRP_PRC ");
                            sendInterleavedPortsResponse(request.getVodSource(),
                                    request.getTimeoutId(),
                                    Interleaved_PRP_ConnectMsg.ResponseType.SEND_MORE_PORTS,
                                    request.getRemoteClientId(),
                                    request.getMsgTimeoutId());
                        }
                        clientInterleavedPort = selectAvailablePortForPRP(ports);
                        session.set_Interleaved_PRP_Port(request.getClientId(), clientInterleavedPort);

                        logger.debug(compName + "Session id(port) are: "
                                + remoteId + "(" + prpPort + "), "
                                + request.getClientId() + "(" + clientInterleavedPort + ")");
                    } else {
                        session = new HolePunching(request.getClientId(),
                                remoteId,
                                HPMechanism.PRP_PRC, /*
                                 * Overall mechanism used
                                 */
                                HPRole.PRP_INTERLEAVED, /*
                                 * client A will do PRP
                                 */
                                HPRole.PRC_INTERLEAVED); /*
                         * client B will do PRC
                         */
                    }
                    hpSessions.put(key, session);
                } else {
                    clientInterleavedPort =
                            session.get_Interleaved_PRP_Port(request.getClientId());
                    session.setSesssionStartTime(System.currentTimeMillis());
                }

                registerClientRecord(request.getVodSource(), request.getClientId(),
                         1500, request.getSetOfAvailablePorts(), true);

                // selecting an available port - if there was already an existing session
                // then use the old PRP port from that session - not one of the newly supplied ones.
                // TODO is that desired behaviour?


                logger.debug(compName + "Found PRP Interleaved port: " + clientInterleavedPort
                        + " for " + request.getSource().getId());

                if (clientInterleavedPort != 0) {
                    // first save the interleaved port in the session

                    // send the ok response back to cancel retry.
                    sendInterleavedPortsResponse(request.getVodSource(),
                            request.getTimeoutId(),
                            Interleaved_PRP_ConnectMsg.ResponseType.OK,
                            request.getRemoteClientId(),
                            request.getMsgTimeoutId());

                    // for PRP_PRC, if other client's interleaved port is set,
                    // then send the go messages to both the clients
                    if (session.getHolePunchingMechanism() == HPMechanism.PRP_PRC) {

                        // 1st request PRC port
                        // Then send GoMsg for PRP

                        Address dummyPublicAddress = new Address(
                                request.getVodSource().getIp(),
                                session.get_Interleaved_PRP_Port(request.getClientId()),
                                request.getClientId());

                        VodAddress prpResponderAddr =
                                new VodAddress(dummyPublicAddress,
                                request.getVodSource().getId(),
                                request.getVodSource().getNatPolicy(),
                                request.getVodSource().getParents());

                        Interleaved_PRC_ServersRequestForPredictionMsg.Request requestMsg =
                                new Interleaved_PRC_ServersRequestForPredictionMsg.Request(
                                self.getAddress(),
                                remoteRecord.getClient(),
                                request.getClientId(), HPMechanism.PRP_PRC,
                                HPRole.PRC_INTERLEAVED,
                                prpResponderAddr, request.getMsgTimeoutId());

                        // oneway msg, so ok to call trigger instead of retry
                        delegator.doTrigger(requestMsg, network);

                    } else if (session.getHolePunchingMechanism() == HPMechanism.PRP_PRP) {
                        // TODO - client supplieqd PRP ports - tell remote
                        // to connect to it.
                        int remoteClientInterleavedPort =
                                session.get_Interleaved_PRP_Port(request.getRemoteClientId());
                        Address remoteClientH = new Address(
                                remoteRecord.getClient().getIp(),
                                remoteClientInterleavedPort,
                                remoteRecord.getClient().getId());
                        VodAddress remoteClientHole = new VodAddress(remoteClientH,
                                VodConfig.SYSTEM_OVERLAY_ID,
                                remoteRecord.getClient().getNatPolicy(),
                                remoteRecord.getClient().getParents());

                        GoMsg.Request goMessageForThisClient = new GoMsg.Request(self.getAddress(),
                                request.getVodSource(),
                                remoteClientHole,
                                session.getHolePunchingMechanism(),
                                session.getHolePunchingRoleOf(request.getClientId()),
                                NUM_RETRIES_PRP_PRP,
                                clientInterleavedPort, bindPort,
                                request.getMsgTimeoutId());

                        logger.debug(compName + request.getVodSource() + " interleaved Port is: "
                                + goMessageForThisClient.get_PRP_PRP_InterleavedPort());
                        // oneway msg, so ok to call trigger instead of retry
                        delegator.doTrigger(goMessageForThisClient, network);

                        Address thisClientH = new Address(request.getSource().getIp(),
                                clientInterleavedPort, request.getSource().getId());
                        VodAddress thisClientHole = new VodAddress(thisClientH,
                                VodConfig.SYSTEM_OVERLAY_ID, request.getVodSource().getNatPolicy(),
                                request.getVodSource().getParents());

                        // inform the other client about the holes that will be opened on the opposite nats
                        GoMsg.Request goMessageForRemoteClient = new GoMsg.Request(self.getAddress(),
                                remoteRecord.getClient(),
                                thisClientHole,
                                session.getHolePunchingMechanism(),
                                session.getHolePunchingRoleOf(request.getRemoteClientId()),
                                NUM_RETRIES_PRP_PRP,
                                session.get_Interleaved_PRP_Port(remoteId),
                                bindPort,
                                request.getMsgTimeoutId());

                        logger.debug(compName + remoteRecord.getClient() + " interleaved Port is: "
                                + goMessageForRemoteClient.get_PRP_PRP_InterleavedPort());
                        // oneway msg, so ok to call trigger instead of retry                        
                        delegator.doTrigger(goMessageForRemoteClient, network);
                    }


                } else {
                    logger.warn(compName + "Ask the client to send more available ports to the server. "
                            + "All ports sent by the client are in use by some one else behind the same nat "
                            + request.getSource());
                    sendInterleavedPortsResponse(request.getVodSource(),
                            request.getTimeoutId(),
                            Interleaved_PRP_ConnectMsg.ResponseType.SEND_MORE_PORTS,
                            request.getRemoteClientId(),
                            request.getMsgTimeoutId());
                    hpSessions.remove(key);
                }
                if (requestNewRemotePorts) {
                    // ask for more ports from child
                    PRP_PreallocatedPortsMsg.Request r =
                            new PRP_PreallocatedPortsMsg.Request(self.getAddress(), request.getVodSource(),
                            request.getMsgTimeoutId());
                    ScheduleRetryTimeout st = new ScheduleRetryTimeout(2000, 1, 2d);
                    PRP_PreallocatedPortsMsg.RequestRetryTimeout t =
                            new PRP_PreallocatedPortsMsg.RequestRetryTimeout(st, r);
                    delegator.doRetry(t); // retry twice
                }

            } else {
                logger.warn(compName + "handle_Interleaved_PRP_SendAvailablePortsTozServerMsg HP Failed coz remote client ("
                        + request.getRemoteClientId() + ") is not registered at parent ");
                sendInterleavedPortsResponse(request.getVodSource(),
                        request.getTimeoutId(),
                        Interleaved_PRP_ConnectMsg.ResponseType.REMOTE_ID_NOT_REGISTERED,
                        request.getRemoteClientId(),
                        request.getMsgTimeoutId());
                hpSessions.remove(key);
            }
        }
    };

    private void sendInterleavedPortsResponse(VodAddress dest, TimeoutId timeoutId,
            Interleaved_PRP_ConnectMsg.ResponseType response,
            int remoteId, TimeoutId msgTimeoutId) {
        // send ack to the the client
        Interleaved_PRP_ConnectMsg.Response responseMsg =
                new Interleaved_PRP_ConnectMsg.Response(self.getAddress(),
                dest, timeoutId, response, remoteId, msgTimeoutId);
        delegator.doTrigger(responseMsg, network);
    }
    Handler<PRC_OpenHoleMsg.Request> handle_PRC_OpenHoleMsg =
            new Handler<PRC_OpenHoleMsg.Request>() {
        @Override
        public void handle(PRC_OpenHoleMsg.Request request) {
            printMsg(request);

            prc(request.getVodSource(), request.getTimeoutId(),
                    request.getRemoteClientId(), request.getMsgTimeoutId());
        }
    };

    private void prc(VodAddress client, TimeoutId timeoutId, int remoteId, TimeoutId msgTimeoutId) {

        String compName = RendezvousServer.this.compName + " - " + msgTimeoutId + " ";

        if (isRemoteClientStillAlive(remoteId)) {
            // first send back the response
            PRC_OpenHoleMsg.Response responseMsg = new PRC_OpenHoleMsg.Response(self.getAddress(),
                    client,
                    timeoutId,
                    PRC_OpenHoleMsg.ResponseType.OK,
                    remoteId, msgTimeoutId);
            delegator.doTrigger(responseMsg, network);
            RegisteredClientRecord responder = registeredClients.get(remoteId);

            HPSessionKey key = new HPSessionKey(client.getId(), remoteId);
            HolePunching session = hpSessions.get(key);

            if (session == null) { // PRP-PRC with client as PRC
                session = new HolePunching(client.getId(), remoteId,
                        HPMechanism.PRP_PRC,
                        HPRole.PRC_INTERLEAVED, HPRole.PRP_INTERLEAVED);
            }

            // predict the port opened on the initiator's nat
            int predictedPort = predictPRCOpenPort(client.getId(), client.getPeerAddress());
            logger.trace(compName + "predicted port for " + key + " is " + predictedPort);
            // send go message to the responder

            // sanity check
            if (session.getResponderID() != remoteId) {
                throw new UnsupportedOperationException("Responder does not match the remote client id");
            }

            Address openedHole = new Address(client.getIp(), predictedPort, client.getId());
            VodAddress openedHoleOnInitiatorNat = new VodAddress(openedHole,
                    VodConfig.SYSTEM_OVERLAY_ID, client.getNatPolicy(),
                    client.getParents());


            GoMsg.Request goMsg = new GoMsg.Request(self.getAddress(), responder.getClient(),
                    openedHoleOnInitiatorNat,
                    session.getHolePunchingMechanism(),
                    session.getHolePunchingRoleOf(session.getResponderID()),
                    NUM_RETRIES_PRC, msgTimeoutId);
            // oneway msg, so ok to call trigger instead of retry            
            delegator.doTrigger(goMsg, network);
        } else {
            logger.warn(compName + "handle_PRC_OpenHoleMsg HP Failed coz remote client ("
                    + remoteId + ") is dead");

            // first send back the response
            PRC_OpenHoleMsg.Response responseMsg = new PRC_OpenHoleMsg.Response(self.getAddress(),
                    client, timeoutId,
                    PRC_OpenHoleMsg.ResponseType.FAILED, remoteId,
                    msgTimeoutId);
            delegator.doTrigger(responseMsg, network);

            //delete the hp session
            HPSessionKey key = new HPSessionKey(client.getId(), remoteId);
            hpSessions.remove(key);
        }

    }
    Handler<Interleaved_PRC_OpenHoleMsg.Request> handle_Interleaved_PRC_OpenHoleMsg =
            new Handler<Interleaved_PRC_OpenHoleMsg.Request>() {
        @Override
        public void handle(Interleaved_PRC_OpenHoleMsg.Request request) {
            printMsg(request);
            String compName = RendezvousServer.this.compName + " - " + request.getMsgTimeoutId() + " ";

            RegisteredClientRecord clientRecord = registeredClients.get(request.getClientId());
            if (clientRecord == null) {
                String exception = compName + "handle_Interleaved_PRC_OpenHoleMsg msg from ("
                        + request.getClientId() + "). Unable to find client registration record. "
                        + "remote client id (" + request.getRemoteClientId() + ")";
                logger.error(exception);
                if (throwException) {
                    throw new NullPointerException(exception);
                } else {
                    return;
                }
            }

            if (isRemoteClientStillAlive(request.getRemoteClientId())) {
                // first send back the response
                Interleaved_PRC_OpenHoleMsg.Response responseMsg =
                        new Interleaved_PRC_OpenHoleMsg.Response(self.getAddress(),
                        request.getVodSource(),
                        request.getTimeoutId(),
                        Interleaved_PRC_OpenHoleMsg.ResponseType.OK,
                        request.getRemoteClientId(),
                        request.getMsgTimeoutId());
                delegator.doTrigger(responseMsg, network);

                // predict the port opened on the initiator's nat
                int thisClientInterleavedPort = predictPRCOpenPort(request.getClientId(),
                        request.getSource()/*
                         * hole opened on the initiator nat
                         */);
                // send go message to the responder
                // sanity check
                HPSessionKey key = new HPSessionKey(request.getClientId(), request.getRemoteClientId());
                HolePunching session = hpSessions.get(key);
                session.set_Interleaved_PRC_PredictivePort(request.getClientId(), thisClientInterleavedPort);

                // TODO: what if replies arrive at different zServers. 
                // This will never get executed.
                if (areBothRepliesRecvd(session)) {

                    // send go message to both the clients
                    int remoteClientInterleavedPort = 0;
                    if (session.getHolePunchingRoleOf(request.getRemoteClientId()) == HPRole.PRP_INTERLEAVED) {
                        remoteClientInterleavedPort = session.get_Interleaved_PRP_Port(request.getRemoteClientId());
                    } else {
                        remoteClientInterleavedPort = session.get_Interleaved_PRC_PredictedPort(request.getRemoteClientId());
                    }

                    RegisteredClientRecord remoteClientRecord = registeredClients.get(request.getRemoteClientId());

                    Address openedHoleOnThisClient = new Address(request.getSource().getIp(),
                            thisClientInterleavedPort, request.getSource().getId());
                    VodAddress openedHoleOnThisClientNat = new VodAddress(openedHoleOnThisClient,
                            VodConfig.SYSTEM_OVERLAY_ID, request.getVodSource().getNatPolicy(),
                            request.getVodSource().getParents());

                    Address openedHoleOnRemoteClient = new Address(remoteClientRecord.getClient().getIp(),
                            remoteClientInterleavedPort, remoteClientRecord.getClient().getId());
                    VodAddress openedHoleOnRemoteClientNat = new VodAddress(openedHoleOnRemoteClient,
                            VodConfig.SYSTEM_OVERLAY_ID,
                            remoteClientRecord.getClient().getNatPolicy(),
                            remoteClientRecord.getClient().getParents());

                    VodAddress remoteAddr = remoteClientRecord.getClient();

                    int numRemoteRetries = (session.getHolePunchingRoleOf(request.getRemoteClientId()) == HPRole.PRP_INTERLEAVED)
                            ? NUM_RETRIES_PRP_PRP : NUM_RETRIES_PRC_PRC;

                    GoMsg.Request goMsgForRemoteClient;
                    if (session.getHolePunchingRoleOf(request.getRemoteClientId()) == HPRole.PRP_INTERLEAVED) {
                        goMsgForRemoteClient = new GoMsg.Request(self.getAddress(),
                                remoteAddr,
                                openedHoleOnThisClientNat,
                                session.getHolePunchingMechanism(),
                                session.getHolePunchingRoleOf(request.getRemoteClientId()),
                                numRemoteRetries,
                                session.get_Interleaved_PRP_Port(request.getRemoteClientId()),
                                false,
                                request.getMsgTimeoutId());
                    } else {
                        goMsgForRemoteClient = new GoMsg.Request(self.getAddress(),
                                remoteAddr,
                                openedHoleOnThisClientNat,
                                session.getHolePunchingMechanism(),
                                session.getHolePunchingRoleOf(request.getRemoteClientId()),
                                numRemoteRetries,
                                request.getMsgTimeoutId());
                    }

                    GoMsg.Request goMsgForThisClient;
                    if (session.getHolePunchingRoleOf(request.getClientId()) == HPRole.PRP_INTERLEAVED) {
                        goMsgForThisClient = new GoMsg.Request(self.getAddress(),
                                request.getVodSource(),
                                openedHoleOnRemoteClientNat,
                                session.getHolePunchingMechanism(),
                                session.getHolePunchingRoleOf(request.getClientId()),
                                NUM_RETRIES_PRP_PRP,
                                session.get_Interleaved_PRP_Port(request.getClientId()),
                                false,
                                request.getMsgTimeoutId());
                    } else {
                        goMsgForThisClient = new GoMsg.Request(self.getAddress(),
                                request.getVodSource(),
                                openedHoleOnRemoteClientNat,
                                session.getHolePunchingMechanism(),
                                session.getHolePunchingRoleOf(request.getClientId()),
                                NUM_RETRIES_PRC_PRC,
                                request.getMsgTimeoutId());
                    }

                    // oneway msgs, so ok to call trigger instead of retry
                    delegator.doTrigger(goMsgForRemoteClient, network);
                    delegator.doTrigger(goMsgForThisClient, network);

                }
            } else {
                logger.warn(compName + "handle_Interleaved_PRC_OpenHoleMsg HP Failed coz remote client ("
                        + request.getRemoteClientId() + ") is dead");

                // send nack to the the cleint
                Interleaved_PRC_OpenHoleMsg.Response responseMsg = new Interleaved_PRC_OpenHoleMsg.Response(self.getAddress(),
                        request.getVodSource(),
                        request.getTimeoutId(),
                        Interleaved_PRC_OpenHoleMsg.ResponseType.FAILED,
                        request.getRemoteClientId(),
                        request.getMsgTimeoutId());
                delegator.doTrigger(responseMsg, network);

                //delete the hp session
                HPSessionKey key = new HPSessionKey(request.getClientId(), request.getRemoteClientId());
                hpSessions.remove(key);
            }

        }
    };
    Handler<GarbageCleanupTimeout> handleGarbageCleanupTimeout =
            new Handler<GarbageCleanupTimeout>() {
        @Override
        public void handle(GarbageCleanupTimeout request) {
            // cleaning registered client records
            if (!registeredClients.isEmpty()) {
                Set<Integer> toBeDeleted = new HashSet<Integer>();
                for (int clientID : registeredClients.keySet()) {
                    RegisteredClientRecord client = registeredClients.get(clientID);
                    if ((System.currentTimeMillis() - client.getLastHeardFrom()) > client.getExpirationTime()) {
                        // open peers are registered withit self
                        if (client.getClient().getId() != self.getId()) {
                            toBeDeleted.add(clientID);
                        }
                    }
                }

                for (int clientID : toBeDeleted) {
                    logger.debug(compName + " Deleting the registration record for " + clientID);
                    registeredClients.remove(clientID);
                }
            }

            // cleaning stale hp sessions
            if (!hpSessions.isEmpty()) {
                Set<HPSessionKey> toBeDeleted = new HashSet<HPSessionKey>();
                for (HPSessionKey key : hpSessions.keySet()) {
                    HolePunching session = hpSessions.get(key);
                    if ((System.currentTimeMillis() - session.getSesssionStartTime()) > sessionExpirationTime) {
                        toBeDeleted.add(key);
                    }
                }

                for (HPSessionKey key : toBeDeleted) {
                    logger.debug(compName + " Deleting the hp Session for " + key);
                    hpSessions.remove(key);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(compName).append("Registered Clients:  ");
            for (int id : registeredClients.keySet()) {
                sb.append(id).append(",");
            }
            logger.trace(compName + sb.toString());
            sb.delete(0, sb.length() - 1);
            sb.append(compName).append("Registered Sessions:  ");
            for (HPSessionKey key : hpSessions.keySet()) {
                sb.append(key.getClient_A_ID()).append(":").append(key.getClient_B_ID()).append(",");
            }
            logger.trace(compName + sb.toString());

        }
    };

    Handler<RelayRequestMsg.ClientToServer> handleRelayRequestMsg =
            new Handler<RelayRequestMsg.ClientToServer>() {
        @Override
        public void handle(RelayRequestMsg.ClientToServer request) {
            printMsg(request);

            logger.trace(compName + "received request to relay message from (" + request.getClientId() + ") "
                    + "to (" + request.getRemoteClientId() + ")" + " - " + request.getMsgTimeoutId());
            RegisteredClientRecord remoteClientRecord = registeredClients.get(request.getRemoteClientId());
            if (remoteClientRecord != null) {
                RelayRequestMsg.ServerToClient requestServerToClient =
                        new RelayRequestMsg.ServerToClient(self.getAddress(),
                        remoteClientRecord.getClient(),
                        request.getClientId(),
                        request.getMessage());

                // ok to call trigger instead of retry, as it is a response
                delegator.doTrigger(requestServerToClient, network);
            }

        }
    };

    private boolean areBothRepliesRecvd(HolePunching session) {
        boolean retVal = false;
        if (session.getHolePunchingMechanism() == HPMechanism.PRC_PRC) {
            if (session.get_Interleaved_PRC_PredictedPort(session.getClient_A_ID()) != 0
                    && session.get_Interleaved_PRC_PredictedPort(session.getClient_B_ID()) != 0) {
                retVal = true;
            }
        } else if (session.getHolePunchingMechanism() == HPMechanism.PRP_PRP) {
            if (session.get_Interleaved_PRP_Port(session.getClient_A_ID()) != 0
                    && session.get_Interleaved_PRP_Port(session.getClient_B_ID()) != 0) {
                retVal = true;
            }
        } else if (session.getHolePunchingMechanism() == HPMechanism.PRP_PRC) {
            if (session.get_Interleaved_PRP_Port(session.getClient_A_ID()) != 0
                    && session.get_Interleaved_PRC_PredictedPort(session.getClient_B_ID()) != 0) {
                retVal = true;
            } else if (session.get_Interleaved_PRP_Port(session.getClient_B_ID()) != 0
                    && session.get_Interleaved_PRC_PredictedPort(session.getClient_A_ID()) != 0) {
                retVal = true;
            }
        }
        logger.debug(compName + " Are both replies recvd: " + retVal);
        return retVal;
    }

    private int predictPRCOpenPort(int initiatorID, Address holeForzServer) {
        RegisteredClientRecord initiatorRecord = registeredClients.get(initiatorID);
        if (initiatorRecord == null) {
            throw new NullPointerException(compName + "predictPRCOpenPort can not find initiator record "
                    + "initiator id (" + initiatorID + ")");
        }
        Nat.MappingPolicy initiatorNat = initiatorRecord.getClient().getMappingPolicy();

        int port;
        if (initiatorNat == Nat.MappingPolicy.ENDPOINT_INDEPENDENT) {
            port = holeForzServer.getPort();
        } else {
            port = holeForzServer.getPort() + initiatorRecord.getDelta();
        }
        return port;
    }

    private int selectAvailablePortForPRP(Set<Integer> setOfAvailablePortsSentByClient) {
        int selectedPort = 0;
        for (int availablePort : setOfAvailablePortsSentByClient) {
            selectedPort = availablePort;
            break;
        }
        return selectedPort;
    }

    private void startHolePunching(HPSessionKey hpSessionKey, TimeoutId msgTimeoutId) {
        //depending on the NATs of the two clients we start the hole punching
        //mechanism i.e. SHP, PRP, PRC etc

        //first get the sesssion
        HolePunching session = hpSessions.get(hpSessionKey);
        int client_A_ID = session.getClient_A_ID();
        int client_B_ID = session.getClient_B_ID();
        int remoteClientFor_A = session.getClient_B_ID();
        int remoteClientFor_B = session.getClient_A_ID();

        RegisteredClientRecord client_A_RegRecord = registeredClients.get(client_A_ID);
        RegisteredClientRecord client_B_RegRecord = registeredClients.get(client_B_ID);

        VodAddress client_A_PublicAddress = client_A_RegRecord.getClient();
        VodAddress client_B_PublicAddress = client_B_RegRecord.getClient();

        HPRole client_A_HPRole = session.getHolePunchingRoleOf(client_A_ID);
        HPRole client_B_HPRole = session.getHolePunchingRoleOf(client_B_ID);

        HPMechanism hpMechanism = session.getHolePunchingMechanism();

        if (session.getHolePunchingMechanism() == HPMechanism.CONNECTION_REVERSAL) {
            // send go message to the peer behind the nat
            if (client_A_HPRole == HPRole.CONNECTION_REVERSAL_OPEN) {
                // send go message to client B 
                GoMsg.Request goMessageForB = new GoMsg.Request(self.getAddress(),
                        client_B_PublicAddress,
                        client_A_PublicAddress,
                        hpMechanism,
                        client_B_HPRole,
                        NUM_RETRIES_CONNECTION_REVERSAL,
                        msgTimeoutId);
                // oneway msg, so ok to call trigger instead of retry
                delegator.doTrigger(goMessageForB, network);
            } else if (client_B_HPRole == HPRole.CONNECTION_REVERSAL_OPEN) {
                // send go message to client A
                GoMsg.Request goMessageForA = new GoMsg.Request(self.getAddress(),
                        client_A_PublicAddress,
                        client_B_PublicAddress,
                        hpMechanism,
                        client_A_HPRole,
                        NUM_RETRIES_CONNECTION_REVERSAL,
                        msgTimeoutId);
                // oneway msg, so ok to call trigger instead of retry
                delegator.doTrigger(goMessageForA, network);
            }
        } else if (session.getHolePunchingMechanism() == HPMechanism.SHP) {
            // for SHP z server sends the SHP messaage to the initiator
            // initiator sends a message to z from some Ua opened on the nat
            // z server forwards the Ua to responder
            // responder sends the message to Ua and opens a point Ub on its nat
            // initiator sends message to Ub and we have a HOLE.

            // sending SimpleHolePunching initiation message to Initiator client

            int initiatorID = session.getInitiatorID();
            int responderID = session.getResponderID();
            RegisteredClientRecord initiator = registeredClients.get(initiatorID);
            RegisteredClientRecord responder = registeredClients.get(responderID);
            SHP_OpenHoleMsg.Initiator responseMsg = new SHP_OpenHoleMsg.Initiator(self.getAddress(),
                    initiator.getClient(), responder.getClient(), SHP_OpenHoleMsg.ResponseType.OK,
                    msgTimeoutId);
                // oneway msg, so ok to call trigger instead of retry
            delegator.doTrigger(responseMsg, network);

            GoMsg.Request goMessage = new GoMsg.Request(self.getAddress(),
                    responder.getClient(),
                    initiator.getClient(),
                    session.getHolePunchingMechanism(),
                    session.getHolePunchingRoleOf(session.getResponderID()),
                    NUM_RETRIES_SHP, msgTimeoutId);
            // oneway msg, so ok to call trigger instead of retry
            delegator.doTrigger(goMessage, network);
        } else if (session.getHolePunchingMechanism() == HPMechanism.PRP) {
            // if this is PRP-PRC, then it is the PRC-client as the PRP client sends PrpConnect
            /*
             * This handler is where the client needs to connect using PRP to
             * the remote node. The remote node has allocated a port locally and
             * now this client wants to connect to it using PRP. This handler
             * sends a dummy msg to the remote node to open the PRP port on the
             * NAT for the remote IP:port. A GoMsg is sent back to the client.
             */
            int initiatorID = session.getInitiatorID();
            int responderID = session.getResponderID();
            RegisteredClientRecord initiator = registeredClients.get(initiatorID);
            RegisteredClientRecord responder = registeredClients.get(responderID);


            // sending the requests for the ports to the initiator
            Integer prpPort = null;
            boolean bindPort = false;
            boolean requestNewPorts = false;
            try {
                prpPort = initiator.popPrpPort();
                if (initiator.sizePrpPorts() < 2) {
                    requestNewPorts = true;
                }
            } catch (NoPortsException ex) {
                requestNewPorts = true;
                prpPort = PortSelector.selectRandomPortOver50000();
                bindPort = true;
            }

            Address dummyAddr = new Address(responder.getClient().getIp(),
                    dummyPort, responder.getClient().getId());
            VodAddress dummyVodAddr = ToVodAddr.hpServer(dummyAddr);

            // no request needed - just send the response.
            PRP_ConnectMsg.Response sendDummyMsg =
                    new PRP_ConnectMsg.Response(self.getAddress(),
                    initiator.getClient(),
                    UUID.nextUUID(), // dummy timeoutId
                    PRP_ConnectMsg.ResponseType.OK,
                    session.getResponderID(),
                    dummyVodAddr,
                    prpPort, bindPort,
                    msgTimeoutId);
            delegator.doTrigger(sendDummyMsg, network);
            // inform the other client about the hole that will be opened on the initiators nat
            // making a go message
            Address openInitHole = new Address(initiator.getClient().getIp(),
                    prpPort, initiator.getClient().getId());
            VodAddress openedHoleOnInitiatorsNat = new VodAddress(openInitHole,
                    VodConfig.SYSTEM_OVERLAY_ID, initiator.getClient().getNatPolicy(),
                    initiator.getClient().getParents());

            GoMsg.Request goMessage = new GoMsg.Request(self.getAddress(),
                    responder.getClient(),
                    openedHoleOnInitiatorsNat,
                    HPMechanism.PRP,
                    HPRole.PRP_RESPONDER,
                    NUM_RETRIES_PRP,
                    prpPort,
                    bindPort,
                    msgTimeoutId);
                // oneway msg, so ok to call trigger instead of retry
            delegator.doTrigger(goMessage, network);

            if (requestNewPorts) {
                // ask for more ports from child
                PRP_PreallocatedPortsMsg.Request r = new PRP_PreallocatedPortsMsg.Request(
                        self.getAddress(), initiator.getClient(), msgTimeoutId);
                ScheduleRetryTimeout st = new ScheduleRetryTimeout(2000, 2, 2);
                PRP_PreallocatedPortsMsg.RequestRetryTimeout t =
                        new PRP_PreallocatedPortsMsg.RequestRetryTimeout(st, r);
                delegator.doRetry(t); // retry twice
            }

        } else if (session.getHolePunchingMechanism() == HPMechanism.PRC) {
            // ask the initiator to send two messages. one to zserver and other to some dummy
            // port on the client nat
            int initiatorID = session.getInitiatorID();
            int responderID = session.getResponderID();
            RegisteredClientRecord initiator = registeredClients.get(initiatorID);
            RegisteredClientRecord responder = registeredClients.get(responderID);
            VodAddress initiatorPublicAddress = initiator.getClient();
            VodAddress responderPublicAddress = responder.getClient();



            HPRole initiatorRole = session.getHolePunchingRoleOf(initiatorID);

            Address dummyPublicAddress = new Address(responderPublicAddress.getIp(),
                    dummyPort, responderPublicAddress.getId());

            // TODO - should parents be null in dummy address here?
            VodAddress dummyPublicAddressOfResponder =
                    new VodAddress(dummyPublicAddress, responderPublicAddress.getOverlayId(),
                    responderPublicAddress.getNatPolicy(),
                    null);

            PRC_ServerRequestForConsecutiveMsg.Request requestMsg =
                    new PRC_ServerRequestForConsecutiveMsg.Request(self.getAddress(),
                    initiatorPublicAddress,
                    responderID, hpMechanism, initiatorRole,
                    dummyPublicAddressOfResponder, msgTimeoutId);

            // oneway msg, so ok to call trigger instead of retry
            delegator.doTrigger(
                    requestMsg, network);

        } else if (session.getHolePunchingMechanism() == HPMechanism.PRP_PRP) {
            // ask both the clients to send some available ports
            // making request for client A and B

            Interleaved_PRP_ServerRequestForAvailablePortsMsg.Request reqFor_A =
                    new Interleaved_PRP_ServerRequestForAvailablePortsMsg.Request(self.getAddress(),
                    client_A_PublicAddress,
                    remoteClientFor_A,
                    hpMechanism, client_A_HPRole, msgTimeoutId);

            Interleaved_PRP_ServerRequestForAvailablePortsMsg.Request reqFor_B =
                    new Interleaved_PRP_ServerRequestForAvailablePortsMsg.Request(self.getAddress(), client_B_PublicAddress,
                    remoteClientFor_B,
                    hpMechanism, client_B_HPRole, msgTimeoutId);

            // oneway msgs, so ok to call trigger instead of retry
            delegator.doTrigger(reqFor_A, network);
            delegator.doTrigger(reqFor_B, network);
        } else if (session.getHolePunchingMechanism() == HPMechanism.PRC_PRC) {
            // ask both the clients to send hole punching message to z
            // from that z will predict hole that will be opened in the future
            // making request for client A and B

            Interleaved_PRC_ServersRequestForPredictionMsg.Request reqFor_A =
                    new Interleaved_PRC_ServersRequestForPredictionMsg.Request(self.getAddress(), client_A_PublicAddress,
                    remoteClientFor_A, hpMechanism, client_A_HPRole,
                    client_B_PublicAddress, msgTimeoutId);

            Interleaved_PRC_ServersRequestForPredictionMsg.Request reqFor_B =
                    new Interleaved_PRC_ServersRequestForPredictionMsg.Request(self.getAddress(), client_B_PublicAddress,
                    remoteClientFor_B, hpMechanism, client_B_HPRole,
                    client_A_PublicAddress, msgTimeoutId);
            // As hole-punching is executed in parallel across many different rendezvous servers, 
            // It is possible that A will receive a response from the zServer, while
            // B will receive a response from a different zServer first. 
            // TODO: If A and B receive the msgs from different zServers, does it matter?
            delegator.doTrigger(reqFor_A, network);
            delegator.doTrigger(reqFor_B, network);
        } else if (session.getHolePunchingMechanism() == HPMechanism.PRP_PRC) {
            // only called by PRC client. PRP client sends (instead of HpConnect.Request) a
            // Interleaved_PRP_ConnectMsg.Request

            if (client_A_HPRole == HPRole.PRC_INTERLEAVED
                    && client_B_HPRole == HPRole.PRP_INTERLEAVED) {
                Integer prpPort;
                boolean newPorts = false;
                VodAddress prpAddr = client_B_PublicAddress;
                try {
                    prpPort = client_B_RegRecord.popPrpPort();
                    Address prpClient = new Address(client_B_PublicAddress.getIp(),
                            prpPort, client_B_PublicAddress.getId());
                    prpAddr = new VodAddress(prpClient,
                            client_B_RegRecord.getClient().getId(),
                            client_B_RegRecord.getClient().getNat(),
                            client_B_RegRecord.getClient().getParents());
                    session.set_Interleaved_PRP_Port(client_B_ID, prpPort);
                } catch (NoPortsException ex) {
                    newPorts = true;
                }

                if (newPorts) {
                    // ask for more ports from child
                    PRP_PreallocatedPortsMsg.Request r =
                            new PRP_PreallocatedPortsMsg.Request(self.getAddress(),
                            client_B_RegRecord.getClient(),
                            msgTimeoutId);
                    ScheduleRetryTimeout st = new ScheduleRetryTimeout(2000, 1, 2d);
                    PRP_PreallocatedPortsMsg.RequestRetryTimeout t =
                            new PRP_PreallocatedPortsMsg.RequestRetryTimeout(st, r);
                    delegator.doRetry(t);
                }

                // send GoMsg first, as it would send a 
//                GoMsg.Request goMsgToA = new GoMsg.Request(self.getAddress(), client_A_PublicAddress, 
//                        prpAddr, hpMechanism, client_A_HPRole, 4);
//                delegator.doTrigger(goMsgToA, network);

                Interleaved_PRC_ServersRequestForPredictionMsg.Request reqFor_A =
                        new Interleaved_PRC_ServersRequestForPredictionMsg.Request(self.getAddress(),
                        client_A_PublicAddress,
                        remoteClientFor_A, hpMechanism, client_A_HPRole, prpAddr, msgTimeoutId);
                delegator.doTrigger(reqFor_A, network);
                // do PRP binding from clientA side
//            } else if (client_B_HPRole == HPRole.PRC_INTERLEAVED && client_A_HPRole == HPRole.PRP_INTERLEAVED) {
////                Interleaved_PRC_ServersRequestForPredictionMsg.Request reqFor_B =
////                        new Interleaved_PRC_ServersRequestForPredictionMsg.Request(self.getAddress(), client_B_PublicAddress,
////                        remoteClientFor_B, hpMechanism, client_B_HPRole);
//                delegator.doTrigger(reqFor_B, network);
                // do PRP binding from clientB side
            } else {
                throw new UnsupportedOperationException(compName + "Wrong Wrong PRP and PRC combination. ");
            }

        } else {
            throw new UnsupportedOperationException(compName + "Nat Traversal stratagy not supported.");
        }
    }

    private void printMsg(HpMsg.Hp msg) {
        logger.trace(compName + msg.getClass().getCanonicalName() + " from "
                + msg.getClientId() + " to " + msg.getRemoteClientId() + " - "
                + msg.getMsgTimeoutId());
    }
    Handler<PRP_PreallocatedPortsMsg.Response> handle_PRP_PreallocatedPortsMsgResponse = new Handler<PRP_PreallocatedPortsMsg.Response>() {
        @Override
        public void handle(PRP_PreallocatedPortsMsg.Response response) {
            printMsg(response);

            String compName = RendezvousServer.this.compName + " - "
                    + response.getMsgTimeoutId() + " ";

            if (delegator.doCancelRetry(response.getTimeoutId())) {
                int childId = response.getSource().getId();
                RegisteredClientRecord rc = registeredClients.get(childId);
                if (rc != null) {
                    Set<Integer> prpPorts = response.getPrpPorts();
                    rc.addPrpPorts(prpPorts);
                    logger.trace(compName + "recvd ports from child " + childId + " " + prpPorts);
                    rc.setLastHeardFrom(System.currentTimeMillis());
                }
            }
        }
    };
    Handler<PRP_PreallocatedPortsMsg.RequestRetryTimeout> handle_PRP_PreallocatedPortsMsgTimeout = new Handler<PRP_PreallocatedPortsMsg.RequestRetryTimeout>() {
        @Override
        public void handle(PRP_PreallocatedPortsMsg.RequestRetryTimeout event) {
            if (delegator.doCancelRetry(event.getTimeoutId())) {
                // remove as child - it has probably died
                int childId = event.getRequestMsg().getDestination().getId();
                registeredClients.remove(childId);
                delegator.doRetry(new HpUnregisterMsg.Request(self.getAddress(),
                        event.getRequestMsg().getVodDestination(), 0,
                        HpRegisterMsg.RegisterStatus.PARENT_REQUEST_FAILED));
                logger.warn(compName + " Child didn't allocate requested ports. Disconnecting child.");
            }
        }
    };
    
    @Override
    public void stop(Stop stop) {
        if (garbageCleanupTimeoutId != null) {
            CancelTimeout ct = new CancelTimeout(garbageCleanupTimeoutId);
            delegator.doTrigger(ct, timer);
        }
    }
}
