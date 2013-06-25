package se.sics.gvod.simulator.nattraverser;

import se.sics.gvod.config.HpClientConfiguration;
import se.sics.gvod.config.NatConfiguration;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.config.NatTraverserConfiguration;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.config.StunServerConfiguration;
import se.sics.kompics.Init;

public final class NatTraverserSimulatorInit extends Init {

    private final NatTraverserConfiguration natTraverserConfig;
    private final ParentMakerConfiguration parentMakerConfig;
    private final HpClientConfiguration hpClientConfig;
    private final RendezvousServerConfiguration rendezvousServerConfig;
    private final StunServerConfiguration stunClientConfig;
    private final NatConfiguration natConfiguration;
	
    public NatTraverserSimulatorInit(
            NatTraverserConfiguration natTraverserConfig,
            HpClientConfiguration hpClientConfig,
            RendezvousServerConfiguration rendezvousServerConfig,
            StunServerConfiguration stunClientConfig,
            ParentMakerConfiguration parentMakerConfig,
            NatConfiguration natGatewayConfiguration) {
        this.natTraverserConfig = natTraverserConfig;
        this.hpClientConfig = hpClientConfig;
        this.rendezvousServerConfig = rendezvousServerConfig;
        this.stunClientConfig = stunClientConfig;
        this.parentMakerConfig = parentMakerConfig;
        this.natConfiguration = natGatewayConfiguration;
    }    

    public HpClientConfiguration getHpClientConfig() {
        return hpClientConfig;
    }

    public NatTraverserConfiguration getNatTraverserConfig() {
        return natTraverserConfig;
    }

    public ParentMakerConfiguration getParentMakerConfig() {
        return parentMakerConfig;
    }

    public RendezvousServerConfiguration getRendezvousServerConfig() {
        return rendezvousServerConfig;
    }

    public StunServerConfiguration getStunClientConfig() {
        return stunClientConfig;
    }

    public NatConfiguration getNatConfig() {
        return natConfiguration;
    }
}
