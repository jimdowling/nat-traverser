package se.sics.gvod.nat.hp.client;

import se.sics.gvod.config.HpClientConfiguration;
import java.util.concurrent.ConcurrentHashMap;
import se.sics.gvod.common.Self;
import se.sics.kompics.Init;

public final class HpClientInit extends Init {

    private final Self self;
    private final ConcurrentHashMap<Integer, OpenedConnection> openedConnections;
    private final HpClientConfiguration config;

    public HpClientInit(Self self,
            ConcurrentHashMap<Integer,OpenedConnection> openedConnections,
            HpClientConfiguration config) {
        assert self != null;
        assert openedConnections != null;
        this.self = self;
        this.openedConnections = openedConnections;
        this.config = config;
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
