package se.sics.gvod.stun.client.events;

import se.sics.gvod.common.Self;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.stun.client.StunClient;
import se.sics.kompics.Init;

public final class StunClientInit extends Init<StunClient> {

    private final Self self;
    private final long seed;
    private StunClientConfiguration config;

    public StunClientInit(Self self, long seed, StunClientConfiguration config) {
        this.self = self;
        this.seed = seed;
        this.config = config;
    }

    public StunClientConfiguration getConfig() {
        return config;
    }

    public Self getSelf() {
        return self;
    }

    public long getSeed() {
        return seed;
    }
    
}
