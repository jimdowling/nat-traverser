/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.client;

import java.util.ArrayList;
import java.util.Collection;
import se.sics.gvod.stun.client.events.StunClientInit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.stun.msgs.EchoChangeIpAndPortMsg;
import se.sics.gvod.stun.msgs.EchoChangePortMsg;
import se.sics.gvod.stun.msgs.EchoMsg;
import se.sics.gvod.stun.msgs.EchoMsg.Response;
import se.sics.gvod.stun.msgs.StunResponseMsg;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.Nat.AllocationPolicy;
import se.sics.gvod.net.Nat.AlternativePortAllocationPolicy;
import se.sics.gvod.net.Nat.FilteringPolicy;
import se.sics.gvod.net.Nat.MappingPolicy;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.stun.client.events.GetNatTypeRequest;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.RTTStore;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.gvod.stun.client.events.GetNatTypeResponseRuleExpirationTime;
import se.sics.gvod.stun.client.events.StunPortAllocResponse;
import se.sics.gvod.stun.client.events.RequestServerHeartBeatTimer;
import se.sics.gvod.stun.client.events.UpnpTimeout;
import se.sics.gvod.stun.upnp.ForwardPortStatus;
import se.sics.gvod.stun.upnp.UpnpComponent;
import se.sics.gvod.stun.upnp.UpnpPort;
import se.sics.gvod.stun.upnp.events.MapPortRequest;
import se.sics.gvod.stun.upnp.events.MapPortsRequest;
import se.sics.gvod.stun.upnp.events.MapPortsResponse;
import se.sics.gvod.stun.upnp.events.MappedPortsChanged;
import se.sics.gvod.stun.upnp.events.ShutdownUpnp;
import se.sics.gvod.stun.upnp.events.UpnpGetPublicIpRequest;
import se.sics.gvod.stun.upnp.events.UpnpGetPublicIpResponse;
import se.sics.gvod.stun.upnp.events.UpnpInit;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.UUID;
import se.sics.kompics.Component;

/**
 *
 * @author jdowling, salman
 *
 * algorithm used in this stun client is described in detail in the
 * http://tools.ietf.org/html/draft-takeda-symmetric-nat-traversal-00
 * http://www.rfc-editor.org/rfc/rfc4787.txt
 *
 *
 * SS1 = Stun Server 1 SS2 = Stun Server 2
 *
 * (Client) UDP_BLOCKED -----------------EchoMsg.Req----------------------->SS1
 * | UDP_BLOCKED <-------------(no reply - timeout)------------------| | | Check
 * replyToIp matches | private IP | | | | V V | NAT_UDP_OK OPEN_CHECK_FIREWALL |
 * | | UDP_WORKS, <---------------(EchoMsg.Resp)------------------------|
 * SS2_FAILED | | ----------EchoChangeIpAndPort.Req----------------------->SS1 |
 * ServerHostChangeMsg.Req | SS2_FAILED <------EchoChangeIpAndPort.Resp(Failed
 * SS2)-----(If not Ack'd at SS1) | V SS2 (port 2) | | CHANGE_IP_TIMEOUT
 * <-------(EchoChangeIpAndPort.Resp not revd)----| | | CHANGED_IP,
 * <------(EchoChangeIpAndPort.Resp recvd)---------------| CHANGE_IP_TIMEOUT | |
 * |---------------EchoChangePort.Req-----------------------> SS1 (port 1) |
 * CHANGE_PORT_TIMEOUT <-------(EchoChangePort.Resp not revd)-------| |
 * CHANGED_PORT <------(EchoChangeIpAndPort.Resp recvd)--------------|
 *
 * FIN_ means that the other branch has finished.
 *
 * CHANGE_IP_TIMEOUT_FIN_PORT, CHANGED_IP_FIN_PORT, CHANGED_PORT_FIN_IP,
 * CHANGED_PORT_TIMEOUT_FIN_IP | Allocate ports 2, 3 on Stun Client | (Port 2)
 * --------- EchoMsg.Req (Try-0)----------------------)-> SS1 (Port 3) ---------
 * EchoMsg.Req (Try-1)----------------------)-> SS1 (Port 2) ---------
 * EchoMsg.Req (Try-2)----------------------)-> SS1 (port 2) (Port 3) ---------
 * EchoMsg.Req (Try-3)----------------------)-> SS1 (port 2) (Port 2) ---------
 * EchoMsg.Req (Try-4)----------------------)-> SS2 (Port 3) ---------
 * EchoMsg.Req (Try-5)----------------------)-> SS2 (Port 2) ---------
 * EchoMsg.Req (Try-6)----------------------)-> SS2 (port 2) (Port 3) ---------
 * EchoMsg.Req (Try-7)----------------------)-> SS2 (port 2) | | | PING_FAILED
 * <------(EchoMsg.ReqTimeout Ping)--------------------| <------(EchoMsg.Req
 * Ping Received all 8)--------------------|
 *
 * For info on expected UDP Nat binding timeouts, see :
 * http://www.ietf.org/proceedings/78/slides/behave-8.pdf From these slides, we
 * measure UDP-2, but a NAT will refresh with UDP-1. Therefore, we need to be
 * conservative in setting the NAT binding timeout.
 *
 */
public class StunClient extends MsgRetryComponent {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final static int NUM_PING_TRIES = 8;
    private Negative<StunPort> stunPort = negative(StunPort.class);
    private Positive<NatNetworkControl> natNetworkControl = positive(NatNetworkControl.class);
    private Component upnp;
    private Set<TimeoutId> upnpRequests = new HashSet<TimeoutId>();
    //Each run initials
    private Set<Address> initialServers = new HashSet<Address>();
    // Exclusive lists to keep track of servers
    private List<Address> test2HeldbackServers = new ArrayList<Address>();
    // TODO - override equals in EchoMsg.Response!
    private List<EchoMsg.Response> test2HoldbackResponses = new ArrayList<EchoMsg.Response>();
    // A black list populated from different places inside different tests of all the runs
    private Set<Address> failedHosts = new HashSet<Address>();
    //Roundtrip measurement stuff
    private Map<Address, Long> echoTimestamps = new HashMap<Address, Long>();
    private Map<Address, RoundTripTime> echoRtts = new HashMap<Address, RoundTripTime>();
    private Self self;
    private Map<Long, Session> sessionMap = new HashMap<Long, Session>();
    private Map<Address, Long> transactionMap = new HashMap<Address, Long>();
    private Set<Address> echoTimeoutedServers = new HashSet<Address>();
    private Random random;
    private StunClientConfiguration config;
    private String compName;
    private boolean upnpStun = false;
    private boolean openIp = false;
    private boolean test1Finished = false;
    private boolean measureNatBindingTimeout = false;
    private boolean ongoing = false;

    private class TriesPair {

        public int a, b;

        TriesPair(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    private class StartUpnp extends Timeout {

        public StartUpnp(ScheduleTimeout st) {
            super(st);
        }
    }

    public StunClient() {
        this(null);
    }

    StunClient(RetryComponentDelegator delegator) {
        super(delegator);

        upnp = create(UpnpComponent.class);

        this.delegator.doSubscribe(handleEchoChangeIpAndPortTimeout, timer);
        this.delegator.doSubscribe(handleEchoChangePortTimeout, timer);
        this.delegator.doSubscribe(handleEchoTimeout, timer);
        this.delegator.doSubscribe(handleRequestServerHeartBeatTimer, timer);
        this.delegator.doSubscribe(handleUpnpTimeout, timer);
        this.delegator.doSubscribe(handleStartUpnp, timer);

        this.delegator.doSubscribe(handleEchoChangeIpAndPortResponse, network);
        this.delegator.doSubscribe(handleEchoChangePortResponse, network);
        this.delegator.doSubscribe(handleEchoResponse, network);

        this.delegator.doSubscribe(handlePortAllocResponse, natNetworkControl);

        this.delegator.doSubscribe(handleMappedPortsChanged, upnp.getPositive(UpnpPort.class));
        this.delegator.doSubscribe(handleMapPortsResponse, upnp.getPositive(UpnpPort.class));
        this.delegator.doSubscribe(handleUpnpGetPublicIpResponse, upnp.getPositive(UpnpPort.class));


        this.delegator.doSubscribe(handleGetNatTypeRequest, stunPort);
        this.delegator.doSubscribe(handleInit, control);
        // handler in super class
        this.delegator.doSubscribe(handleRTO, timer);
    }
    Handler<StunClientInit> handleInit = new Handler<StunClientInit>() {
        @Override
        public void handle(StunClientInit init) {

            self = init.getSelf();
            config = init.getConfig();
            compName = "(" + self.getId() + ")";

            random = new Random(init.getSeed() + self.getId());

            sessionMap.clear();
            logger.trace(compName
                    + " local address is " + self.getId() + "@" + self.getIp() + ":" + self.getPort()
                    + " retry delay is " + config.getRto()
                    + " ruleExpirationMinWait " + config.getRuleExpirationMinWait()
                    + " ruleExpirationIncrement " + config.getRuleExpirationIncrement());

            if (config.isUpnpEnable()) {
                delegator.doTrigger(new UpnpInit(), upnp.getControl());
            }
        }
    };
    Handler<GetNatTypeRequest> handleGetNatTypeRequest = new Handler<GetNatTypeRequest>() {
        @Override
        public void handle(GetNatTypeRequest event) {
            logger.debug(compName + "handleGetNatTypeRequest, Stun server Addresses are " + printServers(event.getStunServerAddresses()));
            if (event.getTimeout() < 0) {
                throw new IllegalArgumentException("Cannot set stun msgRetryTimeout to a negative value");
            }
            if (ongoing) {
                sendResponse(GetNatTypeResponse.Status.ONGOING);
                return;
            }

            ongoing = true;
            //cleans out craps of the previous rounds
            initialServers.clear();
            initialServers = event.getStunServerAddresses();
            initialServers.removeAll(failedHosts);
            test2HeldbackServers.clear();
            test2HoldbackResponses.clear();

            echoTimestamps.clear();
            echoRtts.clear();
            echoTimeoutedServers.clear();
            transactionMap.clear();
            openIp = false;
            test1Finished = false;
            measureNatBindingTimeout = event.isMeasureNatBindingTimeout();
            sessionMap.clear();

            if (config.isUpnpEnable()) {
                startUpnp();
            } else {
                startEcho();
            }

        }
    };

    private String printServers(Collection<Address> servers) {
        StringBuilder sb = new StringBuilder();
        for (Address address : servers) {
            sb.append(address.toString()).append(", ");
        }
        return sb.toString();
    }

    private void startEcho() {
        //
        // TEST 1
        //
        if (initialServers.isEmpty()) {
            sendResponse(GetNatTypeResponse.Status.NO_SERVER);
            return;
        }

        // Even and Odd Ids:
        // If a 
        boolean evenId = false, oddId = false;
        for (Address serverAddress : initialServers) {
            if (serverAddress.getId() % 2 == 0) {
                if (evenId) {
                    continue;
                }
                evenId = true;
            } else if (serverAddress.getId() % 2 == 1) {
                if (oddId) {
                    continue;
                }
            }
            oddId = true;
            long transactionId = random.nextLong();
            VodAddress sVodAddr = ToVodAddr.stunServer(serverAddress);
            Session session = new Session(transactionId,
                    self.getAddress().getPeerAddress(), sVodAddr.getPeerAddress(),
                    measureNatBindingTimeout);
            sessionMap.put(transactionId, session);
            transactionMap.put(serverAddress, transactionId);
            echoTimestamps.put(serverAddress, System.currentTimeMillis());
            sendEchoRequest(ToVodAddr.stunServer(serverAddress), EchoMsg.Test.UDP_BLOCKED, transactionId);
        }
    }
    
private void sendEchoRequest(VodAddress target, EchoMsg.Test testType, long transactionId) {
        int rto = calculateRto(target.getPeerAddress(), 0);

        EchoMsg.Request bindingReq = new EchoMsg.Request(self.getAddress(),
                target, testType, transactionId);
        ScheduleRetryTimeout st =
                new ScheduleRetryTimeout(rto, config.getRtoRetries(),
                config.getRtoScale());
        EchoMsg.RequestRetryTimeout requestRetryTimeout =
                new EchoMsg.RequestRetryTimeout(st, bindingReq);
        delegator.doRetry(requestRetryTimeout);
        logger.debug(compName + "Sending " + testType + " from "
                + self.getAddress() + " to" + target.getPeerAddress() + " . Rto="
                + config.getRto() + " retries=" + config.getRtoRetries() + " tid: "
                + transactionId);
    }

    private int calculateRto(Address dest, long additional) {
        RoundTripTime echoRtt = echoRtts.get(dest);
        long client2ServerRtt = (echoRtt != null) ? echoRtt.getRtt() : config.getRto();
        return (int) ((client2ServerRtt * 1.5) + (additional)
                + config.getMinimumRtt());
    }

    private void sendEchoChangeIpAndPortRequest(VodAddress target, long transactionId) {

        Session session = sessionMap.get(transactionId);
        long server2PartnerRto = session.getBestPartnerRtt();
        int rto = calculateRto(target.getPeerAddress(),
                server2PartnerRto * VodConfig.STUN_PARTNER_RTO_MULTIPLIER
                + config.getMinimumRtt());
        logger.debug(compName + "sendEchoChangeIpAndPortRequest " + " Rto=" + rto + " - "
                + transactionId);
        EchoChangeIpAndPortMsg.Request echoChangeIpReq = new EchoChangeIpAndPortMsg.Request(
                self.getAddress(), target, transactionId);
        ScheduleRetryTimeout st =
                new ScheduleRetryTimeout(rto, config.getRtoRetries(),
                config.getRtoScale());
        EchoChangeIpAndPortMsg.RequestRetryTimeout requestMsg =
                new EchoChangeIpAndPortMsg.RequestRetryTimeout(st, echoChangeIpReq);
        delegator.doRetry(requestMsg);

        logger.debug(compName + "Sending EchoChangeIpandPort from :"
                + self.getAddress() + " to" + target.getIp()
                + " rto= " + rto + " retries= " + config.getRtoRetries()
                + " tid: " + transactionId);

    }

    private void sendEchoChangePortRequest(VodAddress target, long transactionId) {

        EchoChangePortMsg.Request echoChangePortReq = new EchoChangePortMsg.Request(
                self.getAddress(), target, transactionId);
        RoundTripTime echoRtt = echoRtts.get(target.getPeerAddress());
        long rto = calculateRto(target.getPeerAddress(), 0);
        ScheduleRetryTimeout st =
                new ScheduleRetryTimeout(rto, config.getRtoRetries(),
                config.getRtoScale());
        delegator.doRetry(new EchoChangePortMsg.RequestRetryTimeout(st, echoChangePortReq));

        logger.debug(compName + "Sending EchoChangePort:"
                + target.getPeerAddress() + " Rto= " + rto
                + " retries=" + config.getRtoRetries() + " tid: " + transactionId);
    }

    private void testIfFinished(Session session, long transactionId) {

        logger.trace(compName + "All tries are ");
        for (int i = 0; i < session.getTotalTryMessagesReceived(); i++) {
            logger.trace(compName + "Try-" + i + " " + session.getTry(i).getPort() + " tid: "
                    + transactionId);
        }

        // now the holes been punched. determining the rule timeout
        // algo: http://tools.ietf.org/html/draft-ietf-behave-nat-behavior-discovery-07#page-11
        session.setRuleDeterminationStartTime(System.currentTimeMillis());
        session.setRuleLifeTime(config.getRuleExpirationMinWait());

        // first determining the mapping policy
        determineMappingPolicy(session);
        if (!determineAllocationPolicy(session)) {
            // TODO - should send back response here? Retry?
            logger.warn(compName + "Could not determine Allocation policy!!!" + " tid: "
                    + transactionId);
        }

        if (session.isFinishedAllocation() && session.isFinishedMapping() && session.isFinishedFilter()) {
            sendResponse(session, GetNatTypeResponse.Status.SUCCEED);
        }
    }

    private void startHeartBeatRequestTimer(long transactionId) {
        Session session = sessionMap.get(transactionId);
        logger.debug(compName + "starting the timer to determine the rule expiration timer. timer sec: " + (session.getRuleLifeTime()) / 1000
                + " tid: " + transactionId);
        ScheduleTimeout st = new ScheduleTimeout(session.getRuleLifeTime());
        st.setTimeoutEvent(new RequestServerHeartBeatTimer(st, transactionId));
        delegator.doTrigger(st, timer);
    }
    Handler<RequestServerHeartBeatTimer> handleRequestServerHeartBeatTimer = new Handler<RequestServerHeartBeatTimer>() {
        @Override
        public void handle(RequestServerHeartBeatTimer event) {
            long transactionId = event.getTransactionId();
            logger.debug(compName + "sending heartbeat to the server" + " tid: "
                    + transactionId);
            Session session = sessionMap.get(transactionId);
            sendServerHeartBeatRequest(self.getAddress(), session.getServer1(), session.getTry(0), transactionId);
        }
    };

    private void sendServerHeartBeatRequest(VodAddress self, VodAddress target,
            Address replyTo,
            long transactionId) {
        logger.debug(compName + " sending HB request src " + self.getPeerAddress() + " dest "
                + target.getPeerAddress()
                + " reply to " + replyTo + " tid: " + transactionId);
        EchoMsg.Request hbRequest = new EchoMsg.Request(self, target, EchoMsg.Test.HEARTBEAT,
                transactionId, replyTo);
        ScheduleRetryTimeout st =
                new ScheduleRetryTimeout(config.getRto(), 0);
        EchoMsg.RequestRetryTimeout requestRetryTimeout =
                new EchoMsg.RequestRetryTimeout(st, hbRequest);
        delegator.doRetry(requestRetryTimeout);
    }

    private boolean determineAllocationPolicy(Session session) {
        // first check for the PP coz alternative policy is difficult to determine
        // not always possible if mapping is EI
        // TODO work on it. i.e. how to determine the alternative allocation policy
        // if the mapping policy is EI

        logger.debug(compName + "First random port == "
                + session.getClientFirstRandomPort() + "  session port(0) == " + session.getTry(0).getPort());

        logger.debug(compName + "Second random port == "
                + session.getClientSecondRandomPort() + "  session port(4) == "
                + session.getTry(4).getPort());

        if (session.getClientFirstRandomPort() == session.getTry(0).getPort()
                || session.getClientSecondRandomPort() == session.getTry(4).getPort()) {

            session.setAllocationPolicy(AllocationPolicy.PORT_PRESERVATION);
            // so the port allocation policy is PP
            // now determining the alternative allocation policy
            // mapping policy has already been determined

            if (session.getMappingPolicy() == Nat.MappingPolicy.ENDPOINT_INDEPENDENT) {
                // TODO work on it. i.e. how to determin the alternative allocation policy
                // if the mapping policy is EI
                // for now just set the allocation policy to PC
                session.setAlternativeAllocationPolicy(AlternativePortAllocationPolicy.PORT_CONTIGUITY);
                session.setFinishedAllocation(true);
                return true;
            } else if (session.getMappingPolicy() == Nat.MappingPolicy.HOST_DEPENDENT) {
                List<TriesPair> list = new ArrayList<TriesPair>();
                list.add(new TriesPair(2, 6));
                int ret = checkContiguity(list, session);
                if (ret == -1) {
                    session.setAlternativeAllocationPolicy(AlternativePortAllocationPolicy.RANDOM);
                    session.setFinishedAllocation(true);
                    return true;
                } else {
                    session.setAlternativeAllocationPolicy(AlternativePortAllocationPolicy.PORT_CONTIGUITY);
                    session.setDelta(ret);
                    session.setFinishedAllocation(true);
                    return true;
                }
            } else if (session.getMappingPolicy() == Nat.MappingPolicy.PORT_DEPENDENT) {
                List<TriesPair> list = new ArrayList<TriesPair>();
                list.add(new TriesPair(1, 2));
                list.add(new TriesPair(2, 3));
                list.add(new TriesPair(5, 6));
                list.add(new TriesPair(6, 7));
                int ret = checkContiguity(list, session);
                if (ret == -1) {
                    session.setAlternativeAllocationPolicy(AlternativePortAllocationPolicy.RANDOM);
                    session.setFinishedAllocation(true);
                    return true;
                } else {
                    session.setAlternativeAllocationPolicy(AlternativePortAllocationPolicy.PORT_CONTIGUITY);
                    session.setDelta(ret);
                    session.setFinishedAllocation(true);
                    return true;
                }
            } else {
                return false;
            }
        } else if (session.getMappingPolicy() == Nat.MappingPolicy.ENDPOINT_INDEPENDENT) {
            List<TriesPair> list = new ArrayList<TriesPair>();
            list.add(new TriesPair(0, 4));
            int ret = checkContiguity(list, session);
            if (ret == -1) {
                session.setAllocationPolicy(AllocationPolicy.RANDOM);
            } else {
                session.setAllocationPolicy(AllocationPolicy.PORT_CONTIGUITY);
                session.setDelta(ret);
            }
            session.setFinishedAllocation(true);
            return true;
        } else if (session.getMappingPolicy() == Nat.MappingPolicy.HOST_DEPENDENT) {
            List<TriesPair> list = new ArrayList<TriesPair>();
            list.add(new TriesPair(0, 2));
            list.add(new TriesPair(3, 4));
            list.add(new TriesPair(5, 6));
            int ret = checkContiguity(list, session);
            if (ret == -1) {
                session.setAllocationPolicy(AllocationPolicy.RANDOM);
                session.setFinishedAllocation(true);
                return true;
            } else {
                session.setAllocationPolicy(AllocationPolicy.PORT_CONTIGUITY);
                session.setDelta(ret);
                session.setFinishedAllocation(true);
                return true;
            }
        } else if (session.getMappingPolicy() == Nat.MappingPolicy.PORT_DEPENDENT) {
            List<TriesPair> list = new ArrayList<TriesPair>();
            list.add(new TriesPair(0, 1));
            list.add(new TriesPair(1, 2));
            list.add(new TriesPair(2, 3));
            list.add(new TriesPair(3, 4));
            list.add(new TriesPair(4, 5));
            list.add(new TriesPair(5, 6));
            list.add(new TriesPair(6, 7));
            int ret = checkContiguity(list, session);
            if (ret == -1) {
                session.setAllocationPolicy(AllocationPolicy.RANDOM);
                session.setFinishedAllocation(true);
                return true;
            } else {
                session.setAllocationPolicy(AllocationPolicy.PORT_CONTIGUITY);
                session.setDelta(ret);
                session.setFinishedAllocation(true);
                return true;
            }
        } else {
            return false;
        }
    }

    // return -1 if the port allocation policy is random
    // otherwise return positive number that is the delta i.e. port increment number
    private int checkContiguity(List<TriesPair> list, Session session) {
        // set minDelta to be an arbitrarily high number. it will be decreased to
        // the minDelta observed over all TriesPairs.
        int minDelta = 1000;
        for (int i = 0; i < list.size(); i++) {
            TriesPair tPair = list.get(i);
            int localPort = session.getTry(tPair.a).getPort();
            int natPort = session.getTry(tPair.b).getPort();
            int difference = Math.abs(localPort - natPort);
            logger.trace(compName + "Port allocation - local/nat ports: {}/{}", localPort, natPort);
            if (difference > config.getRandTolerance()) // Assume it is a Random Port Allocation Policy if true
            {
                session.setDelta(1);
                return -1;
            } else {
                if (difference < minDelta) {
                    minDelta = difference;
                }
            }
        }
        session.setDelta(minDelta);
        return minDelta;
    }

    private void determineMappingPolicy(Session session) {
        // if try 1 to 4 have same public address then the mapping is EI
        Address a0 = session.getTry(0);
        Address a1 = session.getTry(1);
        Address a2 = session.getTry(2);
        Address a3 = session.getTry(3);
        logger.debug(compName + "Determining mapping policy with tries: "
                + a0 + " :: "
                + a1 + " :: "
                + a2 + " :: "
                + a3 + " :: " + " tid: " + session.getTransactionId());

        if (a0.equals(a1) && a0.equals(a2) && a0.equals(a3)) {
            session.setMappingPolicy(MappingPolicy.ENDPOINT_INDEPENDENT);
        } // if try 1 and try 2 are same and try 3 and try 4 are same then
        // the mapping is Host Dependent
        // TODO - this isn't working. HD mapping policy is very rare. Ignore for now.
        else if (a0.equals(a1) && a2.equals(a3) && !a0.equals(a2)) {
            session.setMappingPolicy(MappingPolicy.HOST_DEPENDENT);
        } // try 1 to try 4 are all different then mapping policy is
        else {
            session.setMappingPolicy(MappingPolicy.PORT_DEPENDENT);
        }
        session.setFinishedMapping(true);
    }

    private void determineMappingAndAllocationPolicies(long transactionId) {
        logger.debug(compName + "Sending PortAllocRequest for two new ports on client...");
        PortAllocRequest allocReq = new PortAllocRequest(self.getId(), 2);
        StunPortAllocResponse allocResp = new StunPortAllocResponse(allocReq, transactionId);
        allocReq.setResponse(allocResp);
        delegator.doTrigger(allocReq, natNetworkControl);
    }
    Handler<StunPortAllocResponse> handlePortAllocResponse =
            new Handler<StunPortAllocResponse>() {
        @Override
        public void handle(StunPortAllocResponse response) {
            logger.debug(compName + "Received two new ports on client...");

            long transactionId = (Long) response.getKey();

            if (response.getAllocatedPorts().size() != 2) {
                throw new IllegalStateException("ERROR: port allocator "
                        + " returned wrong number of ports. expecting 2"
                        + " returned " + response.getAllocatedPorts().size());
            }
            Iterator<Integer> iter = response.getAllocatedPorts().iterator();
            int firstRandPort = iter.next();
            int secondRandPort = iter.next();

            logger.debug(compName + " handle port allocation response. "
                    + " ports in use " + firstRandPort + ", " + secondRandPort);


            // See the NatCracker paper for more details.
            // Try 0 to Try 7 to four addresses
            Session session = sessionMap.get(transactionId);
            VodAddress serverS1Address = session.getServer1();
            VodAddress serverS2Address = ToVodAddr.stunServer(
                    session.getPartnerServer().getPeerAddress());
            int s1AlternativePort = VodConfig.DEFAULT_STUN_PORT_2;
//                    session.getServer1Port2();

            VodAddress serverS1AddressPrime = 
                    ToVodAddr.stunServer2(new Address(serverS1Address.getIp(),
                    s1AlternativePort, serverS1Address.getId()));
            VodAddress serverS2AddressPrime = ToVodAddr.stunServer2(
                    new Address(serverS2Address.getIp(),
                    s1AlternativePort, serverS2Address.getId()));

            session.setClientFirstRandomPort(firstRandPort);
            session.setClientSecondRandomPort(secondRandPort);

            // make two source Addressses
            VodAddress sourceAddress1 = ToVodAddr.stunClient(new Address(self.getIp(),
                    firstRandPort, self.getId()));
            VodAddress sourceAddress2 = ToVodAddr.stunClient(new Address(self.getIp(),
                    secondRandPort, self.getId()));

            // send NUM_PING_TRIES EchoMsg.Reqeuest for diferent source ports.
            int tryId = 0;

            sendPingRequest(sourceAddress1, serverS1Address, transactionId, tryId++);
            sendPingRequest(sourceAddress1, serverS1AddressPrime, transactionId, tryId++);
            sendPingRequest(sourceAddress1, serverS2Address, transactionId, tryId++);
            sendPingRequest(sourceAddress1, serverS2AddressPrime, transactionId, tryId++);
            sendPingRequest(sourceAddress2, serverS1Address, transactionId, tryId++);
            sendPingRequest(sourceAddress2, serverS1AddressPrime, transactionId, tryId++);
            sendPingRequest(sourceAddress2, serverS2Address, transactionId, tryId++);
            sendPingRequest(sourceAddress2, serverS2AddressPrime, transactionId, tryId++);

        }
    };

    public void sendRuleTimoutValue(long transactionId, long ruleTimeoutVal) {
        delegator.doTrigger(new GetNatTypeResponseRuleExpirationTime(ruleTimeoutVal), stunPort);
        logger.debug(compName + " Removing session: " + transactionId);
        sessionMap.remove(transactionId);
    }

    private void printMsgDetails(StunResponseMsg message) {
        logger.trace(compName + " - "
                + message.getClass().getCanonicalName()
                + ": "
                + " ; Public src: " + message.getSource()
                + " ; Private src: " + message.getReplyPublicAddr()
                + " ; Private dest: " + message.getDestination()
                + " ; TimeoutId = " + message.getTimeoutId()
                + " ; transactionId = " + message.getTransactionId());
    }

    private void sendPingRequest(VodAddress source, VodAddress dest,
            long transactionId, int tryId) {
        EchoMsg.Request pingReq =
                new EchoMsg.Request(source, dest, EchoMsg.Test.PING, transactionId);
        pingReq.setTryId(tryId);

        int rto = calculateRto(dest.getPeerAddress(), 0);
        ScheduleRetryTimeout st =
                new ScheduleRetryTimeout(rto,
                config.getRtoRetries(), config.getRtoScale());

        EchoMsg.RequestRetryTimeout requestRetryTimeout =
                new EchoMsg.RequestRetryTimeout(st, pingReq);

        delegator.doRetry(requestRetryTimeout);
        logger.debug(compName + "Sending Echo Ping " + tryId
                + " to " + dest.getPeerAddress()
                + " from " + source 
                + " Rto=" + rto
                + " tid: " + transactionId);
    }

    private void storeSample(Address serverAddress, long rtt) {
        if (rtt > 5000) { // set a max RTT to be 5 seconds
            rtt = 5 * 1000;
        }
        // Store the RTT as a DEFAULT_PORT sample, so that it can be used by ParentMaker
        RTTStore.addSample(self.getId(), ToVodAddr.hpServer(serverAddress), rtt);
        RoundTripTime pt = new RoundTripTime(rtt, serverAddress);
        echoRtts.put(serverAddress, pt);

    }
    Handler<EchoMsg.Response> handleEchoResponse = new Handler<EchoMsg.Response>() {
        @Override
        public void handle(EchoMsg.Response event) {
            printMsgDetails(event);
            long transactionId = event.getTransactionId();
            if (delegator.doCancelRetry(event.getTimeoutId())) {
                logger.debug(compName + " EchoMsg.Response Recvd - timeoutId = "
                        + event.getTimeoutId() + " tid: " + transactionId);
                Session session = sessionMap.get(transactionId);
                Address serverAddress = event.getSource();

                if (event.getTestType() == EchoMsg.Test.UDP_BLOCKED) {
                    Long echoTs = echoTimestamps.get(serverAddress);
                    if (echoTs != null) {
                        logger.trace("RTT sample for " + serverAddress + " was {}/{}",
                                System.currentTimeMillis(), echoTs);
                        storeSample(serverAddress, System.currentTimeMillis() - echoTs);
                    } else {
                        logger.warn("RTT was null from " + serverAddress + " Setting it to 5 seconds.");
                        storeSample(serverAddress, 5 * 1000);
                    }
                    session.setState(SessionState.MAPPING_ALLOCATION);

                    Stack<Address> partners = new Stack<Address>();
                    partners.addAll(event.getPartners());
                    int bestRto = event.getBestPartnerRto();

                    // if i have already contacted a node - then don't use it as 2nd server 
                    // because a binding has already been created for it in my NAT
                    for (Address server : initialServers) {
                        if (partners.contains(server)) {
                            partners.remove(server);
                        }
                    }

                    if (partners.isEmpty()) {
                        manageHostFailure(session, serverAddress,
                                GetNatTypeResponse.Status.SECOND_SERVER_FAILED);
                        return;
                    } else {
                        session.setPartnerServers(partners, bestRto);
                    }

                    if (test1Finished) {
                        if (!test2HeldbackServers.contains(serverAddress)) {
                            test2HeldbackServers.add(serverAddress);
                            test2HoldbackResponses.add(event);
                            logger.debug(compName + " server " + serverAddress + " was held back.");
                        }
                    } else {
                        test1Finished = true;
                        startTest2(event);
                    }

                } else if (event.getTestType() == EchoMsg.Test.PING) {
                    int tryId = event.getTryId();
                    session.setTry(tryId, event.getReplyPublicAddr());
                    logger.debug(compName + " received Try-" + tryId + " from "
                            + event.getReplyPublicAddr()
                            //                            .getId()
                            + ". Total received tries: " + session.getTotalTryMessagesReceived()
                            + " tid: " + transactionId);
                    if (session.getTotalTryMessagesFinished() == NUM_PING_TRIES) {
                        if (session.getTotalTryMessagesReceived() != NUM_PING_TRIES) {
                            manageTest2Failure(session, session.getServer1().getPeerAddress());
                        } else if (session.getState() != SessionState.FINISHED) {
                            // TODO - Do not just wait for other responses 
                            // don't need to do anything - when branch 1 finishes, it responds.
                            testIfFinished(session, transactionId);
                        } else {
                            logger.debug(compName + "Duplicate PING EchoMsg.Response received "
                                    + " tid: " + transactionId);
                        }
                    }
                } else if (event.getTestType() == EchoMsg.Test.HEARTBEAT) {
                    // receaived heart beat fromt ther server --> rule is still valid.
                    // request again for the heart beat
                    session.setRuleLifeTime(session.getRuleLifeTime()
                            + config.getRuleExpirationIncrement());
                    startHeartBeatRequestTimer(transactionId);
                } else if (event.getTestType() == EchoMsg.Test.FAILED_NO_PARTNER) {
                    sendResponse(session, GetNatTypeResponse.Status.SECOND_SERVER_FAILED);
                }
            } else {
                logger.debug(compName + "EchoMsg.Response rcvd late. Flag: "
                        + event.getTestType().toString() + " tid: " + transactionId);
            }
        }
    };
    /**
     * Echo request is not returned to the client. Have retried sending this
     * message, and after all retries, this event handler gets called.
     */
    Handler<EchoMsg.RequestRetryTimeout> handleEchoTimeout =
            new Handler<EchoMsg.RequestRetryTimeout>() {
        @Override
        public void handle(EchoMsg.RequestRetryTimeout event) {
            long transactionId = event.getRequestMsg().getTransactionId();
            logger.debug(compName + " handled EchoMsg.RequestRetryTimeout "
                    + event.getRequestMsg().getTestType().toString()
                    + "to " + event.getRequestMsg().getDestination()
                    + " tid: " + transactionId);

            if (delegator.doCancelRetry(event.getTimeoutId())) {
                Session session = sessionMap.get(transactionId);
                EchoMsg.Test testType = event.getRequestMsg().getTestType();

                if (testType == EchoMsg.Test.UDP_BLOCKED) {
                    VodAddress server = session.getServer1();
                    echoTimeoutedServers.add(server.getPeerAddress());
                    echoTimestamps.remove(server.getPeerAddress());
                    manageHostFailure(session, server.getPeerAddress(), GetNatTypeResponse.Status.FIRST_SERVER_FAILED);
                } else if (testType == EchoMsg.Test.PING) {
                    logger.debug(compName + "FAILED: EchoMsg.Test.PING response failed for Try-ID "
                            + event.getRequestMsg().getTryId() + " tid: " + transactionId);
                    // server or server' has failed while executing STUN.
                    session.setTry(event.getRequestMsg().getTryId(), null);
                    if (session.getTotalTryMessagesFinished() == NUM_PING_TRIES) {
                        manageTest2Failure(session, session.getServer1().getPeerAddress());
                    }
                } else if (testType == EchoMsg.Test.HEARTBEAT) {
                    long ruleTimeout = System.currentTimeMillis() - session.getRuleDeterminationStartTime()
                            - config.getRuleExpirationIncrement();
                    session.setRuleLifeTime(ruleTimeout);
                    sendRuleTimoutValue(transactionId, ruleTimeout);

                }
            } else {
                logger.debug(compName + "StunClient: cancelRetry FAILED. EchoMsg.RequestRetry "
                        + "rcvd late. Flag: " + event.getRequestMsg().getTestType()
                        + " tid: " + transactionId);
            }
        }
    };

    private void manageTest2Failure(Session session, Address server) {
        //No server remains that is progressing test II or III, 
        // so dequeue a heldback server and let it to progress        
        test1Finished = false;
        if (!test2HeldbackServers.isEmpty() && !test2HoldbackResponses.isEmpty()) {
            test2HeldbackServers.remove(0);
            Response response = test2HoldbackResponses.get(0);
            test2HoldbackResponses.remove(0);
            startTest2(response);
        }
        manageHostFailure(session, server, GetNatTypeResponse.Status.ALL_HOSTS_TIMED_OUT);
    }

    private void manageHostFailure(Session session, Address server, GetNatTypeResponse.Status status) {
        failedHosts.add(server);
        if (echoTimeoutedServers.containsAll(initialServers)) {
            logger.debug(compName + " All hosts have timed-out stun");
        } else if (failedHosts.containsAll(initialServers)) {
            logger.debug(compName + " All hosts are identified as failed");
        }
        sendResponse(session, status);
    }

    private void startTest2(EchoMsg.Response event) {
        long transactionId = event.getTransactionId();
        Session session = sessionMap.get(transactionId);
        Address publicAddr = event.getReplyPublicAddr();
        Address privateAddr = session.getPrivateAddress();

        if (publicAddr.equals(privateAddr)) {
            session.setFilterState(FilterState.OPEN_CHECK_FIREWALL);
            // OPEN-IP
            // now, check there is no firewall filtering incoming 
            // echo from another host/port.


            // don't need to check for these, node is open.
            session.setMapAllocState(MappingAllocState.FINISHED);
            openIp = true;
            sendEchoChangeIpAndPortRequest(session.getServer1(), session.getTransactionId());
        } else {
            session.setFilterState(FilterState.NAT_UDP_OK);
            session.setServer1Port2(event.getPortChange());
            // save the public address received. will be used later on
            session.setPublicAddrTest1(event.getReplyPublicAddr());
            // get first S2 server
            if (event.getReplyPublicAddr().equals(self.getAddress().getPeerAddress())) {
                logger.debug(compName + " Wow! NAT == SELF " + self.getAddress());
            }

            logger.debug(compName + "Alternative Server is " + session.getPartnerServer());

            sendEchoChangeIpAndPortRequest(session.getServer1(), session.getTransactionId());
        }

    }
    Handler<EchoChangeIpAndPortMsg.Response> handleEchoChangeIpAndPortResponse =
            new Handler<EchoChangeIpAndPortMsg.Response>() {
        @Override
        public void handle(EchoChangeIpAndPortMsg.Response event) {
            printMsgDetails(event);

            // ignore duplicate responses..
            if (delegator.doCancelRetry(event.getTimeoutId())) {
                long transactionId = event.getTransactionId();
                Session session = sessionMap.get(transactionId);
                VodAddress server1 = session.getServer1();

                logger.debug(compName + "StunClient: EchoChangeIpandPort.ResponseMsg received. from " + event.getSource());

                if (event.getStatus() == EchoChangeIpAndPortMsg.Response.Status.FAIL) {
                    logger.debug(compName + " Server " + server1 + " failed because of no remaining alive partner");
                    manageTest2Failure(session, server1.getPeerAddress());
                    return;
                }

                // TEST II (check for firewall) succeed. Open IP.
                if (openIp) {
                    // open-ip, no firewall
                    sendResponse(session, GetNatTypeResponse.Status.SUCCEED);
                } else {
                    // Finished Filtering.
                    session.setFilteringPolicy(FilteringPolicy.ENDPOINT_INDEPENDENT);
                    session.setFinishedFilter(true);

                    session.setFilterState(FilterState.CHANGE_IP_RECVD);

                    determineMappingAndAllocationPolicies(transactionId);

                    if (session.isFinishedAllocation() && session.isFinishedMapping()) {
                        sendResponse(session, GetNatTypeResponse.Status.SUCCEED);
                    } else {
                        logger.debug(compName + "Not finished all branches yet. "
                                + event.getTransactionId());
                    }
                }
            } else {
                logger.debug(compName + event.getClass().getName() + " Duplicate response..");
            }
        }
    };
    Handler<EchoChangeIpAndPortMsg.RequestRetryTimeout> handleEchoChangeIpAndPortTimeout =
            new Handler<EchoChangeIpAndPortMsg.RequestRetryTimeout>() {
        @Override
        public void handle(EchoChangeIpAndPortMsg.RequestRetryTimeout event) {
            logger.debug(compName + "EchoChangeIpAndPortMsg.Request Timeout "
                    + event.getRequestMsg().getTransactionId());
            if (delegator.doCancelRetry(event.getTimeoutId())) {

                logger.debug(compName + "EchoChangeIpAndPortMsg.Request Cancelled Timeout "
                        + event.getTimeoutId() + " tid: " + event.getRequestMsg().getTransactionId());

                // TEST III
                //
                long transactionId = event.getRequestMsg().getTransactionId();
                Session session = sessionMap.get(transactionId);
                if (session == null) {
                    System.out.println("Missing entry for " + transactionId);
                    for (Long t : sessionMap.keySet()) {
                        System.out.println(t);
                    }
                }
                sendEchoChangePortRequest(session.getServer1(), transactionId);

                // if the node is behind a firewall, we have to set its allocation policy.
                // the EchoChangePortRequest will check its filtering policy
                if (openIp) {
                    // TODO - is this complete??
                    openIp = false;
                    session.setFinishedAllocation(true);
                    session.setAllocationPolicy(AllocationPolicy.PORT_PRESERVATION);
                    session.setFinishedMapping(true);
                    session.setMappingPolicy(MappingPolicy.ENDPOINT_INDEPENDENT);
                }
            } else {
                logger.warn(compName + "Cancel retry EchoChangeIpAndPortMsg failed. Response should have been received."
                        + " tid: " + event.getRequestMsg().getTransactionId());
            }
        }
    };
    Handler<EchoChangePortMsg.Response> handleEchoChangePortResponse =
            new Handler<EchoChangePortMsg.Response>() {
        @Override
        public void handle(EchoChangePortMsg.Response event) {
            printMsgDetails(event);

            if (delegator.doCancelRetry(event.getTimeoutId())) {

                long transactionId = event.getTransactionId();
                determineMappingAndAllocationPolicies(transactionId);

                Session session = sessionMap.get(transactionId);
                session.setFilteringPolicy(FilteringPolicy.HOST_DEPENDENT);
                session.setFinishedFilter(true);
                if (session.isFinishedAllocation() && session.isFinishedMapping()) {
                    sendResponse(session, GetNatTypeResponse.Status.SUCCEED);
                }
            } else {
                logger.debug(compName + "Stun Client EchoChangePort Response. Cancel Retry FAILED"
                        + " tid: " + event.getTransactionId());
            }
        }
    };
    Handler<EchoChangePortMsg.RequestRetryTimeout> handleEchoChangePortTimeout =
            new Handler<EchoChangePortMsg.RequestRetryTimeout>() {
        @Override
        public void handle(EchoChangePortMsg.RequestRetryTimeout event) {

            long transactionId = event.getRequestMsg().getTransactionId();
            logger.debug(compName + "EchoChangePortMsg.Request Timeout "
                    + " tid: " + transactionId);
            if (delegator.doCancelRetry(event.getTimeoutId())) {
                logger.debug(compName + "EchoChangePortMsg.Request Cancelled Timeout "
                        + " tid: " + transactionId);
                determineMappingAndAllocationPolicies(transactionId);

                Session session = sessionMap.get(transactionId);
                session.setFilteringPolicy(FilteringPolicy.PORT_DEPENDENT);
                session.setFinishedFilter(true);
                if (session.isFinishedAllocation() && session.isFinishedMapping()) {
                    sendResponse(session,
                            GetNatTypeResponse.Status.SUCCEED);
                } else {
                    logger.warn(compName + "EchoChangePort: Not finished all branches "
                            + " tid: " + transactionId);
                }
            }

        }
    };

    private void sendResponse(GetNatTypeResponse.Status status) {
        sendResponse(null, status);
    }

    private void sendResponse(Session session, GetNatTypeResponse.Status status) {

        long tid = (session == null) ? -1 : session.getTransactionId();
        long timeTaken = (session == null) ? 0 : (System.currentTimeMillis() - session.getStartTime());
        logger.debug(compName + " sending the nat response . status " + status
                + " tid: " + tid);

        Nat nat;
        if (status == GetNatTypeResponse.Status.SUCCEED) {
            if (openIp == true) {
                nat = new Nat(Nat.Type.OPEN);
            } else if (upnpStun) {
                nat = new Nat(Nat.Type.OPEN);
//                nat = new Nat(Nat.Type.UPNP, publicUPNPAddress,
//                        MappingPolicy.ENDPOINT_INDEPENDENT,
//                        AllocationPolicy.PORT_PRESERVATION,
//                        FilteringPolicy.ENDPOINT_INDEPENDENT);                
            } else if (session != null) {
                nat = new Nat(Nat.Type.NAT, session.getMappingPolicy(), session.getAllocationPolicy(),
                        session.getFilteringPolicy(), session.getDelta(),
                        Nat.DEFAULT_RULE_EXPIRATION_TIME /*rule time determination is in progress. for now set it to default*/);
            } else {
                nat = new Nat(Nat.Type.NAT, null,
                        MappingPolicy.PORT_DEPENDENT,
                        AllocationPolicy.RANDOM,
                        FilteringPolicy.PORT_DEPENDENT);
                status = GetNatTypeResponse.Status.FAIL;
            }

            // If the NAT is already set by UPNP and STUN completes, don't
            // downgrade from OPEN to NAT
            if (self.getNat() != null & !self.isOpen()) {
                self.setNat(nat);
            }
        } else if (status == GetNatTypeResponse.Status.ALL_HOSTS_TIMED_OUT) {
            nat = new Nat(Nat.Type.UDP_BLOCKED);
            self.setNat(nat);
        } else if (status == GetNatTypeResponse.Status.UPNP_STUN_SERVERS_ENABLED) {
            logger.info("UPnP mapped ports to run stun servers");
            return;
        } else {
            nat = new Nat(Nat.Type.NAT,
                    MappingPolicy.PORT_DEPENDENT,
                    AllocationPolicy.RANDOM,
                    FilteringPolicy.PORT_DEPENDENT, 1, 60 * 1000);
        }

        logger.debug(compName + status + ": Nat Type is " + nat.toString());

        delegator.doTrigger(new GetNatTypeResponse(nat, status, null, timeTaken), stunPort);
        ongoing = false;

        if (session != null) {
            if (session.isMeasureNatBindingTimeout()) {
                startHeartBeatRequestTimer(session.getTransactionId());
            } else {
                sessionMap.remove(session.getTransactionId());

            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////             UPNP Stuff              ////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    private void startUpnp() {
        ScheduleTimeout st = new ScheduleTimeout(3000);
        StartUpnp t = new StartUpnp(st);
        st.setTimeoutEvent(t);
        trigger(st, timer);
    }
    Handler<StartUpnp> handleStartUpnp = new Handler<StartUpnp>() {
        @Override
        public void handle(StartUpnp event) {
            sendUpnpMapPortsRequest();
        }
    };

    private void sendUpnpMapPortsRequest() {
        // Send out two requests - GetPublicIpRequest looks for an IGN, it returns quickly
        // if there is no IGN.
        // If there is an IGN, then MapPortResponse will return with a new port, but it's slow
        // to return if there's no IGN.
        Map<Integer, Integer> privatePublicPorts = new HashMap<Integer, Integer>();
        privatePublicPorts.put(self.getPort(), self.getPort());

        TimeoutId requestId = UUID.nextUUID();
        upnpRequests.add(requestId);
        ScheduleTimeout st = new ScheduleTimeout(config.getUpnpTimeout());
        UpnpTimeout timedOutUpnp = new UpnpTimeout(st, requestId);
        st.setTimeoutEvent(timedOutUpnp);
        delegator



.doTrigger(new UpnpGetPublicIpRequest(requestId),
                upnp.getPositive(UpnpPort.class  

    ));

        MapPortRequest.Protocol upnpProtocol = MapPortRequest.Protocol.UDP;
    MapPortsRequest request = new MapPortsRequest(requestId,
            privatePublicPorts, upnpProtocol);

    delegator.doTrigger (request, upnp.getPositive

    

    (UpnpPort.class  

        ));
        delegator.doTrigger (st, timer);
    }

    private void upnpTimedOut(TimeoutId requestId) {
        if (upnpRequests.remove(requestId)) {
            logger.debug(compName + "UPnP Failed, running STUN");
            delegator.doTrigger(new ShutdownUpnp(), upnp.getPositive(UpnpPort.class));
        }
        sendResponse(GetNatTypeResponse.Status.NO_UPNP);
        ongoing = true;
        startEcho();
    }
    Handler<UpnpTimeout> handleUpnpTimeout = new Handler<UpnpTimeout>() {
        @Override
        public void handle(UpnpTimeout event) {
            if (upnpRequests.contains(event.getRequestId())) {
                upnpTimedOut(event.getRequestId());
            }
        }
    };
    Handler<UpnpGetPublicIpResponse> handleUpnpGetPublicIpResponse = new Handler<UpnpGetPublicIpResponse>() {
        @Override
        public void handle(UpnpGetPublicIpResponse event) {
            logger.debug(compName + " UPNP external ip: " + event.getExternalIp());

            // If there's no UPnP IGN, then this should return first.
            if (event.getExternalIp() == null) {

                upnpTimedOut(event.getRequestId());
            } else {
                logger.debug(compName + " Public IP of the IGN is: " + event.getExternalIp());
                // MapPortResponse will be handled now
            }
        }
    };
    Handler<MappedPortsChanged> handleMappedPortsChanged = new Handler<MappedPortsChanged>() {
        @Override
        public void handle(MappedPortsChanged event) {
            logger.info(compName + "UPnP ports have changed status");

            Map<Integer, ForwardPortStatus> changedPorts = event.getChangedPorts();
            for (int p : changedPorts.keySet()) {
                logger.info(compName + " " + p + ": " + changedPorts.get(p));
            }

        }
    };
    Handler<MapPortsResponse> handleMapPortsResponse = new Handler<MapPortsResponse>() {
        @Override
        public void handle(MapPortsResponse event) {
            logger.trace("Mapped ports response received: " + event.isStatus());

            // if I already received a response that there's no IGN, then echo already started
            TimeoutId requestId = event.getRequestId();
            if (upnpRequests.contains(event.getRequestId())) {
                upnpRequests.remove(requestId);
                CancelTimeout ct = new CancelTimeout(requestId);
                delegator.doTrigger(ct, timer);

                if (event.isStatus()) {
                    Map<Integer, Integer> mapPorts = event.getPrivatePublicPorts();
                    int externalPort = 0;
                    // 3 ports can be returned - either 2 stunserver ports or our self port.
                    for (int p : mapPorts.keySet()) {
                        if (p != VodConfig.DEFAULT_STUN_PORT
                                && p != VodConfig.DEFAULT_STUN_PORT_2) {
                            externalPort = mapPorts.get(p);
                            break;
                        }
                    }
                    if (externalPort == 0) { // stun server ports returned
                        boolean upnpStunStatus = mapPorts.size() == 2;
                        for (int p : mapPorts.keySet()) {
                            if (p != VodConfig.DEFAULT_STUN_PORT
                                    && p != VodConfig.DEFAULT_STUN_PORT_2) {
                                upnpStunStatus = false;
                            }
                        }
                        if (upnpStunStatus) {
                            upnpStun = true;
                            sendResponse(GetNatTypeResponse.Status.UPNP_STUN_SERVERS_ENABLED);
                        } else {
                            logger.warn(compName + "Shouldn't get here. UPnP port mapping response for port not handled by StunClient.");
                        }
                    } else { // external port mapped
                        upnpStun = true;
                        sendResponse(GetNatTypeResponse.Status.SUCCEED);

                        Map<Integer, Integer> privatePublicPorts = new HashMap<Integer, Integer>();
                        // Stun server ports
                        privatePublicPorts.put(VodConfig.DEFAULT_STUN_PORT, VodConfig.DEFAULT_STUN_PORT);
                        privatePublicPorts.put(VodConfig.DEFAULT_STUN_PORT_2, VodConfig.DEFAULT_STUN_PORT_2);
                        // if mapping of stun server ports fails, it doesn't matter. client proceeds as
                        // upnp nat type, with no stun-server support
                        // TODO - Enable the line below if we want the node to 
                        // act as a stun server as well
//                        sendUpnpMapPortsRequest(privatePublicPorts);
                    }
                } else { // !upnpSupported, then start Stun protocol
                    startEcho();
                    delegator.doTrigger(new ShutdownUpnp(),
                            upnp.getPositive(UpnpPort.class));
                }
            }
        }
    };

    @Override
    public void stop(Stop stop) {
        delegator.doTrigger(new ShutdownUpnp(), upnp.getControl());

    }
}