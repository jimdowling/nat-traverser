package se.sics.gvod.stun.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import se.sics.gvod.stun.server.events.StunServerInit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import se.sics.gvod.timer.TimeoutId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.stun.msgs.EchoChangeIpAndPortMsg;
import se.sics.gvod.stun.msgs.EchoChangePortMsg;
import se.sics.gvod.stun.msgs.EchoMsg;
import se.sics.gvod.stun.msgs.ServerHostChangeMsg;
import se.sics.gvod.stun.msgs.StunRequestMsg;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Handler;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.RTTStore;
import se.sics.gvod.common.RTTStore.RTT;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.SelfImpl;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.StunServerConfiguration;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.kompics.Stop;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.kompics.Fault;
import se.sics.kompics.Positive;

/**
 * A partner is required to provide the stun service. Only nodes with the same
 * polarity can be partners - even or odd IDs can be partners. The reason is
 * that clients can then send 2 echo requests to 2 servers in parallel, knowing
 * that it won't mess up the NAT type identification by creating a NAT binding
 * to a partner as a side-effect of parallelizing the first Echo test.
 *
 * @author jdowling
 */
public final class StunServer extends MsgRetryComponent {

    private static final int NUM_NEW_PARTNERS_PER_CYCLE = 2;
    private static final Logger logger = LoggerFactory.getLogger(StunServer.class);
    private Positive<NatNetworkControl> netControl = positive(NatNetworkControl.class);
    private List<Partner> partners = new ArrayList<Partner>();
    private Map<TimeoutId, Long> partnerRTTs = new HashMap<TimeoutId, Long>();
    private Set<Long> outstandingHostChangeRequests = new HashSet<Long>();
    private String compName;
    private Self self;
    private StunServerConfiguration config;

    private class PingPartnersTimeout extends Timeout {

        public PingPartnersTimeout(SchedulePeriodicTimeout st) {
            super(st);
        }
    }

    public class StunPortBindResponse extends PortBindResponse {

        public StunPortBindResponse(PortBindRequest request) {
            super(request);
        }
    }

    public StunServer(StunServerInit init) {
        this(null, init);
    }

//------------------------------------------------------------------------    
    public StunServer(RetryComponentDelegator delegator, StunServerInit init) {
        super(delegator);
        this.delegator.doAutoSubscribe();
        doInit(init);
    }
//------------------------------------------------------------------------    

    public void doInit(StunServerInit init) {
        Self s = init.getSelf();
        self = new SelfImpl(new Nat(Nat.Type.OPEN),
                s.getIp(), VodConfig.DEFAULT_STUN_PORT, s.getId(), s.getOverlayId());
        compName = "(" + self.getId() + ") ";
        config = init.getConfig();

        StringBuilder sb = new StringBuilder();
        sb.append(compName).append("Starting SunServer: ").append(self.getAddress()).append("\n");
        sb.append(compName).append(self.getAddress().getPeerAddress().getId()).append(" - partners: ");
        for (VodAddress p : init.getPartners()) {
            addPartner(p);
            sb.append(p.getPeerAddress().getId()).append(", ");
        }

        SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(config.getPartnerHeartbeatPeriod(),
                config.getPartnerHeartbeatPeriod());
        PingPartnersTimeout ppt = new PingPartnersTimeout(spt);
        spt.setTimeoutEvent(ppt);
        delegator.doTrigger(spt, timer);
        logger.info(sb.toString());

        bindPort(VodConfig.DEFAULT_STUN_PORT);
        bindPort(VodConfig.DEFAULT_STUN_PORT_2);
    }

    private void bindPort(int port) {
        Address a = new Address(self.getIp(), port, self.getId());
        PortBindRequest allocReq1 = new PortBindRequest(a, Transport.UDP);
        StunPortBindResponse allocResp1 = new StunPortBindResponse(allocReq1);
        allocReq1.setResponse(allocResp1);
        delegator.doTrigger(allocReq1, netControl);
    }
    Handler<StunPortBindResponse> handleStunPortResponse
            = new Handler<StunPortBindResponse>() {
                @Override
                public void handle(StunPortBindResponse response) {
                    if (response.getStatus() != StunPortBindResponse.Status.SUCCESS) {
                        throw new IllegalStateException(compName + " couldn't allocate stun server port: "
                                + response.getPort() + " . Response status = " + response.getStatus());
                    }
                }
            };
//------------------------------------------------------------------------    
    Handler<PingPartnersTimeout> handlePingPartnersTimeout = new Handler<PingPartnersTimeout>() {
        @Override
        public void handle(PingPartnersTimeout event) {
            long worstRto = Long.MAX_VALUE / 10;
            int bestOpenNodes = NUM_NEW_PARTNERS_PER_CYCLE;
            Partner p = getWorstPartner();

            if (p == null) {
                bestOpenNodes = config.getMaxNumPartners();
            } else {
                worstRto = p.getRTO();
            }

            // the rto should be at least 2.5 times the RTT, so there is some tolerance
            // built into this
            for (RTTStore.RTT rtt : RTTStore.getAllOnAvgBetterRtts(self.getId(), worstRto, 20 /* tolerance*/)) {
                if (rtt.getAddress().isOpen()) {
                    addPartner(rtt.getAddress());
                    if (--bestOpenNodes == 0) {
                        break;
                    }
                }
            }
        }
    };
//------------------------------------------------------------------------    
    Handler<EchoMsg.Request> handleEchoRequestMsg = new Handler<EchoMsg.Request>() {
        @Override
        public void handle(EchoMsg.Request msg) {
            printMsgDetails(msg);

            // this server listens on both the ports i.e. Pa, and Pb.
            // if the echo is for Pa then reply using Pa, and if the echo is for
            // Pb then reply through Pb. request to any other port is simply dropped
            int port = msg.getDestination().getPort();

            VodAddress sourceAddress = null;
            if (port == VodConfig.DEFAULT_STUN_PORT) {
                sourceAddress = self.getAddress();  // self have port Pa
            } else if (port == VodConfig.DEFAULT_STUN_PORT_2) {
                sourceAddress = ToVodAddr.stunServer2(self.getAddress().getPeerAddress());
            } else {
                logger.warn(compName + " sent to unauthorized port : " + port);
                return;
            }

            Address replyTo = msg.getReplyTo();
            if (msg.getTestType() != EchoMsg.Test.HEARTBEAT) {
                replyTo = msg.getSource();
            } else {
                logger.debug("Received a Echo.HEARTBEAT msg");
            }
            logger.debug("For {} . ReplyTo is " + replyTo, msg.getTestType());

            EchoMsg.Response responseMsg = new EchoMsg.Response(sourceAddress,
                    ToVodAddr.stunClient(replyTo),
                    msg.getSource(),
                    getPartnerAddresses(),
                    (int) config.getRto(),
                    msg.getTestType(),
                    msg.getTransactionId(),
                    msg.getTimeoutId(),
                    VodConfig.DEFAULT_STUN_PORT_2);

            // set the tryID in the response message
            if (msg.getTestType() == EchoMsg.Test.PING) {
                responseMsg.setTryId(msg.getTryId());
            }

            delegator.doTrigger(responseMsg, network);
        }
    };
//------------------------------------------------------------------------    
    Handler<EchoChangePortMsg.Request> handleEchoChangePort = new Handler<EchoChangePortMsg.Request>() {
        @Override
        public void handle(EchoChangePortMsg.Request message) {
            printMsgDetails(message);

            // reply using the other port i.e. "VodConfig.DEFAULT_STUN_PORT_2"
            VodAddress changedAddress = ToVodAddr.stunServer2(self.getAddress().getPeerAddress());

            delegator.doTrigger(new EchoChangePortMsg.Response(changedAddress,
                    message.getVodSource(),
                    message.getSource(),
                    message.getTransactionId(),
                    message.getTimeoutId()), network);
        }
    };
//------------------------------------------------------------------------    
    Handler<EchoChangeIpAndPortMsg.Request> handleEchoChangeIpRequestMsg = new Handler<EchoChangeIpAndPortMsg.Request>() {
        @Override
        public void handle(EchoChangeIpAndPortMsg.Request message) {
            printMsgDetails(message);

            int srcId = message.getSource().getId();
            sendHostChange(srcId, message.getVodSource(), message.getTransactionId(), message.getTimeoutId());
        }
    };
//------------------------------------------------------------------------    
    Handler<ServerHostChangeMsg.Request> handleServerHostChangeMsgRequest = new Handler<ServerHostChangeMsg.Request>() {
        @Override
        public void handle(ServerHostChangeMsg.Request message) {
            printMsgDetails(message);

            // TODO - add this node to the RandomView or some list of previously seen nodes.
            Address clientPublicAddr = message.getClientPublicAddr();
            long transactionId = message.getTransactionId();
            TimeoutId originalTimeoutId = message.getOriginalTimeoutId();

            Address myChangeportAddress = new Address(self.getIp(), VodConfig.DEFAULT_STUN_PORT_2, self.getId());
            delegator.doTrigger(new EchoChangeIpAndPortMsg.Response(
                    ToVodAddr.stunServer(myChangeportAddress),
                    ToVodAddr.stunClient(clientPublicAddr),
                    transactionId, originalTimeoutId), network);
            logger.debug(compName + "StunServer: sending EchoChangeIpandPort response from 2nd server to: "
                    + clientPublicAddr.getId()
                    + " with timeoutId = " + originalTimeoutId);

            delegator.doTrigger(new ServerHostChangeMsg.Response(
                    self.getAddress(), message.getVodSource(),
                    transactionId, message.getTimeoutId()), network);
            logger.debug(compName + "Sending ServerHostChangeMsg.Response "
                    + "response from 2nd server to: "
                    + message.getSource().getId());
        }
    };
//------------------------------------------------------------------------    
    Handler<ServerHostChangeMsg.Response> handleServerHostChangeMsgResponse = new Handler<ServerHostChangeMsg.Response>() {
        @Override
        public void handle(ServerHostChangeMsg.Response message) {
            logger.debug(compName + "Recvd: " + message.getClass().getName());

            Long sendTime = partnerRTTs.remove(message.getTimeoutId());
            delegator.doCancelRetry(message.getTimeoutId());
            if (sendTime != null) {
                addPartner(message.getVodSource(), System.currentTimeMillis() - sendTime);
            } else {
                logger.warn(compName + " Couldn't find send timer for partner: " + message.getSource());
            }
            printPartners();
            outstandingHostChangeRequests.remove(message.getTransactionId());
        }
    };
//------------------------------------------------------------------------    
    Handler<ServerHostChangeMsg.RequestTimeout> handleServerHostChangeTimeout = new Handler<ServerHostChangeMsg.RequestTimeout>() {
        @Override
        public void handle(ServerHostChangeMsg.RequestTimeout event) {
            // If I haven't already received a response for a ServerHostChangeMsg.Request
            // then tell the client that EchoChangeIpAndPort has failed.
            ServerHostChangeMsg.Request req = (ServerHostChangeMsg.Request) event.getMsg();
            Address dest = event.getMsg().getDestination();
            TimeoutId timeoutId = event.getTimeoutId();
            if (partnerRTTs.remove(timeoutId) != null) {
                VodAddress vodDest = ToVodAddr.stunServer(dest);
                failedPartner(vodDest);
                logger.warn(compName + " ServerHostChangeMsg Timeout for " + dest.getId()
                        + ". Num partners left now: " + partners.size());
            }
            if (outstandingHostChangeRequests.remove(req.getTransactionId()) == true) {
                delegator.doTrigger(new EchoChangeIpAndPortMsg.Response(self.getAddress(),
                        req.getVodSource(),
                        req.getTransactionId(),
                        req.getOriginalTimeoutId(),
                        EchoChangeIpAndPortMsg.Response.Status.FAIL),
                        network);
            }
        }
    };

//------------------------------------------------------------------------    
    private void sendHostChange(int srcId, VodAddress clientPublicIp, long transactionId, TimeoutId originalTimeoutId) {
        Set<Partner> bestPartners = getBestPartners(srcId, VodConfig.STUN_PARTNER_NUM_PARALLEL);
        if (bestPartners.isEmpty()) {
            delegator.doTrigger(new EchoChangeIpAndPortMsg.Response(self.getAddress(),
                    clientPublicIp,
                    transactionId,
                    originalTimeoutId,
                    EchoChangeIpAndPortMsg.Response.Status.FAIL),
                    network);
            logger.error("No partner found for sending ServerHostChangeMsg.Request.");
        } else {

            outstandingHostChangeRequests.add(transactionId);
            for (Partner p : bestPartners) {
                VodAddress dest = ToVodAddr.stunServer(p.getAddress());

                // Using a fixed RTO that must always be lower than
                // EchoMsgChangeIpAndPort.RequestTimeout, so that a EchoMsgChangeIpAndPort.Response
                // returns before EchoMsgChangeIpAndPort.RequestTimeout if the 2nd server is alive.
                // The only edge case not covered is if the 2nd server acks this msg, but it's
                // reply to the client is lost. In this case, the client will believe it has a more
                // restrictive nat than it actually has (it's Filtering will be HD or PD, instead
                // of EI
                long worstCaseRto = config.getRto() * VodConfig.STUN_PARTNER_RTO_MULTIPLIER
                        + config.getMinimumRtt();

                logger.debug(compName + "ServerHostChangeMsg.Request sent to " + p.getAddress().getId()
                        + " , src=" + clientPublicIp.getId() + " privSrc="
                        + clientPublicIp.getId() + " rto = " + worstCaseRto);

                // Setting timeouts based on RTOs was not good enough for Guifi.net,
                // user-supplied values taken instead.
                ServerHostChangeMsg.Request req = new ServerHostChangeMsg.Request(self.getAddress(),
                        dest, clientPublicIp.getPeerAddress(), transactionId, originalTimeoutId);
                ScheduleRetryTimeout st = new ScheduleRetryTimeout(worstCaseRto,
                        config.getRtoRetries(), config.getRtoScale());
                ServerHostChangeMsg.RequestTimeout shct
                        = new ServerHostChangeMsg.RequestTimeout(st, req);
                TimeoutId timeoutId = delegator.doRetry(shct);
                partnerRTTs.put(timeoutId, System.currentTimeMillis());
            }
        }
    }

//------------------------------------------------------------------------    
    private void printPartners() {
        for (Partner p : partners) {
            logger.debug(compName + "Partner: " + p.getAddress().getId() + " RTT: " + p.getRTO());
        }
    }

//------------------------------------------------------------------------    
    private void printMsgDetails(StunRequestMsg message) {
        logger.trace(compName + message.getClass().getCanonicalName() + ": "
                + " ; Public src: " + message.getSource()
                + " ; Public dest: " + message.getDestination()
                + " ; TimeoutId: " + message.getTimeoutId()
                + " ; transactionId: " + message.getTransactionId());
    }

//------------------------------------------------------------------------    
    private void addPartner(VodAddress addr, long rtt) {
        RTTStore.addSample(self.getId(), addr, rtt);
        addPartner(addr);
    }

//------------------------------------------------------------------------    
    private void addPartner(VodAddress addr) {

        if (addr == null) {
            logger.warn(compName + "New stun partner was null");
            return;
        }

        if (addr.getId() == self.getId()) {
            logger.debug(compName + "Cannot add self as partner");
            return;
        }
//        if ((self.getId() % 2) != (addr.getId() % 2)) {
//            logger.trace("Not adding " + addr + " . Only accept partners of the same polarity.");
//        }

        Partner newPartner = new Partner(self.getId(), addr, config.getRto());
        if (partners.contains(newPartner)) {
            logger.trace(compName + "Tried to re-add existing partner.");
            return;
        }

        partners.add(newPartner);

        logger.debug(compName + "Stun server (" + self.getId() + ") added partner: " + newPartner.getAddress().getId());

        if (partners.size() >= config.getMaxNumPartners()) {
            Partner worst = getWorstPartner();
            if (worst != null) {
                partners.remove(worst);
                logger.trace("Stun server (" + self.getId() + ") removed partner: " + worst.getAddress().getId());
            }
        }
    }

//------------------------------------------------------------------------    
    private boolean failedPartner(VodAddress node) {
        logger.debug(compName + "Removed partner: " + node.getId());
        RTTStore.removeSamples(self.getId(), node);
        return partners.remove(new Partner(self.getId(), node, config.getRto()));
    }

//------------------------------------------------------------------------    
    private SortedSet<Partner> getBestPartners(int srcId, int numPartners) {
        Collections.sort(partners);
        SortedSet<Partner> candidates = new TreeSet<Partner>();
        Iterator<Partner> iter = partners.iterator();
        Partner candidate = null;
        int i = 0;
        while (iter.hasNext() && i < numPartners) {
            candidate = iter.next();
            if (srcId != candidate.getAddress().getId()) {
                i++;
                candidates.add(candidate);
            }
        }

        return candidates;
    }

//------------------------------------------------------------------------    
    private Partner getWorstPartner() {
        if (partners.isEmpty()) {
            return null;
        }

        List<Partner> contactedPartners = new ArrayList<Partner>();
        contactedPartners.addAll(partners);

        // don't return partners who just joined and have a MAX_VALUE RTT
        List<Partner> toRemove = new ArrayList<Partner>();
        for (Partner p : contactedPartners) {
            if (p.getRTO() == Long.MAX_VALUE) {
                toRemove.add(p);
            }
        }

        // at least 2 partners not just joined, remove just joined partners
        if (contactedPartners.size() > toRemove.size() + 1) {
            contactedPartners.removeAll(toRemove);
        }

        if (contactedPartners.isEmpty() || toRemove.isEmpty()) {
            return null;
        }

        Collections.sort(contactedPartners);
        Partner worst = contactedPartners.get(0);

        // pick arbitrary 'bad' latency for worst partner of 100ms
        if (worst.getRTO() > 100 && toRemove.size() > 0) {
            return worst;
        } else //  our worst is good enough, so return node we havent measured RTT to yet.
        {
            return toRemove.get(0);
        }
    }

//------------------------------------------------------------------------    
    private Set<Address> getPartnerAddresses() {
        Set<Address> partnerAddresses = new HashSet<Address>();
        for (Partner p : partners) {
            partnerAddresses.add(ToVodAddr.stunServer2(p.getAddress()).getPeerAddress());
        }

        return partnerAddresses;
    }

//------------------------------------------------------------------------    
    @Override
    public void stop(Stop event) {
        //handleStop is the one that call stop() - calling handleStop() again will end up in an endless loop
        //handleStop.handle(event);
        
    }
}
