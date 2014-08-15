package se.sics.gvod.stun.upnp.events;

import se.sics.gvod.stun.upnp.UpnpComponent;
import se.sics.kompics.Init;

public final class UpnpInit extends Init<UpnpComponent> {

    public static final int DEFAULT_UPNP_DISCOVERY_TIMEOUT = 1 * 1200;
    public static final int DEFAULT_ROOT_DEVICE_TIMEOUT = 1 * 1200;

    private final int discoveryTimeout;
    private final int rootDeviceTimeout;

    public UpnpInit() {
        this.discoveryTimeout = DEFAULT_UPNP_DISCOVERY_TIMEOUT;
        this.rootDeviceTimeout = DEFAULT_ROOT_DEVICE_TIMEOUT;
    }

    /**
     *
     * @param discoveryTimeout the waiting time before UPnP component can be used.
     * The time required to identify a IGN on the network.
     * @param rootDeviceTimeout
     */
    public UpnpInit(int discoveryTimeout, int rootDeviceTimeout) {
        this.discoveryTimeout = discoveryTimeout;
        this.rootDeviceTimeout = rootDeviceTimeout;
    }

    public int getRootDeviceTimeout() {
        return rootDeviceTimeout;
    }

    public int getDiscoveryTimeout() {
        return discoveryTimeout;
    }
}
