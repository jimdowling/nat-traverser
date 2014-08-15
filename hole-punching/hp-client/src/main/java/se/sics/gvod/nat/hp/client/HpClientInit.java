package se.sics.gvod.nat.hp.client;

import se.sics.gvod.config.HpClientConfiguration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import se.sics.gvod.common.Self;
import se.sics.kompics.Init;

public final class HpClientInit extends Init<HpClient> {

    private final Self self;
    private final ConcurrentHashMap<Integer, OpenedConnection> openedConnections;
    private final HpClientConfiguration config;
    private final ConcurrentSkipListSet<Integer> boundPorts;
    
    public HpClientInit(Self self,
            ConcurrentHashMap<Integer,OpenedConnection> openedConnections,
            HpClientConfiguration config,
            ConcurrentSkipListSet<Integer> parentPorts) {
        assert self != null;
        assert openedConnections != null;
        assert parentPorts != null;
        this.self = self;
        this.openedConnections = openedConnections;
        this.config = config;
        this.boundPorts = parentPorts;
    }

    public ConcurrentSkipListSet<Integer> getBoundPorts() {
        return boundPorts;
    }
    
    public HpClientConfiguration getConfig() {
        return config;
    }
    
    public ConcurrentHashMap<Integer, OpenedConnection> getOpenedConnections() {
        return openedConnections;
    }

    public Self getSelf() {
        return self;
    }
}
