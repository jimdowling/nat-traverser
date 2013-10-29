package se.sics.kompics.nat.utils.getip;

import java.net.Inet6Address;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.ComponentDefinition;

import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.kompics.nat.utils.getip.events.IpChange;

/**
 * A stateful component that gets the current list of IP addresses bound to
 * different network interfaces on this host. A client can register for an
 * IpChange event that is sent when the the list of available IP addresses
 * changes.
 *
 * @author jdowling
 */
public final class ResolveIp extends ComponentDefinition {

    public static final int RECHECK_NETWORK_INTERFACES_PERIOD = 30 * 1000;
    public static final int DISCOVERY_TIMEOUT = 2 * 1000;
    public static final int LEASE_DURATION = 60 * 60 * 1000;
    private static final Logger logger = LoggerFactory.getLogger(ResolveIp.class);
    private Negative<ResolveIpPort> resolveIpPort = negative(ResolveIpPort.class);
    private Positive<Timer> timerPort = positive(Timer.class);
    Set<IpAddrStatus> addrs = new HashSet<IpAddrStatus>();
    private boolean initialized = false;
    private GetIpRequest lastRequest;
    private InetAddress boundIp;

    class IpComparator implements Comparator<IpAddrStatus> {

        @Override
        public int compare(IpAddrStatus obj1, IpAddrStatus obj2) {
            if (obj1 == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
            }
            if (obj1 == obj2 || obj1.equals(obj2)) {
                return 0;
            }

            if (obj1.getAddr() instanceof Inet6Address) {
                return -1;
            }
            if (obj2.getAddr() instanceof Inet6Address) {
                return 1;
            }
            if (obj2.isUp() && !obj1.isUp()) {
                return -1;
            }
            if (obj1.isUp() && !obj2.isUp()) {
                return -1;
            }
            if (obj1.getNetworkPrefixLength() < obj2.getNetworkPrefixLength()) {
                return 1;
            }
            if (obj1.getNetworkPrefixLength() > obj2.getNetworkPrefixLength()) {
                return -1;
            }
            try {
                InetAddress loopbackIp = InetAddress.getByName("127.0.0.1");
                if (obj1.getAddr().equals(loopbackIp)) {
                    return 1;
                } else if (obj2.getAddr().equals(loopbackIp)) {
                    return -1;
                }

                InetAddress localIp = InetAddress.getByName("127.0.1.1");
                if (obj1.getAddr().equals(localIp)) {
                    return 1;
                } else if (obj2.getAddr().equals(localIp)) {
                    return -1;
                }
            } catch (UnknownHostException ex) {
                java.util.logging.Logger.getLogger(ResolveIp.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (obj1.isUp() == true && obj2.isUp() == false) {
                return -1;
            }
            if (obj2.isUp() == true && obj1.isUp() == false) {
                return 1;
            }
            if (obj1.getMtu() > obj2.getMtu()) {
                return -1;
            } else {
                return 1;
            }
        }
    };

    public static class RecheckIpTimeout extends Timeout {

        private final boolean filterTenDot;
        private final boolean filterPrivateIps;
        private final boolean filterLoopback;

        public RecheckIpTimeout(SchedulePeriodicTimeout request,
                boolean filterTenDot,
                boolean filterPrivateIps, boolean filterLoopback) {
            super(request);
            this.filterLoopback = filterLoopback;
            this.filterTenDot = filterTenDot;
            this.filterPrivateIps = filterPrivateIps;
        }

        public boolean isFilterLoopback() {
            return filterLoopback;
        }

        public boolean isFilterTenDot() {
            return filterTenDot;
        }

        public boolean isFilterPrivateIps() {
            return filterPrivateIps;
        }
    }

    public ResolveIp() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        subscribe(handleGetIpRequest, resolveIpPort);
        subscribe(handleRecheckIpTimeout, timerPort);
    }
    private Handler<RecheckIpTimeout> handleRecheckIpTimeout = new Handler<RecheckIpTimeout>() {
        @Override
        public void handle(RecheckIpTimeout event) {
            Set<IpAddrStatus> difference = getLocalNetworkInterfaces(event.isFilterTenDot(), event.isFilterPrivateIps(), event.isFilterLoopback());

            getBoundIpAddr();

            logger.trace("Checked network interfaces for changes. Num changes : "
                    + difference.size());
            if (difference.size() > 0) {
                trigger(new IpChange(new ArrayList<IpAddrStatus>(difference), boundIp), resolveIpPort);
            }
        }
    };

    private void getBoundIpAddr() {
        try {
            boundIp = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(ResolveIp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Set<IpAddrStatus> difference(Set<IpAddrStatus> a,
            Set<IpAddrStatus> b) {

        Set<IpAddrStatus> diff = new HashSet<IpAddrStatus>(a);
        diff.removeAll(b);
        return diff;
    }

    private Set<IpAddrStatus> getLocalNetworkInterfaces(boolean filterTenDot,
            boolean filterPrivateIps, boolean filterLoopback) {
        Set<IpAddrStatus> addresses = new HashSet<IpAddrStatus>();

        Enumeration<NetworkInterface> nis = null;
        try {
            nis = NetworkInterface.getNetworkInterfaces();

            if (nis == null) {
                return addresses;
            }

            printInterfaces();

            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                boolean isVirtual = ni.isVirtual();
                if (isVirtual == true) {
                    continue;
                }

                List<InterfaceAddress> ifaces = ni.getInterfaceAddresses();
                for (InterfaceAddress ifaceAddr : ifaces) {
                    if (ifaceAddr == null) {
                        continue;
                    }
                    InetAddress addr = ifaceAddr.getAddress();
                    // ignore ipv6 addresses
                    if (addr instanceof Inet6Address) {
                        continue;
                    }
                    int networkPrefixLength = ifaceAddr.getNetworkPrefixLength();
                    String textualPrefixAddr = addr.getHostAddress();
                    String textual2PartAddr = addr.getHostAddress();
                    int firstDot = textualPrefixAddr.indexOf(".");
                    int secondDot = textualPrefixAddr.indexOf(".", firstDot + 1);
                    if (firstDot > 0) {
                        textualPrefixAddr = textualPrefixAddr.substring(0, firstDot);
                        textual2PartAddr = textual2PartAddr.substring(0, secondDot);
                    }
                    try {
                        if ((addr.isLoopbackAddress() == false
                                || addr.isLoopbackAddress() == true && filterLoopback == false)
                                && ((addr instanceof Inet6Address) == false)) {
                            // plab nodes have 2 n/w interfaces, the local addr is "10.*.*.*"
                            if (filterLoopback == false
                                    || textual2PartAddr.compareTo("127.0") != 0) {
                                if (filterTenDot == false
                                        || textualPrefixAddr.compareTo("10") != 0) {
                                    if (filterPrivateIps == false
                                            || textual2PartAddr.compareTo("192.168") != 0) {
                                        int mtu = ni.getMTU();
                                        boolean isUp = ni.isUp();
                                        IpAddrStatus ipAddr
                                                = new IpAddrStatus(ni, addr, isUp, networkPrefixLength, mtu);
                                        addresses.add(ipAddr);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Problem with getting IP-address of a network interface: {}", e.getMessage());
                    }
                }
            }
        } catch (SocketException e) {
            logger.warn("Couldn't get the local inet address: {}", e.getMessage());
        }

        Set<IpAddrStatus> difference = difference(addresses, addrs);

        if (difference.size() > 0) {
            addrs.clear();
            addrs.addAll(addresses);
        }

        return difference;
    }
    private Handler<GetIpRequest> handleGetIpRequest = new Handler<GetIpRequest>() {
        @Override
        public void handle(GetIpRequest event) {

            lastRequest = event;
            boolean checkNetIfs = event.isPeriodicCheckNetworkInterfaces();
            boolean filterTenDot = event.isFilterTenDotIps();
            boolean filterPrivateIps = event.isFilterPrivateIps();
            boolean filterLoopback = event.isFilterLoopback();

            if (initialized == false && checkNetIfs == true) {
                SchedulePeriodicTimeout st
                        = new SchedulePeriodicTimeout(RECHECK_NETWORK_INTERFACES_PERIOD,
                                RECHECK_NETWORK_INTERFACES_PERIOD);
                RecheckIpTimeout msgTimeout = new RecheckIpTimeout(st, filterTenDot,
                        filterPrivateIps, filterLoopback);
                st.setTimeoutEvent(msgTimeout);
                trigger(st, timerPort);
                initialized = true;
            }

            getLocalNetworkInterfaces(filterTenDot, filterPrivateIps, filterLoopback);

            List<IpAddrStatus> listAddrs = new ArrayList<IpAddrStatus>(addrs);
            Collections.sort(listAddrs, new IpComparator());

            sendGetIpResponse();
        }
    };

    private void sendGetIpResponse() {
        // send a copy of the IpAddrStatus objects back to the client
        List<IpAddrStatus> netIfs = new ArrayList<IpAddrStatus>(addrs);
        // sort the IP addresses by whether their network interface is up or not
        Collections.sort(netIfs, new IpComparator());
        InetAddress firstIp;
        if (netIfs.isEmpty()) {
            firstIp = null;
        } else {
            firstIp = netIfs.get(0).getAddr();
        }
        trigger(new GetIpResponse(lastRequest, netIfs, firstIp), resolveIpPort);
    }

    @SuppressWarnings("unused")
    private static double getIpAsDouble(byte[] ipAsBytes) {

        return 16777216 * (ipAsBytes[0] & 0xFF) + 65536 * (ipAsBytes[1] & 0xFF) + 256
                * (ipAsBytes[2] & 0xFF) + (ipAsBytes[3] & 0xFF);
    }

    @SuppressWarnings("unused")
    private static byte[] convertIpFromIntToByte(int ipAsInt) {
        byte[] dword = new byte[4];
        dword[3] = (byte) (ipAsInt & 0x00FF);
        dword[2] = (byte) ((ipAsInt >> 8) & 0x000000FF);
        dword[1] = (byte) ((ipAsInt >> 16) & 0x000000FF);
        dword[0] = (byte) ((ipAsInt >> 24) & 0x000000FF);

        return dword;
    }

    private static void printInterfaces() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> listInetAddr = ni.getInetAddresses();
                while (listInetAddr.hasMoreElements()) {
                    InetAddress addr = listInetAddr.nextElement();
                    // return first IP address found that is not local and reachable
                    try {
                        if (addr.isLoopbackAddress() == false && ((addr instanceof Inet6Address) == false)) {
                            logger.debug(addr.toString());
                        }
                    } catch (Exception e) {
                        logger.warn("Problem with getting IP-address of a network interface: {}", e.getMessage());
                    }
                }
            }
        } catch (SocketException ex) {
            java.util.logging.Logger.getLogger(ResolveIp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
