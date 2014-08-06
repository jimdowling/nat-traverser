package se.sics.gvod.nat.traversal.events;

import java.util.HashSet;
import java.util.Set;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.Self;
import se.sics.gvod.config.HpClientConfiguration;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.config.NatTraverserConfiguration;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.config.StunServerConfiguration;
import se.sics.gvod.nat.traversal.NatTraverser;
import se.sics.kompics.Init;

public final class NatTraverserInit extends Init<NatTraverser> {

    private final Self self;
    private final long seed;
    private final Set<Address> publicNodes;
    private final NatTraverserConfiguration natTraverserConfig;
    private final ParentMakerConfiguration parentMakerConfig;
    private final HpClientConfiguration hpClientConfig;
    private final RendezvousServerConfiguration rendezvousServerConfig;
    private final StunServerConfiguration stunServerConfig;
    private final StunClientConfiguration stunClientConfig;
    private boolean openServer;

    public NatTraverserInit(Self self,
            Set<Address> publicNodes,
            long seed) {
        this(self, publicNodes, seed, NatTraverserConfiguration.build(),
                HpClientConfiguration.build(),
                RendezvousServerConfiguration.build(),
                StunServerConfiguration.build(),
                StunClientConfiguration.build(),
                ParentMakerConfiguration.build(),
                false);
    }

    public NatTraverserInit(Self self,
            Set<Address> publicNodes,
            long seed,
            NatTraverserConfiguration natTraverserConfig,
            HpClientConfiguration hpClientConfig,
            RendezvousServerConfiguration rendezvousServerConfig,
            StunServerConfiguration stunServerConfig,
            StunClientConfiguration stunClientConfig,
            ParentMakerConfiguration parentMakerConfig,
            boolean openServer) {
        assert (self != null);
        this.self = self;
        if (publicNodes == null) {
            this.publicNodes = new HashSet<Address>();
        } else {
            this.publicNodes = publicNodes;            
        }
        this.natTraverserConfig = natTraverserConfig;
        this.hpClientConfig = hpClientConfig;
        this.rendezvousServerConfig = rendezvousServerConfig;
        this.stunClientConfig = stunClientConfig;
        this.stunServerConfig = stunServerConfig;
        this.parentMakerConfig = parentMakerConfig;
        this.seed = seed;
        this.openServer = openServer;
    }

    public Self getSelf() {
        return self;
    }

    public long getSeed() {
        return seed;
    }

    public StunClientConfiguration getStunClientConfig() {
        return stunClientConfig;
    }
    
    public StunServerConfiguration getStunServerConfig() {
        return stunServerConfig;
    }

    public HpClientConfiguration getHpClientConfig() {
        return hpClientConfig;
    }

    public RendezvousServerConfiguration getRendezvousServerConfig() {
        return rendezvousServerConfig;
    }

    public NatTraverserConfiguration getNatTraverserConfig() {
        return natTraverserConfig;
    }

    public ParentMakerConfiguration getParentMakerConfig() {
        return parentMakerConfig;
    }

    public Set<Address> getPublicNodes() {
        return publicNodes;
    }

    public boolean isOpenServer() {
        return openServer;
    }

    public void setOpenServer(boolean openServer) {
        this.openServer = openServer;
    }
    
}
