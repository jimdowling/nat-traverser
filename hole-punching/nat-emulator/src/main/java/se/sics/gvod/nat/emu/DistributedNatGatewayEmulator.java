package se.sics.gvod.nat.emu;

import java.net.InetAddress;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.msgs.RelayMsgNetty;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.hp.msgs.HpMsg;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.nat.emu.events.DistributedNatGatewayEmulatorInit;
import se.sics.gvod.nat.emu.events.NatPortBindResponse;
import se.sics.gvod.nat.emu.events.RuleCleanupTimeout;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.events.*;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.stun.upnp.UpnpPort;
import se.sics.gvod.stun.upnp.events.*;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.kompics.*;

/**
 * DistributedNatGatewayEmulator doesn't rewrite the clientId in messages. Can
 * be used on planetlab.
 *
 * @author jdowling
 */
public class DistributedNatGatewayEmulator extends MsgRetryComponent {

    private final Logger logger = LoggerFactory.getLogger(DistributedNatGatewayEmulator.class);
    Negative<VodNetwork> upperNet = negative(VodNetwork.class);
    Negative<NatNetworkControl> upperNetControl = negative(NatNetworkControl.class);
    Positive<NatNetworkControl> lowerNetControl = positive(NatNetworkControl.class);
    Negative<UpnpPort> upnpPort = negative(UpnpPort.class);
    // policies
    private Nat.MappingPolicy mappingPolicy;
    private Nat.AllocationPolicy allocationPolicy;
    private Nat.AlternativePortAllocationPolicy alternativePortAllocationPolicy;
    private Nat.FilteringPolicy filteringPolicy;
    private Nat.Type natType; /* type of nat i.e. natted, upnp or open*/

    private boolean isExpirationTimerEnabled = true;
    private boolean clashingOverrides = false;
    // NAT State
    private InetAddress natPublicAddress;
    /* NAT Open Ports */
    /* opened port, private end point */
    Map<Integer, Address> pubPortToPrivateAddr;

    /*
     * Private to public port MAP
     /* Private End Point (Vi), Map<public node IP and id, 
     //     * Map<Destination Port, Port Opened on NAT >>
     * Map<Port Opened on NAT >>
     */
    Map<Address, Map<InetAddress, Map<Integer, Integer>>> privateEndPointToDestinationTable;
    /* Timers  for mapped ports*/
    Map<Integer, Long> timers;
    // Port Assignment
    private Random rand;
    private int startPortRange;
    private int endPortRange;
    private int portCounter;
    private int ruleCleanupPeriod;
    private int ruleLifeTime;
    private int randomPortSeed;
    private int upnpMappedPort = -1;
    private int stunServer1MappedPort = -1;
    private int stunServer2MappedPort = -1;
    private Set<Integer> allocatedPorts = new HashSet<Integer>();
    private String compName;

    public static class BindingSession {

        private final Address v;
        private final InetAddress publicEndPoint;
        private final int dstPort;
        private final RewriteableMsg msg;

        public BindingSession(Address v, InetAddress publicEndPoint, int dstPort,
                RewriteableMsg msg) {
            this.v = v;
            this.publicEndPoint = publicEndPoint;
            this.dstPort = dstPort;
            this.msg = msg;
        }

        public int getDstPort() {
            return dstPort;
        }

        public InetAddress getPublicEndPoint() {
            return publicEndPoint;
        }

        public Address getV() {
            return v;
        }

        public RewriteableMsg getMsg() {
            return msg;
        }
    }

    public DistributedNatGatewayEmulator() {
        this(null);
    }

    public DistributedNatGatewayEmulator(RetryComponentDelegator delegator) {
        super(delegator);
        this.delegator.doSubscribe(handleStart, control);
        this.delegator.doSubscribe(handleStop, control);
        this.delegator.doSubscribe(handleUpperMessage, upperNet);
        this.delegator.doSubscribe(handleLowerMessage, network);
        this.delegator.doSubscribe(handleInit, control);
        this.delegator.doSubscribe(handleUpnpGetPublicIpRequest, upnpPort);
        this.delegator.doSubscribe(handleMapPortsRequest, upnpPort);
        this.delegator.doSubscribe(handleUnmapPortRequest, upnpPort);
        this.delegator.doSubscribe(handleShutdownUpnp, upnpPort);
        this.delegator.doSubscribe(handleNatPortBindResponse, lowerNetControl);
        this.delegator.doSubscribe(handlePortBindRequest, upperNetControl);
        this.delegator.doSubscribe(handlePortAllocRequest, upperNetControl);
        this.delegator.doSubscribe(handlePortDeleteRequest, upperNetControl);

        // handler in super class
        this.delegator.doSubscribe(handleRTO, timer);
    }
    Handler<DistributedNatGatewayEmulatorInit> handleInit = new Handler<DistributedNatGatewayEmulatorInit>() {
        @Override
        public void handle(DistributedNatGatewayEmulatorInit init) {
            startPortRange = init.getStartPortRange();
            endPortRange = init.getEndPortRange();
            natType = init.getNatType();
            mappingPolicy = init.getMp();
            allocationPolicy = init.getAp();
            filteringPolicy = init.getFp();
            alternativePortAllocationPolicy = init.getAltAp();
            ruleCleanupPeriod = init.getRuleCleanupPeriod();
            natPublicAddress = init.getNatIP();
            clashingOverrides = init.isClashingOverrides();
            ruleLifeTime = init.getRuleLifeTime();
            randomPortSeed = init.getRandomPortSeed();
            reset();
            compName = "Nat(" + init.getNatIP() + ":" + natType + ") ";

            logger.debug(compName + "NatGateway: " + natPublicAddress + " - "
                    + mappingPolicy + " - "
                    + " - " + allocationPolicy
                    + " - " + filteringPolicy);
            SchedulePeriodicTimeout st = new SchedulePeriodicTimeout(ruleCleanupPeriod, ruleCleanupPeriod);
            RuleCleanupTimeout msgTimeout = new RuleCleanupTimeout(st);
            st.setTimeoutEvent(msgTimeout);
            delegator.doTrigger(st, timer);
        }
    };
    Handler<PortBindRequest> handlePortBindRequest = new Handler<PortBindRequest>() {
        @Override
        public void handle(PortBindRequest event) {

            logger.trace(compName + "Port bind request received.");

            PortBindResponse response = event.getResponse();
            if (allocatedPorts.contains(event.getPort())) {
                response.setStatus(PortBindResponse.Status.PORT_ALREADY_BOUND);
            } else {
                allocatedPorts.add(event.getPort());
                response.setStatus(PortBindResponse.Status.SUCCESS);
            }
            delegator.doTrigger(response, upperNetControl);
        }
    };
    Handler<PortAllocRequest> handlePortAllocRequest = new Handler<PortAllocRequest>() {
        @Override
        public void handle(PortAllocRequest event) {
            int numPorts = event.getNumPorts();

            logger.trace(compName + "Port allocation request received.");

            Set<Integer> setPorts = new HashSet<Integer>();

            for (int i = 0; i < numPorts; i++) {
                int randPort = -1;
                do {
                    randPort = rand.nextInt(Math.abs(endPortRange - startPortRange)) + startPortRange;
                } while (allocatedPorts.contains(randPort));
                allocatedPorts.add(randPort);
                setPorts.add(randPort);
                logger.trace(compName + "Allocated port : " + randPort);
            }

            PortAllocResponse response = event.getResponse();
            response.setAllocatedPorts(setPorts);
            delegator.doTrigger(response, upperNetControl);
        }
    };
    Handler<PortDeleteRequest> handlePortDeleteRequest = new Handler<PortDeleteRequest>() {
        @Override
        public void handle(PortDeleteRequest message) {
            Set<Integer> p = message.getPortsToDelete();
            Set<Integer> deletedPorts = new HashSet<Integer>();
            for (int i : p) {
                if (allocatedPorts.remove(i)) {
                    deletedPorts.add(i);
                }
            }
            PortDeleteResponse response = message.getResponse();
            if (response != null) {
                response.setPorts(deletedPorts);
                delegator.doTrigger(response, upperNetControl);
            }
        }
    };
    Handler<UnmapPortsRequest> handleUnmapPortRequest =
            new Handler<UnmapPortsRequest>() {
        @Override
        public void handle(UnmapPortsRequest event) {
            logger.trace(compName + "UnmapPortsRequest event");
            delegator.doTrigger(new UnmapPortsResponse(event, true), upnpPort);
        }
    };
    Handler<ShutdownUpnp> handleShutdownUpnp =
            new Handler<ShutdownUpnp>() {
        @Override
        public void handle(ShutdownUpnp event) {
            logger.trace(compName + "ShutdownUpnp");
            // do nothing
        }
    };
    Handler<MapPortsRequest> handleMapPortsRequest = new Handler<MapPortsRequest>() {
        @Override
        public void handle(MapPortsRequest event) {
            logger.trace(compName + "handleMapPortRequest");
            if (isUpnp()) {

                boolean res = true;
                Map<Integer, Integer> mappedPorts = event.getPrivatePublicPorts();
                for (int p : mappedPorts.keySet()) {

                    if (p == VodConfig.DEFAULT_STUN_PORT && stunServer1MappedPort == -1) {
                        stunServer1MappedPort = VodConfig.DEFAULT_STUN_PORT;
                        mappedPorts.put(p, p);
                    } else if (p == VodConfig.DEFAULT_STUN_PORT_2 && stunServer2MappedPort == -1) {
                        stunServer1MappedPort = VodConfig.DEFAULT_STUN_PORT_2;
                        mappedPorts.put(p, p);
                    } else if (upnpMappedPort == -1) {
                        upnpMappedPort = p;
                    } else {
                        res = false;
                    }
                }

                logger.debug(compName + "UPNP mapping " + upnpMappedPort);
                delegator.doTrigger(new MapPortsResponse(event, mappedPorts,
                        natPublicAddress, 99 /* natId */, res), upnpPort);

            } else {
                delegator.doTrigger(new MapPortsResponse(event, event.getPrivatePublicPorts(),
                        natPublicAddress, 99 /* natId */, false), upnpPort);
            }
        }
    };
    Handler<UpnpGetPublicIpRequest> handleUpnpGetPublicIpRequest =
            new Handler<UpnpGetPublicIpRequest>() {
        @Override
        public void handle(UpnpGetPublicIpRequest event) {
            logger.trace(compName + "handleUPNPGetPublicIPrequest");
            delegator.doTrigger(new UpnpGetPublicIpResponse(event, natPublicAddress), upnpPort);
        }
    };
    Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
        }
    };

    private boolean isUpnp() {
        return natType == Nat.Type.UPNP;
    }

    String getExistingMappingsAsString(Map<InetAddress, Map<Integer, Integer>> ds) {
        StringBuilder sb = new StringBuilder();
        sb.append(" Existing mappings: ");
        if (ds != null) {
            for (InetAddress iip : ds.keySet()) {
                sb.append(iip.toString()).append(":");
                for (int p : ds.get(iip).keySet()) {
                    sb.append(p).append(",");
                }
            }
        }
        return sb.toString();
    }
    Handler<RewriteableMsg> handleUpperMessage = new Handler<RewriteableMsg>() {
        @Override
        public void handle(RewriteableMsg inMsg) {
            RewriteableMsg msg = inMsg.copy();

            if (msg instanceof HpMsg) {
                HpMsg hm = (HpMsg) msg;
                logger.debug(compName + inMsg.getSource() + " -- "
                        + inMsg.getDestination() + " of type "
                        + inMsg.getClass().getCanonicalName() + " - "
                        + hm.getMsgTimeoutId());
            }

            if (msg.getDestination().getIp().equals(natPublicAddress)
                    && msg.getDestination().getId() == msg.getSource().getId()) {
                throw new IllegalArgumentException("Sending msg to itself in NAT: "
                        + inMsg.getSource() + " -- "
                        + inMsg.getDestination() + " of type "
                        + inMsg.getClass());
            }

            if (msg.getSource().getPort() < 1024 || msg.getSource().getPort() > 65535
                    || msg.getDestination().getPort() < 1024 || msg.getDestination().getPort() > 65535) {
                throw new IllegalArgumentException("Out-of-range port for msg: "
                        + inMsg.getSource() + " -- "
                        + inMsg.getDestination() + " of type "
                        + inMsg.getClass());
            }
            if (natType == Nat.Type.OPEN) {
                delegator.doTrigger(msg, network);
                return;
            } else if (isUpnp()) {
                logger.trace(compName + "Handle Upper Message.UPnP");

                if (upnpMappedPort != msg.getSource().getPort() && msg.getSource().getPort() != 8081
                        && msg.getSource().getPort() != BaseCommandLineConfig.DEFAULT_STUN_PORT
                        && msg.getSource().getPort() != BaseCommandLineConfig.DEFAULT_STUN_PORT_2) {
                    throw new IllegalStateException(" port is not mapped. mapped port: "
                            + upnpMappedPort + " src port: " + msg.getSource().getPort()
                            + " msg class: " + msg.getClass().getName());
                }
                // before sending the message change the src address
                Address newSourceAddress = new Address(natPublicAddress,
                        msg.getSource().getPort(),
                        msg.getSource().getId());
                msg.rewritePublicSource(newSourceAddress);
                delegator.doTrigger(msg, network);
                logger.trace(compName + " packet send to lower port. private src:"
                        //                        + msg.getPrivateSource() 
                        + " src:"
                        + msg.getSource() + " dest: "
                        + msg.getDestination() + " message class: "
                        + msg.getClass().toString());
                return;
            }

            if (natPublicAddress.equals(msg.getSource().getIp())
                    && (msg.getSource().getId() == msg.getDestination().getId())) {
                logger.warn(compName + "NAT Gateway and msg have same source Ip!!"
                        + msg.getClass().getName());
                throw new IllegalStateException("NAT Gateway and msg have same source Ip! "
                        + msg.getClass().getName());
            }


            String src = msg.getSource().toString();
            if (msg instanceof RelayMsgNetty.Response) {
                RelayMsgNetty.Response vm = (RelayMsgNetty.Response) msg;
                src = vm.getVodSource().toString();
            }
            if (msg instanceof RelayMsgNetty.Request) {
                RelayMsgNetty.Request vm = (RelayMsgNetty.Request) msg;
                src = vm.getVodSource().toString();
                Nat n = vm.getVodSource().getNat();
                if (mappingPolicy != n.getMappingPolicy() || allocationPolicy
                        != n.getAllocationPolicy() || filteringPolicy != n.getFilteringPolicy()) {
                    logger.warn(compName + " VIOLATION");
                }
            }

            logger.trace(compName + "Upper Message. Src: " + src + " dest: "
                    + msg.getDestination() + " - " + msg.getClass());
            // Mapping policy and Allocation policy

            InetAddress srcIp = msg.getSource().getIp();
            InetAddress destIp = msg.getDestination().getIp();
            int srcPort = msg.getSource().getPort();
            int destPort = msg.getDestination().getPort();

            int mappedPort = srcPort;

            // Reused endpoints
            Address privateEndPoint = msg.getSource();


            // JIM - this code is checking whether a new mapping needs to be
            // created or whether an old one can be reused.

            if (!privateEndPointToDestinationTable.containsKey(privateEndPoint)) {
                // Not talking with the host
                logger.debug(compName + "No existing mapping for Src: " + privateEndPoint + " - " + msg.getClass());

                /*
                 * There is no mapping for this two endpoints,
                 * we need to open a port
                 */
                mapPort(msg, privateEndPoint, destIp, destPort, srcIp, srcPort, 0);
                return;
            } else {
                // If v is already talking with somebody outside
                Map<InetAddress, Map<Integer, Integer>> ds =
                        privateEndPointToDestinationTable.get(privateEndPoint);
                logger.trace(compName + "Upper. Src: " + src + " dest: "
                        + msg.getDestination() + " - " + msg.getClass()
                        + "::" + natPublicAddress + "::"
                        + getExistingMappingsAsString(ds));

                if (ds.containsKey(destIp)
                        && ds.get(destIp).containsKey(destPort)) {
                    // don't need to bind port here, already talking to somebody at this destIp:destPort
                    mappedPort = ds.get(destIp).get(destPort);
                    logger.debug(compName + "No need to create new port for " + destIp + ":" + destPort + " at private addr {}  "
                            + msg.getClass(), privateEndPoint);

                    logger.debug(compName + getExistingMappingsAsString(ds));
                } else {
                    // Mapping Policy
                    switch (mappingPolicy) {
                        case ENDPOINT_INDEPENDENT:
                            // TODO: E corresponds always a single U
                            mappedPort = ds.values().iterator().next().values().iterator().next();
//                            mappedPort = ds.values().iterator().next();

                            //put this mappedPort in the map
//                            if (ds.containsKey(destIp) && ds.get(destIp).keySet().contains(ds)) {
//                            Map<Integer, Integer> m = new HashMap<Integer, Integer>();
                            Map<Integer, Integer> m = ds.get(destIp);
                            if (m == null) {
                                m = new HashMap<Integer, Integer>();
                                ds.put(destIp, m);
                            }
                            m.put(destPort, mappedPort);

//                                logger.trace(compName +"Adding new port for {} at private addr {}. Mapped port = " +
//                                        mappedPort, publicEndPointIP, privateEndPoint);
//                                StringBuilder sb = new StringBuilder();
//                                sb.append(" Existing mappings: ");
//                                for (IpIntPair iip : ds.keySet()) {
//                                    sb.append(iip.toString()).append("; ");
//                                }
//                                logger.trace(compName +sb.toString());
//                            } else if (!ds.containsKey(destIp)) {
//                                Map<Integer, Integer> set = new HashMap<Integer, Integer>();
//                                set.put(destPort, mappedPort);
//                                ds.put(destIp, set);
                            logger.debug(compName + "Adding new ip/port for {} at private addr {} at nat: "
                                    + natPublicAddress, destIp, privateEndPoint);
                            logger.debug(compName + getExistingMappingsAsString(ds));
                            //                            } else {
//                                throw new UnsupportedOperationException("ERROR: Unhandled Situation.");
//                            }
                            break;
                        case HOST_DEPENDENT:
                            if (ds.containsKey(destIp)) {
                                mappedPort = ds.get(destIp).values().iterator().next();

                                Map<Integer, Integer> entry = ds.get(destIp);
                                if (entry == null) {
                                    entry = new HashMap<Integer, Integer>();
                                    ds.put(destIp, entry);
                                }

//                                if (ds.containsKey(destIp) && ds.get(destIp).containsKey(destPort) == false) {
//                                    Map<Integer, Integer> entry = new HashMap<Integer, Integer>();
                                entry.put(destPort, mappedPort);
//                                    ds.put(destIp, entry);
//                                }
                            } else {
                                // Allocate a new port
                                mapPort(msg, privateEndPoint, destIp, destPort, srcIp, srcPort, 0);
                                return;
                            }
                            break;
                        case PORT_DEPENDENT:
                            // Allocate a new port. Case in which mapping is already present was considered previously
                            mapPort(msg, privateEndPoint, destIp, destPort, srcIp, srcPort, 0);
                            return;
                        default:
                            break;
                    }
                }
            }

            // Create/Renew Timestamp
            timers.put(mappedPort, System.currentTimeMillis());

            // Forward the message the the public address as the source address and mapped port as the source port

            Address newSourceAddress = new Address(natPublicAddress, mappedPort,
                    msg.getSource().getId());

            msg.rewritePublicSource(newSourceAddress);
            delegator.doTrigger(msg, network);
            logger.debug(compName + "handleUpperMsg in Nat. timeoutId " + msg.getTimeoutId()
                    + " src: " + msg.getSource()
                    + " dest: " + msg.getDestination()
                    + " msg class " + shortClassName(msg.getClass().toString()));
        }
    };
    Handler<RewriteableMsg> handleLowerMessage = new Handler<RewriteableMsg>() {
        @Override
        public void handle(RewriteableMsg msg) {
            if (msg instanceof HpMsg) {
                HpMsg hm = (HpMsg) msg;
                logger.debug(compName + msg.getSource() + " -- "
                        + msg.getDestination() + " of type "
                        + msg.getClass().getCanonicalName() + " - "
                        + hm.getMsgTimeoutId());
            }


            logger.debug(compName + "handleLowerMsg in Nat. timeoutId " + msg.getTimeoutId()
                    + " src: " + msg.getSource()
                    + " dest: " + msg.getDestination()
                    + " msg class " + shortClassName(msg.getClass().toString()));

            if (msg.getSource().getPort() < 1024 || msg.getSource().getPort() > 65535
                    || msg.getDestination().getPort() < 1024 || msg.getDestination().getPort() > 65535) {
                throw new IllegalArgumentException("Out-of-range port for msg: "
                        + msg.getSource() + " -- "
                        + msg.getDestination()
                        + " of type "
                        + msg.getClass());
            }
            if (natType == Nat.Type.OPEN) {
                delegator.doTrigger(msg, upperNet);
            } else if (isUpnp()) {
                logger.trace(compName + "HandleLowerMessage. UPnP");
                // before sending the message change the dest address

                Address newDestinationAddress = new Address(natPublicAddress,
                        msg.getDestination().getPort(),
                        msg.getDestination().getId());
                msg.rewriteDestination(newDestinationAddress);
                delegator.doTrigger(msg, upperNet);
                logger.trace(compName + "allowing the packet in src " + msg.getSource()
                        + " dest " + msg.getDestination()
                        + " msg class " + msg.getClass().toString());
            } else {
                //filtering policy
                boolean forward = false;

                int destPort = msg.getDestination().getPort();
                int srcPort = msg.getSource().getPort();
                // TODO - can there be multiple private Addresses??
                Address v = pubPortToPrivateAddr.get(destPort);
                InetAddress srcIp = msg.getSource().getIp();
                if (v != null && !isExpired(destPort)) {
                    // Mapping exists
                    // Create/Renew Timestamp
                    timers.put(destPort, System.currentTimeMillis());

                    Map<InetAddress, Map<Integer, Integer>> map =
                            privateEndPointToDestinationTable.get(v);
                    if (map != null) {
                        logger.debug(compName + "Lower. Found mapping. Src: " + srcIp + ":"
                                + srcPort + " -> "
                                //                                + natPublicAddress + ":" + destPort 
                                + " private addr: " + v + " => (dest)"
                                + msg.getDestination()
                                + "  "
                                + msg.getClass()
                                //                                + "::" + natPublicAddress + "::"
                                + ". existing mappings ("
                                + getExistingMappingsAsString(map) + ")");

                        switch (filteringPolicy) {
                            case ENDPOINT_INDEPENDENT:
                                forward = true;
                                break;
                            case HOST_DEPENDENT:
                                if (map.containsKey(srcIp)) {
                                    Map<Integer, Integer> portMap = map.get(srcIp);
                                    Collection<Integer> ints = portMap.values();
                                    if (ints.contains(destPort)) {
                                        forward = true;
                                    } else {
                                        /* No port is open for that peer */
                                        logger.warn(compName + "Drop HD");
                                    }
                                }
                                break;
                            case PORT_DEPENDENT:

                                if (map.containsKey(srcIp)) {
                                    Map<Integer, Integer> portMap = map.get(srcIp);
                                    Collection<Integer> ints = portMap.values();
                                    if (ints.contains(destPort) && portMap.get(srcPort) != null
                                            && portMap.get(srcPort) == destPort) {
                                        forward = true;
                                    } else {
                                        /*
                                         * Either no port is open for that
                                         * peer or src port is not the same
                                         * as it sent earlier
                                         */
                                        // Drop  1
                                        logger.warn(compName + "Drop PD-1 " + msg.getClass().getCanonicalName());
                                        logger.warn(compName + "Existing mappings: "
                                                + portMap);
                                        if (msg instanceof HpMsg) {
                                            HpMsg hm = (HpMsg) msg;
                                            logger.debug(compName + "Drop PD-1 " + msg.getSource() + " -- "
                                                    + msg.getDestination() + " of type "
                                                    + msg.getClass().getCanonicalName() + " - "
                                                    + hm.getMsgTimeoutId());
                                        }

                                    }
                                } else {
                                    // Drop 2
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("Drop PD-2 ").append(msg.getClass()).append(" - ");
                                    sb.append(mappingPolicy).append(":").append(allocationPolicy).append(":").append(filteringPolicy).append(" - ");
                                    if (msg instanceof VodMsg) {
                                        VodMsg vm = (VodMsg) msg;
                                        sb.append(vm.getVodSource());
                                        sb.append(vm.getVodDestination());
                                    } else if (msg instanceof RelayMsgNetty.Response) {
                                        RelayMsgNetty.Response vm = (RelayMsgNetty.Response) msg;
                                        sb.append(vm.getVodSource());
                                        sb.append(vm.getVodDestination());
                                    } else if (msg instanceof RelayMsgNetty.Request) {
                                        RelayMsgNetty.Request vm = (RelayMsgNetty.Request) msg;
                                        sb.append(vm.getVodSource());
                                        sb.append(vm.getVodDestination());
                                    } else {
                                        sb.append(msg.getSource());
                                    }
                                    sb.append(getExistingMappingsAsString(map));
                                    logger.warn(compName + sb.toString());
                                    if (msg instanceof HpMsg) {
                                        HpMsg hm = (HpMsg) msg;
                                        logger.debug(compName + "Drop PD-2 " + msg.getSource() + " -- "
                                                + msg.getDestination() + " of type "
                                                + msg.getClass().getCanonicalName() + " - "
                                                + hm.getMsgTimeoutId());
                                    }


                                }
                                break;
                            default:
                                break;
                        }
                    } else {
                        logger.debug(compName + "Lower. No mapping found. Src: " + srcIp + ":"
                                + srcPort + " destPort: "
                                + destPort + " private addr: " + v + " - " + msg.getClass()
                                + "::" + natPublicAddress + "::");
                    }
                }
                if (forward) {
                    // Change dest port and address
                    msg.rewriteDestination(v);
                    delegator.doTrigger(msg, upperNet);

                    logger.debug(compName + "FORWARDING the packet in src " + msg.getSource()
                            + " dest " + msg.getDestination()
                            + " timeoutId " + msg.getTimeoutId()
                            + " msg class "
                            + shortClassName(msg.getClass().toString()));
                } else {
                    logger.debug(compName + "FILTERING "
                            + filteringPolicy
                            + " Dropped Msg. Src:"
                            + msg.getSource() + " - "
                            + " dest:"
                            + msg.getDestination() + " message class: "
                            + shortClassName(msg.getClass().toString())
                            + getExistingMappingsAsString(privateEndPointToDestinationTable.get(v)));
            if (msg instanceof HpMsg) {
                HpMsg hm = (HpMsg) msg;
                logger.debug(compName + msg.getSource() + " -- "
                        + msg.getDestination() + " FILTERING "
                        + msg.getClass().getCanonicalName() + " - "
                        + hm.getMsgTimeoutId());
            }
                }
            }

        }
    };
    /**
     *
     */
    Handler<RuleCleanupTimeout> handleRuleCleanupTimeout = new Handler<RuleCleanupTimeout>() {
        @Override
        public void handle(RuleCleanupTimeout event) {
            logger.trace(compName + "GC running... ruleLife Time is " + ruleLifeTime + " time(sec)" + System.currentTimeMillis() / 1000);
            Set<Integer> removed = new HashSet<Integer>();
            for (Integer mappedPort : timers.keySet()) {
                if (isExpired(mappedPort)) {
                    logger.trace(compName + "removing  rule for port " + mappedPort);
                    // Remove from timers
                    removed.add(mappedPort);
                    // Remove from open endpoint
                    Address privateEndPoint = pubPortToPrivateAddr.remove(mappedPort);
                    // Look at reverse mapping
                    Map<InetAddress, Map<Integer, Integer>> map = privateEndPointToDestinationTable.get(privateEndPoint);

                    Set<InetAddress> publicIPsToBeRemoved = new HashSet<InetAddress>();
                    for (InetAddress publicEndPoint : map.keySet()) {
                        Set<Integer> destinationPortsToBeRemoved = new HashSet<Integer>();
                        Map<Integer, Integer> iMap = map.get(publicEndPoint);
                        for (Integer destPort : iMap.keySet()) {
                            if (iMap.get(destPort).equals(mappedPort)) {
                                destinationPortsToBeRemoved.add(destPort);
                            }
                        }

                        for (Integer destPort : destinationPortsToBeRemoved) {
                            iMap.remove(destPort);
                        }


                        if (iMap.isEmpty()) {
                            publicIPsToBeRemoved.add(publicEndPoint);
                        }
                    }

                    for (InetAddress publicIP : publicIPsToBeRemoved) {
                        map.remove(publicIP);
                    }

                    // Remove reverse mapping if empty
                    if (map.isEmpty()) {
                        privateEndPointToDestinationTable.remove(privateEndPoint);
                    }
                }

                String openPorts = "   Open Ports at (" + System.currentTimeMillis() / 1000 + ") :";
                for (int port : pubPortToPrivateAddr.keySet()) {

                    openPorts += ", " + port + "(" + timers.get(port) / 1000 + ")";
                }
                logger.trace(compName + "map size is " + privateEndPointToDestinationTable.size() + openPorts);
            }
            for (Integer port : removed) {
                // Remove from timers
                timers.remove(port);
            }
        }
    };
    Handler<NatPortBindResponse> handleNatPortBindResponse = new Handler<NatPortBindResponse>() {
        @Override
        public void handle(NatPortBindResponse event) {

            int portToMap = event.getPort();
            BindingSession session = event.getSession();
            Address v = session.getV();
            InetAddress publicEndPoint = session.getPublicEndPoint();
            int dstPort = session.getDstPort();
            RewriteableMsg msg = session.getMsg();

            if (event.getStatus() != NatPortBindResponse.Status.SUCCESS) {
                int numRetries = event.getNumRetries();
                if (numRetries < 3) {
                    mapPort(msg, v, publicEndPoint, dstPort, natPublicAddress, dstPort, numRetries + 1);
                } else {
                    // remove entries from tables
                    Address privateAddr = pubPortToPrivateAddr.remove(portToMap);
                    if (privateAddr != null) {
                        logger.warn(compName + "Removing nat port {} for private {}", portToMap, privateAddr);
                        Map<InetAddress, Map<Integer, Integer>> mapping = privateEndPointToDestinationTable.remove(privateAddr);
                        if (mapping != null) {
                            mapping.remove(publicEndPoint);
                        } else {
                            logger.warn(compName + "No privateEndPointToDestinationTable entry to remove for " + mapping);
                        }
                    } else {
                        logger.warn(compName + "No pubPortToPrivateAddr entry for " + portToMap);
                    }


                    if (event.getStatus() == NatPortBindResponse.Status.PORT_ALREADY_BOUND) {
                        // Something wrong here.
                        // TODO - change this to a normal PortBindResponse.Failed event
                        delegator.doTrigger(new Fault(new IllegalStateException("Could not bind port "
                                + portToMap + " . Port already bound.")), control);
                    }

                }
                return;
            }


            // after mapPort()
            // Create/Renew Timestamp
            timers.put(portToMap, System.currentTimeMillis());
            // Forward the message the public address as the source address and mapped port as the source port
            Address newSourceAddress = new Address(natPublicAddress,
                    portToMap, msg.getSource().getId());

            // TODO - Do i need to rewrite the source address, as Netty will rewrite
            // it for me when the message is received??
            // However, I should send the message from the Nat's ip/port, as Netty
            // will not send a message from an IP address and port that it is not bound to.
            msg.rewritePublicSource(newSourceAddress);

            delegator.doTrigger(msg, network);

            logger.debug(compName + " NatPortBindResponse packet send to lower port. private src:"
                    + " public src:"
                    + msg.getSource() + " dest: "
                    + msg.getDestination()
                    + " timeoutId: " + msg.getTimeoutId()
                    + " message class: "
                    + msg.getClass().toString());
        }
    };

    public void mapPort(RewriteableMsg msg, Address v, InetAddress destIp,
            int dstPort, InetAddress srcIp, int srcPort, int numRetries) {
        int portToMap = 0;
        // Check if others mapped that port
        switch (allocationPolicy) {
            case PORT_PRESERVATION:
                Address mappedEnd = pubPortToPrivateAddr.get(srcPort);
                if (mappedEnd == null && numRetries == 0) {
                    // Nobody is using that port - port binding didn't fail
                    portToMap = srcPort;
                } else {
                    // Somebody is already using that port
                    if ((clashingOverrides || isExpired(srcPort)) && numRetries == 0) {
                        /*
                         * Remove mapping from NAT table
                         */
                        // Mapping will be automatically overridden
                        portToMap = srcPort;
                    } else {
                        switch (alternativePortAllocationPolicy) {
                            case PORT_CONTIGUITY:
                                portToMap = getContiguousPort();
                                break;
                            case RANDOM:
                                portToMap = getRandomPort_UnusedIfPossible();
                                break;
                            default:
                                portToMap = getRandomPort_UnusedIfPossible();
                                break;
                        }
                    }
                }
                break;
            case PORT_CONTIGUITY:
                portToMap = getContiguousPort();
                break;
            case RANDOM:
                portToMap = getRandomPort_UnusedIfPossible();
                break;
            default:
                break;
        }

        logger.trace(compName + "Requested mapping from private port " + v.getPort() + " to public port: " + portToMap);
        PortBindRequest req = new PortBindRequest(99 /* natId */, portToMap);
        BindingSession session = new BindingSession(v, destIp, dstPort, msg);
        NatPortBindResponse resp = new NatPortBindResponse(req, session, numRetries);
        req.setResponse(resp);
        delegator.doTrigger(req, lowerNetControl);

        // Map the new one, mapping overrides current mapping
        // TODO: should not override
        if (pubPortToPrivateAddr.containsKey(portToMap)) {
            logger.warn(compName + "GOING TO OVERRIDE private mapping: {}/{} . Existing mapping: "
                    + pubPortToPrivateAddr.get(portToMap), portToMap, v);
        }
        pubPortToPrivateAddr.put(portToMap, v);
        Map<InetAddress, Map<Integer, Integer>> map = null;
        if (!privateEndPointToDestinationTable.containsKey(v)) {

            map = new HashMap<InetAddress, Map<Integer, Integer>>();
            privateEndPointToDestinationTable.put(v, map);

        } else {
            map = privateEndPointToDestinationTable.get(v);
        }
        Map<Integer, Integer> s;
        if (map.containsKey(destIp)) {
            s = map.get(destIp);
        } else {
            s = new HashMap<Integer, Integer>();
        }
        s.put(dstPort, portToMap);
        map.put(destIp, s);
        logger.trace(compName + "successfully mapped new port " + portToMap + " From: " + v + " To: " + destIp + ":" + dstPort);
    }

    private boolean isExpired(int port) {
        boolean contains = timers.containsKey(port);
        return isExpirationTimerEnabled && contains
                && (System.currentTimeMillis() - timers.get(port) > ruleLifeTime);
    }

    private int getRandomPort_UnusedIfPossible() {
        boolean found = false;
        int ret = 0;
        int limit = pubPortToPrivateAddr.size();
        int counter = 0;
        do {
            ret = rand.nextInt(endPortRange - startPortRange) + startPortRange;
            counter++;
            if (!pubPortToPrivateAddr.containsKey(ret) || counter > limit) {
                found = true;
            }
        } while (!found);
        return ret;
    }

    private int getContiguousPort() {
        int ret = portCounter++;
        if (portCounter > endPortRange) {
            // wrap-around to 30-40,000.
            ret = portCounter =
                    rand.nextInt(endPortRange - startPortRange) + startPortRange;
        }
        return ret;
    }

    private void reset() {
        pubPortToPrivateAddr = new HashMap<Integer, Address>();
        privateEndPointToDestinationTable = new HashMap<Address, Map<InetAddress, Map<Integer, Integer>>>();
        timers = new HashMap<Integer, Long>();
        rand = new Random(randomPortSeed);
        portCounter = startPortRange;
    }

    private String shortClassName(String longClassName) {
        StringBuffer fullClassName = new StringBuffer(longClassName);
        StringBuffer reversed = fullClassName.reverse();
        String reversedName = reversed.substring(0, reversed.indexOf("."));
        String className = (new StringBuffer(reversedName)).reverse().toString();
        return className;
    }

    @Override
    public void stop(Stop event) {
        logger.debug(compName + " nat gateway stopped");
    }
}
