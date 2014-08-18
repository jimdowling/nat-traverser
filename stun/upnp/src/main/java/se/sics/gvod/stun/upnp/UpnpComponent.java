package se.sics.gvod.stun.upnp;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import org.cybergarage.upnp.UPnP;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.stun.upnp.events.MapPortRequest;
import se.sics.gvod.stun.upnp.events.MapPortsRequest;
import se.sics.gvod.stun.upnp.events.MapPortsResponse;
import se.sics.gvod.stun.upnp.events.MappedPortsChanged;
import se.sics.gvod.stun.upnp.events.ShutdownUpnp;
import se.sics.gvod.stun.upnp.events.UnmapPortsRequest;
import se.sics.gvod.stun.upnp.events.UnmapPortsResponse;
import se.sics.gvod.stun.upnp.events.UpnpGetPublicIpRequest;
import se.sics.gvod.stun.upnp.events.UpnpGetPublicIpResponse;
import se.sics.gvod.stun.upnp.events.UpnpInit;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Start;
import se.sics.kompics.Stop;

public final class UpnpComponent extends ComponentDefinition
        implements ForwardPortCallback {

    private static final Logger logger = LoggerFactory.getLogger(UpnpComponent.class);
    private Negative<UpnpPort> upnpPort
            = negative(UpnpPort.class);
    private int discoveryTimeout = 3000; // ms
    private int rootDeviceTimeout = 2500; // ms
    private Cybergarage upnp;
    private InetAddress publicIp;
    private UpnpComponent component;
    private boolean upnpSetup = false;

    public UpnpComponent(UpnpInit init) {
        component = this;
        doInit(init);
        subscribe(handleStart, control);
        subscribe(handleStop, control);

//        subscribe(handleMapPortRequest, upnpPort);
        subscribe(handleMapPortsRequest, upnpPort);
        subscribe(handleUnmapPortRequest, upnpPort);
        subscribe(handleUpnpGetPublicIpRequest, upnpPort);
        subscribe(handleShutdownUpnp, upnpPort);
    }

    private void doInit(UpnpInit init) {
        upnpSetup = true;
        discoveryTimeout = init.getDiscoveryTimeout();
        rootDeviceTimeout = init.getRootDeviceTimeout();
        UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
        Random r = new Random(System.currentTimeMillis());
        int multicastPort = 50000 + r.nextInt(100);
        upnp = new Cybergarage(component, multicastPort);
    }

    public Handler<Start> handleStart = new Handler<Start>() {

        @Override
        public void handle(Start event) {
            try {
                upnp.init();
            } catch (RuntimeException e) {
                logger.warn(e.toString());
                upnpSetup = false;
            }
            try {
                Thread.currentThread().sleep(discoveryTimeout);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(UpnpComponent.class.getName()).log(Level.SEVERE, null, ex);
            }

            DetectedIP[] ips = upnp.getAddress();
            if (ips != null) {
                for (DetectedIP ip : ips) {
                    publicIp = ip.publicAddress;
                    int mtu = ip.mtu;
                }
            }
        }
    };
    public Handler<Stop> handleStop = new Handler<Stop>() {

        @Override
        public void handle(Stop event) {
            if (upnpSetup) {
                upnp.terminate();
            }
        }
    };
    public Handler<MapPortsRequest> handleMapPortsRequest = new Handler<MapPortsRequest>() {

        @Override
        public void handle(MapPortsRequest event) {

            Map<Integer, Integer> mapPorts = event.getPrivatePublicPorts();
            Set<Integer> privatePorts = mapPorts.keySet();

            logger.debug("MapPortsRequest for ports recvd: " + privatePorts.size());
            if (upnpSetup == false) {
                trigger(new MapPortsResponse(event, mapPorts,
                        publicIp, 0 /*fake id*/, false), upnpPort);
                return;
            }

            MapPortRequest.Protocol protocol = event.getProtocol();
            int protocolType = -1;
            String protocolName = "";
            if (protocol == MapPortRequest.Protocol.UDP) {
                protocolType = ForwardPort.PROTOCOL_UDP_IPV4;
                protocolName = "udp";
            } else if (protocol == MapPortRequest.Protocol.TCP) {
                protocolType = ForwardPort.PROTOCOL_TCP_IPV4;
                protocolName = "tcp";
            }
            CopyOnWriteArraySet<ForwardPort> portsToForwardNow = new CopyOnWriteArraySet<ForwardPort>();
            for (int privatePort : privatePorts) {
                int requestedPort = mapPorts.get(privatePort);

                String mappingName = "kompics " + protocolName + " " + requestedPort;
                ForwardPort fp = new ForwardPort(mappingName, false,
                        protocolType, requestedPort);
                portsToForwardNow.add(fp);
                registerCallbackChangeMappedPort(mappingName, requestedPort, protocolType);
            }
            Map<ForwardPort, Boolean> res = upnp.registerPorts(portsToForwardNow);
            boolean status = true;
            for (ForwardPort fp : res.keySet()) {
                if (res.get(fp) == false) {
                    status = false;
                }
            }
            if (status == false) {
                // just remove all port mappings on failure
                upnp.unregisterPortMappings();
            }

            logger.info("Sending MapPortsResponse with status " + status);
            trigger(new MapPortsResponse(event, mapPorts,
                    publicIp, 0/*fake id*/, status), upnpPort);
        }
    };

//    private boolean mapPort(int privatePort, int requestedPort, MapPortRequest.Protocol protocol) {
//        int protocolType = -1;
//        String protocolName = "";
//        if (protocol == MapPortRequest.Protocol.UDP) {
//            protocolType = ForwardPort.PROTOCOL_UDP_IPV4;
//            protocolName = "udp";
//        } else if (protocol == MapPortRequest.Protocol.TCP) {
//            protocolType = ForwardPort.PROTOCOL_TCP_IPV4;
//            protocolName = "tcp";
//        }
//        /**
//         * Don't use IpV6 - false.
//         */
//        ForwardPort fp = new ForwardPort("kompics upnp port " + requestedPort,
//                false, protocolType,
//                requestedPort);
//        String mappingName = "port mapping " + requestedPort;
//        boolean res = false;
////                upnp.tryAddMapping(protocolName, privatePort, mappingName, fp);
//        if (res == true) {
//            registerCallbackChangeMappedPort(mappingName, requestedPort, protocolType);
//        }
//
//        return res;
//    }
//    public Handler<MapPortRequest> handleMapPortRequest = new Handler<MapPortRequest>() {
//
//        @Override
//        public void handle(MapPortRequest event) {
//            int privatePort = event.getPrivatePort();
//            int requestedPort = event.getRequestedPort();
//            logger.debug("MapPortRequest for : " + requestedPort);
//
//            if (upnpSetup == false) {
//                trigger(new MapPortResponse(event, event.getPrivatePort(),
//                        requestedPort, publicIp, 0 /*fake id*/, false), upnpPort);
//                return;
//            }
//            MapPortRequest.Protocol protocol = event.getProtocol();
//
//            boolean res = mapPort(privatePort, privatePort, protocol);
//
//            trigger(new MapPortResponse(event, event.getPrivatePort(),
//                    requestedPort, publicIp, 0/*fake id*/, res), upnpPort);
//
//        }
//    };
    private void registerCallbackChangeMappedPort(String name, int port, int protocol) {
        CopyOnWriteArraySet<ForwardPort> ports = new CopyOnWriteArraySet<ForwardPort>();
        ports.add(new ForwardPort(name, false, protocol, port));
//        upnp.onChangePublicPorts(ports, this);
    }
    public Handler<UnmapPortsRequest> handleUnmapPortRequest
            = new Handler<UnmapPortsRequest>() {

                public void handle(UnmapPortsRequest event) {
                    logger.debug("Unmapping UpnP Ports");
                    if (upnpSetup == false) {
                        trigger(new UnmapPortsResponse(event, false), upnpPort);
                        return;
                    }
                    boolean success = true;

                    upnp.unregisterPortMappings();

                    trigger(new UnmapPortsResponse(event, success), upnpPort);

                }
            };
    public Handler<UpnpGetPublicIpRequest> handleUpnpGetPublicIpRequest
            = new Handler<UpnpGetPublicIpRequest>() {

                @Override
                public void handle(UpnpGetPublicIpRequest event) {

                    if (upnpSetup == false) {
                        trigger(new UpnpGetPublicIpResponse(event, null), upnpPort);
                    } else {
                        trigger(new UpnpGetPublicIpResponse(event, publicIp), upnpPort);
                    }
                }
            };
    public Handler<ShutdownUpnp> handleShutdownUpnp
            = new Handler<ShutdownUpnp>() {

                @Override
                public void handle(ShutdownUpnp event) {

                    if (upnpSetup) {
                        upnp.terminate();
                    }
                }
            };

    public void deviceNotifyReceived(SSDPPacket ssdpPacket) {

        // TODO trigger some results back to client
        logger.debug("Device notified: " + ssdpPacket.toString());
//        trigger(new , upnp);

    }

    @Override
    public void portForwardStatus(Map statuses) {
        Map<Integer, ForwardPortStatus> changedPorts
                = new HashMap<Integer, ForwardPortStatus>();
        Set keys = statuses.keySet();
        for (Object key : keys) {
            ForwardPort fp = (ForwardPort) key;
            ForwardPortStatus status = (ForwardPortStatus) statuses.get(key);
            changedPorts.put(fp.portNumber, status);

//          trigger(new MapPortResponse(event, event.getPrivatePort(),
//                    requestedPort, publicIp, 0/*fake id*/, res), upnpPort);
        }

        trigger(new MappedPortsChanged(changedPorts), upnpPort);
    }
}
