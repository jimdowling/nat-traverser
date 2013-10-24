package se.sics.gvod.nat.hp.client;

import java.util.Set;
import se.sics.gvod.config.HpClientConfiguration;
import java.util.concurrent.ConcurrentHashMap;
import se.sics.gvod.common.Self;
import se.sics.kompics.Init;

public final class HpClientInit extends Init {

    private final Self self;
    private final ConcurrentHashMap<Integer, OpenedConnection> openedConnections;
    private final HpClientConfiguration config;
    private final ConcurrentHashMap<Integer,Set<Integer>> parentPorts;
    
    public HpClientInit(Self self,
            ConcurrentHashMap<Integer,OpenedConnection> openedConnections,
            HpClientConfiguration config,
            ConcurrentHashMap<Integer,Set<Integer>> parentPorts) {
        assert self != null;
        assert openedConnections != null;
        assert parentPorts != null;
        this.self = self;
        this.openedConnections = openedConnections;
        this.config = config;
        this.parentPorts = parentPorts;
    }

    public ConcurrentHashMap<Integer, Set<Integer>> getParentPorts() {
        return parentPorts;
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
