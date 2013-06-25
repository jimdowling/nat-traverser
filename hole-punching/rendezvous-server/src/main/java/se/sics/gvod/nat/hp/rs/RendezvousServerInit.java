package se.sics.gvod.nat.hp.rs;

import se.sics.gvod.config.RendezvousServerConfiguration;
import java.util.concurrent.ConcurrentHashMap;
import se.sics.gvod.common.Self;
import se.sics.gvod.nat.hp.rs.RendezvousServer.RegisteredClientRecord;
import se.sics.kompics.Init;

public final class RendezvousServerInit extends Init {

    private final Self self;
    private final ConcurrentHashMap<Integer, RegisteredClientRecord> registeredClients;
    RendezvousServerConfiguration config;

    public RendezvousServerInit(Self self,
            ConcurrentHashMap<Integer, RegisteredClientRecord> registeredClients,
            RendezvousServerConfiguration config
            ) {
        assert(self != null);
        this.self = self;
        this.registeredClients = registeredClients;
        this.config = config;
    }


    public Self getSelf() {
        return self;
    }

    public ConcurrentHashMap<Integer, RegisteredClientRecord> getRegisteredClients() {
        return registeredClients;
    }

    public RendezvousServerConfiguration getConfig() {
        return config;
    }

}
