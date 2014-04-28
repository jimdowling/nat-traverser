/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.upnp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ActionList;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.ServiceList;
import org.cybergarage.upnp.ServiceStateTable;
import org.cybergarage.upnp.StateVariable;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.cybergarage.upnp.device.NotifyListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * some code has been borrowed from Freenet and Limewire : @see com.limegroup.gnutella.UPnPManager
 *
 * @see http://www.upnp.org/
 * @see http://en.wikipedia.org/wiki/Universal_Plug_and_Play
 *
 * TODO: Support multiple IGDs ?
 * TODO: Advertise the node like the MDNS plugin does
 * TODO: Implement EventListener and react on ip-change
 */
public class Cybergarage extends ControlPoint implements DeviceChangeListener,
        NotifyListener {

    private static final Logger logger = LoggerFactory.getLogger(Cybergarage.class);
    /** some schemas */
    private static final String ROUTER_DEVICE = "urn:schemas-upnp-org:device:InternetGatewayDevice:1";
    private static final String WAN_DEVICE = "urn:schemas-upnp-org:device:WANDevice:1";
    private static final String WANCON_DEVICE = "urn:schemas-upnp-org:device:WANConnectionDevice:1";
    private static final String WAN_IP_CONNECTION = "urn:schemas-upnp-org:service:WANIPConnection:1";
    private static final String WAN_PPP_CONNECTION = "urn:schemas-upnp-org:service:WANPPPConnection:1";
    private Device _router;
    private Service _service;
    private boolean isDisabled = false; // We disable the plugin if more than one IGD is found
    private final Object lock = new Object();
    /** List of ports we want to forward */
    private CopyOnWriteArraySet<ForwardPort> portsToForward;
    /** List of ports we have actually forwarded */
    private CopyOnWriteArraySet<ForwardPort> portsForwarded;
    /** Callback to call when a forward fails or succeeds */
    private ForwardPortCallback forwardCallback;
    private final UpnpComponent component;


    public Cybergarage(UpnpComponent component, int multicastPort) {
        super(multicastPort);
        portsForwarded = new CopyOnWriteArraySet<ForwardPort>();
        this.component = component;
        addDeviceChangeListener(this);
        addNotifyListener(this);
    }

    public void init() {
        super.start();
    }

    public void terminate() {
        unregisterPortMappings();
        super.stop();
    }

    public DetectedIP[] getAddress() {
        logger.trace("UP&P.getAddress() is called \\o/");
        if (isDisabled) {
            logger.debug("Plugin has been disabled previously, ignoring request.");
            return null;
        } else if (!isNATPresent()) {
            logger.debug("No UP&P device found, detection of the external ip address using the plugin has failed");
            return null;
        }

        DetectedIP result = null;
        final String natAddress = getNATAddress();
        try {
            // TODO: report a different connection type if port forwarding has succeeded?
            result = new DetectedIP(InetAddress.getByName(natAddress), DetectedIP.NOT_SUPPORTED);

            logger.debug("Successful UP&P discovery :" + result);
            return new DetectedIP[]{result};
        } catch (UnknownHostException e) {
            logger.error("Caught an UnknownHostException resolving " + natAddress, e);
            return null;
        }
    }

    public void deviceAdded(Device dev) {
        synchronized (lock) {
            if (isDisabled) {
                logger.debug("Plugin has been disabled previously, ignoring new device.");
                return;
            }
        }
        if (!ROUTER_DEVICE.equals(dev.getDeviceType()) || !dev.isRootDevice()) {
            return; // Silently ignore non-IGD devices
        } else if (isNATPresent()) {
            logger.error("We got a second IGD on the network! the plugin doesn't handle that: let's disable it.");
            isDisabled = true;

            synchronized (lock) {
                _router = null;
                _service = null;
            }

            stop();
            return;
        }

        logger.debug("UP&P IGD found : " + dev.getFriendlyName());
        synchronized (lock) {
            _router = dev;
        }

        discoverService();
        // We have found the device we need: stop the listener thread
        stop();
        synchronized (lock) {
            if (_service == null) {
                logger.error("The IGD device we got isn't suiting our needs, let's disable the plugin");
                isDisabled = true;
                _router = null;
                return;
            }
        }
//        registerPortMappings();
    }

    private void registerPortMappings() {
        CopyOnWriteArraySet ports;
        synchronized (lock) {
            ports = portsToForward;
        }
        if (ports == null) {
            return;
        }
        registerPorts(ports);
    }

    /**
     * Traverses the structure of the router device looking for the port mapping service.
     */
    private void discoverService() {
        synchronized (lock) {
            for (Iterator iter = _router.getDeviceList().iterator(); iter.hasNext();) {
                Device current = (Device) iter.next();
                if (!current.getDeviceType().equals(WAN_DEVICE)) {
                    continue;
                }

                DeviceList l = current.getDeviceList();
                for (int i = 0; i < current.getDeviceList().size(); i++) {
                    Device current2 = l.getDevice(i);
                    if (!current2.getDeviceType().equals(WANCON_DEVICE)) {
                        continue;
                    }

                    _service = current2.getService(WAN_PPP_CONNECTION);
                    if (_service == null) {
                        logger.debug(_router.getFriendlyName() + " doesn't seems to be using PPP; we won't be able to extract bandwidth-related informations out of it.");
                        _service = current2.getService(WAN_IP_CONNECTION);
                        if (_service == null) {
                            logger.error(_router.getFriendlyName() + " doesn't export WAN_IP_CONNECTION either: we won't be able to use it!");
                        }
                    }

                    return;
                }
            }
        }
    }

    private boolean tryAddMapping(String protocol, int port, String description, ForwardPort fp) {
        logger.debug("Registering a port mapping for " + port + "/" + protocol);
        int nbOfTries = 0;
        boolean isPortForwarded = false;
        while (nbOfTries++ < 1) {
            isPortForwarded = addMapping(protocol, port, "GVod 0.1 " + description, fp);
            if (isPortForwarded) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        logger.debug((isPortForwarded ? "Mapping is successful!" : "Mapping has failed!") + " (" + nbOfTries + " tries)");
        return isPortForwarded;
    }

    public void unregisterPortMappings() {
        CopyOnWriteArraySet<ForwardPort> ports;
        synchronized (lock) {
            ports = portsForwarded;
        }
        this.unregisterPorts(ports);
    }

    public void deviceRemoved(Device dev) {
        synchronized (lock) {
            if (_router == null) {
                return;
            }
            if (_router.equals(dev)) {
                _router = null;
                _service = null;
            }
        }
    }

    /**
     * @return whether we are behind an UPnP-enabled NAT/router
     */
    public boolean isNATPresent() {
        return _router != null && _service != null;
    }

    /**
     * @return the external address the NAT thinks we have.  Blocking.
     * null if we can't find it.
     */
    public String getNATAddress() {
        if (!isNATPresent()) {
            return null;
        }

        Action getIP = _service.getAction("GetExternalIPAddress");
        if (getIP == null || !getIP.postControlAction()) {
            return null;
        }

        return (getIP.getOutputArgumentList().getArgument("NewExternalIPAddress")).getValue();
    }

    /**
     * @return the reported upstream bit rate in bits per second. -1 if it's not available. Blocking.
     */
    public int getUpstramMaxBitRate() {
        if (!isNATPresent()) {
            return -1;
        }

        Action getIP = _service.getAction("GetLinkLayerMaxBitRates");
        if (getIP == null || !getIP.postControlAction()) {
            return -1;
        }

        return Integer.valueOf(getIP.getOutputArgumentList().getArgument("NewUpstreamMaxBitRate").getValue());
    }

    /**
     * @return the reported downstream bit rate in bits per second. -1 if it's not available. Blocking.
     */
    public int getDownstreamMaxBitRate() {
        if (!isNATPresent()) {
            return -1;
        }

        Action getIP = _service.getAction("GetLinkLayerMaxBitRates");
        if (getIP == null || !getIP.postControlAction()) {
            return -1;
        }

        return Integer.valueOf(getIP.getOutputArgumentList().getArgument("NewDownstreamMaxBitRate").getValue());
    }

    private void listStateTable(Service serv, StringBuffer sb) {
        ServiceStateTable table = serv.getServiceStateTable();
        sb.append("<div><small>");
        for (int i = 0; i < table.size(); i++) {
            StateVariable current = table.getStateVariable(i);
            sb.append(current.getName() + " : " + current.getValue() + "<br>");
        }
        sb.append("</small></div>");
    }

    private void listActionsArguments(Action action, StringBuffer sb) {
        ArgumentList ar = action.getArgumentList();
        for (int i = 0; i < ar.size(); i++) {
            Argument argument = ar.getArgument(i);
            if (argument == null) {
                continue;
            }
            sb.append("<div><small>argument (" + i + ") :" + argument.getName() + "</small></div>");
        }
    }

    private void listActions(Service service, StringBuffer sb) {
        ActionList al = service.getActionList();
        for (int i = 0; i < al.size(); i++) {
            Action action = al.getAction(i);
            if (action == null) {
                continue;
            }
            sb.append("<div>action (" + i + ") :" + action.getName());
            listActionsArguments(action, sb);
            sb.append("</div>");
        }
    }

    private String toString(String action, String Argument, Service serv) {
        Action getIP = serv.getAction(action);
        if (getIP == null || !getIP.postControlAction()) {
            return null;
        }

        Argument ret = getIP.getOutputArgumentList().getArgument(Argument);
        return ret.getValue();
    }

    // TODO: extend it! RTFM
    private void listSubServices(Device dev, StringBuffer sb) {
        ServiceList sl = dev.getServiceList();
        for (int i = 0; i < sl.size(); i++) {
            Service serv = sl.getService(i);
            if (serv == null) {
                continue;
            }
            sb.append("<div>service (" + i + ") : " + serv.getServiceType() + "<br>");
            if ("urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1".equals(serv.getServiceType())) {
                sb.append("WANCommonInterfaceConfig");
                sb.append(" status: " + toString("GetCommonLinkProperties", "NewPhysicalLinkStatus", serv));
                sb.append(" type: " + toString("GetCommonLinkProperties", "NewWANAccessType", serv));
                sb.append(" upstream: " + toString("GetCommonLinkProperties", "NewLayer1UpstreamMaxBitRate", serv));
                sb.append(" downstream: " + toString("GetCommonLinkProperties", "NewLayer1DownstreamMaxBitRate", serv) + "<br>");
            } else if ("urn:schemas-upnp-org:service:WANPPPConnection:1".equals(serv.getServiceType())) {
                sb.append("WANPPPConnection");
                sb.append(" status: " + toString("GetStatusInfo", "NewConnectionStatus", serv));
                sb.append(" type: " + toString("GetConnectionTypeInfo", "NewConnectionType", serv));
                sb.append(" upstream: " + toString("GetLinkLayerMaxBitRates", "NewUpstreamMaxBitRate", serv));
                sb.append(" downstream: " + toString("GetLinkLayerMaxBitRates", "NewDownstreamMaxBitRate", serv) + "<br>");
                sb.append(" external IP: " + toString("GetExternalIPAddress", "NewExternalIPAddress", serv) + "<br>");
            } else if ("urn:schemas-upnp-org:service:Layer3Forwarding:1".equals(serv.getServiceType())) {
                sb.append("Layer3Forwarding");
                sb.append("DefaultConnectionService: " + toString("GetDefaultConnectionService", "NewDefaultConnectionService", serv));
            } else if (WAN_IP_CONNECTION.equals(serv.getServiceType())) {
                sb.append("WANIPConnection");
                sb.append(" status: " + toString("GetStatusInfo", "NewConnectionStatus", serv));
                sb.append(" type: " + toString("GetConnectionTypeInfo", "NewConnectionType", serv));
                sb.append(" external IP: " + toString("GetExternalIPAddress", "NewExternalIPAddress", serv) + "<br>");
            } else if ("urn:schemas-upnp-org:service:WANEthernetLinkConfig:1".equals(serv.getServiceType())) {
                sb.append("WANEthernetLinkConfig");
                sb.append(" status: " + toString("GetEthernetLinkStatus", "NewEthernetLinkStatus", serv) + "<br>");
            } else {
                sb.append("~~~~~~~ " + serv.getServiceType());
            }
            listActions(serv, sb);
            listStateTable(serv, sb);
            sb.append("</div>");
        }
    }

    private void listSubDev(String prefix, Device dev, StringBuffer sb) {
        sb.append("<div><p>Device : " + dev.getFriendlyName() + " - " + dev.getDeviceType() + "<br>");
        listSubServices(dev, sb);

        DeviceList dl = dev.getDeviceList();
        for (int j = 0; j < dl.size(); j++) {
            Device subDev = dl.getDevice(j);
            if (subDev == null) {
                continue;
            }

            sb.append("<div>");
            listSubDev(dev.getFriendlyName(), subDev, sb);
            sb.append("</div></div>");
        }
        sb.append("</p></div>");
    }

//	public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException {
//		if(request.isParameterSet("getDeviceCapabilities")) {
//			final StringBuffer sb = new StringBuffer();
//			sb.append("<html><head><title>UPnP report</title></head><body>");
//			listSubDev("WANDevice", _router, sb);
//			sb.append("</body></html>");
//			return sb.toString();
//		}
//
//		HTMLNode pageNode = pr.getPageMaker().getPageNode("UP&P plugin configuration page", false, null);
//		HTMLNode contentNode = pr.getPageMaker().getContentNode(pageNode);
//
//		if(isDisabled) {
//			HTMLNode disabledInfobox = contentNode.addChild("div", "class", "infobox infobox-error");
//			HTMLNode disabledInfoboxHeader = disabledInfobox.addChild("div", "class", "infobox-header");
//			HTMLNode disabledInfoboxContent = disabledInfobox.addChild("div", "class", "infobox-content");
//
//			disabledInfoboxHeader.addChild("#", "UP&P plugin report");
//			disabledInfoboxContent.addChild("#", "The plugin has been disabled; Do you have more than one UP&P IGD on your LAN ?");
//			return pageNode.generate();
//		} else if(!isNATPresent()) {
//			HTMLNode notFoundInfobox = contentNode.addChild("div", "class", "infobox infobox-warning");
//			HTMLNode notFoundInfoboxHeader = notFoundInfobox.addChild("div", "class", "infobox-header");
//			HTMLNode notFoundInfoboxContent = notFoundInfobox.addChild("div", "class", "infobox-content");
//
//			notFoundInfoboxHeader.addChild("#", "UP&P plugin report");
//			notFoundInfoboxContent.addChild("#", "The plugin hasn't found any UP&P aware, compatible device on your LAN.");
//			return pageNode.generate();
//		}
//
//		HTMLNode foundInfobox = contentNode.addChild("div", "class", "infobox infobox-normal");
//		HTMLNode foundInfoboxHeader = foundInfobox.addChild("div", "class", "infobox-header");
//		HTMLNode foundInfoboxContent = foundInfobox.addChild("div", "class", "infobox-content");
//
//		// FIXME L10n!
//		foundInfoboxHeader.addChild("#", "UP&P plugin report");
//		foundInfoboxContent.addChild("p", "The following device has been found : ").addChild("a", "href", "?getDeviceCapabilities").addChild("#", _router.getFriendlyName());
//		foundInfoboxContent.addChild("p", "Our current external ip address is : " + getNATAddress());
//		int downstreamMaxBitRate = getDownstreamMaxBitRate();
//		int upstreamMaxBitRate = getUpstramMaxBitRate();
//		if(downstreamMaxBitRate > 0)
//			foundInfoboxContent.addChild("p", "Our reported max downstream bit rate is : " + getDownstreamMaxBitRate()+ " bits/sec");
//		if(upstreamMaxBitRate > 0)
//			foundInfoboxContent.addChild("p", "Our reported max upstream bit rate is : " + getUpstramMaxBitRate()+ " bits/sec");
//		synchronized(lock) {
//			if(portsToForward != null) {
//				for(Iterator i=portsToForward.iterator();i.hasNext();) {
//					ForwardPort port = (ForwardPort) i.next();
//					if(portsForwarded.contains(port)) {
//						foundInfoboxContent.addChild("p", "The "+port.name+" port "+port.portNumber+" / "+port.protocol+" has been forwarded successfully.");
//					} else {
//						foundInfoboxContent.addChild("p", "The "+port.name+" port "+port.portNumber+" / "+port.protocol+" has not been forwarded.");
//					}
//				}
//			}
//		}
//
//		return pageNode.generate();
//	}
//
//	public String handleHTTPPost(HTTPRequest request) throws PluginHTTPException {
//		return null;
//	}
//
//	public String handleHTTPPut(HTTPRequest request) throws PluginHTTPException {
//		return null;
//	}
    private boolean addMapping(String protocol, int port, String description, ForwardPort fp) {
        if (isDisabled || !isNATPresent() || _router == null) {
            return false;
        }

        // Just in case...
        removeMapping(protocol, port, fp, true);

        Action add = _service.getAction("AddPortMapping");
        if (add == null) {
            logger.error("Couldn't find AddPortMapping action!");
            return false;
        }


        add.setArgumentValue("NewRemoteHost", "");
        add.setArgumentValue("NewExternalPort", port);
        add.setArgumentValue("NewInternalClient", _router.getInterfaceAddress());
        add.setArgumentValue("NewInternalPort", port);
        add.setArgumentValue("NewProtocol", protocol);
        add.setArgumentValue("NewPortMappingDescription", description);
        add.setArgumentValue("NewEnabled", "1");
        add.setArgumentValue("NewLeaseDuration", 10*1000);
//        add.setArgumentValue("NewLeaseDuration", 0);

        if (add.postControlAction()) {
            synchronized (lock) {
                portsForwarded.add(fp);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean removeMapping(String protocol, int port, ForwardPort fp, boolean noLog) {
        if (isDisabled || !isNATPresent()) {
            return false;
        }

        Action remove = _service.getAction("DeletePortMapping");
        if (remove == null) {
            logger.error("Couldn't find DeletePortMapping action!");
            return false;
        }

        // remove.setArgumentValue("NewRemoteHost", "");
        remove.setArgumentValue("NewExternalPort", port);
        remove.setArgumentValue("NewProtocol", protocol);

        boolean retval = remove.postControlAction();
        synchronized (lock) {
            portsForwarded.remove(fp);
        }

        if (!noLog) {
            logger.debug("UPnP: Removed mapping for " + fp.name + " " + port + " / " + protocol);
        }
        return retval;
    }

    public void onChangePublicPorts(CopyOnWriteArraySet<ForwardPort> ports, ForwardPortCallback cb) {
        CopyOnWriteArraySet<ForwardPort> portsToDumpNow = null;
        CopyOnWriteArraySet<ForwardPort> portsToForwardNow = null;
        logger.debug("UP&P Forwarding " + ports.size() + " ports...");
        synchronized (lock) {
            if (forwardCallback != null && forwardCallback != cb && cb != null) {
                logger.error("ForwardPortCallback changed from " + forwardCallback + " to " + cb + " - using new value, but this is very strange!");
            }
            forwardCallback = cb;
            if (portsToForward == null || portsToForward.isEmpty()) {
                portsToForward = ports;
                portsToForwardNow = ports;
                portsToDumpNow = null;
            } else if (ports == null || ports.isEmpty()) {
                portsToDumpNow = portsToForward;
                portsToForward = ports;
                portsToForwardNow = null;
            } else {
                // Some ports to keep, some ports to dump
                // Ports in ports but not in portsToForwardNow we must forward
                // Ports in portsToForwardNow but not in ports we must dump
                for (Iterator i = ports.iterator(); i.hasNext();) {
                    ForwardPort port = (ForwardPort) i.next();
                    if (portsToForward.contains(port)) {
                        // We have forwarded it, and it should be forwarded, cool.
                    } else {
                        // Needs forwarding
                        if (portsToForwardNow == null) {
                            portsToForwardNow = new CopyOnWriteArraySet<ForwardPort>();
                        }
                        portsToForwardNow.add(port);
                    }
                }
                for (Iterator i = portsToForward.iterator(); i.hasNext();) {
                    ForwardPort port = (ForwardPort) i.next();
                    if (ports.contains(port)) {
                        // Should be forwarded, has been forwarded, cool.
                    } else {
                        // Needs dropping
                        if (portsToDumpNow == null) {
                            portsToDumpNow = new  CopyOnWriteArraySet<ForwardPort>();
                        }
                        portsToDumpNow.add(port);
                    }
                }
                portsToForward = ports;
            }
            if (_router == null) {
                return; // When one is found, we will do the forwards
            }
        }
        if (portsToDumpNow != null) {
            unregisterPorts(portsToDumpNow);
        }
        if (portsToForwardNow != null) {
            registerPorts(portsToForwardNow);
        }
    }

    public Map<ForwardPort,Boolean> registerPorts(CopyOnWriteArraySet<ForwardPort> portsToForwardNow) {
        logger.info("Register ports: " + portsToForwardNow.size());
        Map<ForwardPort,Boolean> res = new HashMap<ForwardPort,Boolean>();
        for (Iterator<ForwardPort> i = portsToForwardNow.iterator(); i.hasNext();) {
            ForwardPort port = i.next();
            String protocol;
            if (port.protocol == ForwardPort.PROTOCOL_UDP_IPV4) {
                protocol = "UDP";
            } else if (port.protocol == ForwardPort.PROTOCOL_TCP_IPV4) {
                protocol = "TCP";
            } else {
                HashMap map = new HashMap();
                map.put(port, new ForwardPortStatus(ForwardPortStatus.DEFINITE_FAILURE, "Protocol not supported", port.portNumber));
                forwardCallback.portForwardStatus(map);
                res.put(port, false);
                continue;
            }
            if (tryAddMapping(protocol, port.portNumber, port.name, port)) {
                HashMap map = new HashMap();
                map.put(port, new ForwardPortStatus(ForwardPortStatus.MAYBE_SUCCESS, "Port apparently forwarded by UPnP", port.portNumber));
//                forwardCallback.portForwardStatus(map);
                res.put(port, true);
            } else {
                HashMap map = new HashMap();
                map.put(port, new ForwardPortStatus(ForwardPortStatus.PROBABLE_FAILURE, "UPnP port forwarding apparently failed", port.portNumber));
//                forwardCallback.portForwardStatus(map);
                res.put(port, false);
            }
        }
        return res;
    }

    public void unregisterPorts(CopyOnWriteArraySet<ForwardPort> portsToForwardNow) {
        for (Iterator<ForwardPort> i = portsToForwardNow.iterator(); i.hasNext();) {
            ForwardPort port = i.next();
            String proto;
            if (port.protocol == ForwardPort.PROTOCOL_UDP_IPV4) {
                proto = "UDP";
            } else if (port.protocol == ForwardPort.PROTOCOL_TCP_IPV4) {
                proto = "TCP";
            } else {
                // Ignore, we've already complained about it
                continue;
            }
            removeMapping(proto, port.portNumber, port, false);
        }
    }

    @Override
    public void deviceNotifyReceived(SSDPPacket ssdpPacket) {

        component.deviceNotifyReceived(ssdpPacket);

    }
}
