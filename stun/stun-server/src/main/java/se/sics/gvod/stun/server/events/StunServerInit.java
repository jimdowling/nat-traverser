package se.sics.gvod.stun.server.events;

import java.util.List;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Init;
import se.sics.gvod.common.Self;
import se.sics.gvod.config.StunServerConfiguration;

public final class StunServerInit extends Init {

    private final List<VodAddress> partners;
    private final Self self;
    private StunServerConfiguration config;

    public StunServerInit(Self self, 
            List<VodAddress> partners, 
            StunServerConfiguration config) {
        if (partners == null || self == null) {
            throw new NullPointerException("StunServerInit had null parameter");
        }
        this.self = self;
        this.partners = partners;
        this.config = config;
    }
    public Self getSelf() {
        return self;
    }

    public List<VodAddress> getPartners() {
        return partners;
    }

    public StunServerConfiguration getConfig() {
        return config;
    }

}
