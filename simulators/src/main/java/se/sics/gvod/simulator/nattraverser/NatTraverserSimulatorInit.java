package se.sics.gvod.simulator.nattraverser;

import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.HpClientConfiguration;
import se.sics.gvod.config.NatConfiguration;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.config.NatTraverserConfiguration;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.config.StunServerConfiguration;
import se.sics.kompics.Init;

public final class NatTraverserSimulatorInit extends Init {

    private final NatTraverserConfiguration natTraverserConfig;
    private final ParentMakerConfiguration parentMakerConfig;
    private final HpClientConfiguration hpClientConfig;
    private final RendezvousServerConfiguration rendezvousServerConfig;
    private final StunClientConfiguration stunClientConfig;
    private final StunServerConfiguration stunServerConfig;
    private final NatConfiguration natConfig;
    private final CroupierConfiguration croupierConfig;
	
    public NatTraverserSimulatorInit(
            NatTraverserConfiguration natTraverserConfig,
            HpClientConfiguration hpClientConfig,
            RendezvousServerConfiguration rendezvousServerConfig,
            StunServerConfiguration stunServerConfig,
            StunClientConfiguration stunClientConfig,
            ParentMakerConfiguration parentMakerConfig,
            NatConfiguration natGatewayConfiguration,
            CroupierConfiguration croupierConfig) {
        this.natTraverserConfig = natTraverserConfig;
        this.hpClientConfig = hpClientConfig;
        this.rendezvousServerConfig = rendezvousServerConfig;
        this.stunServerConfig = stunServerConfig;
        this.stunClientConfig = stunClientConfig;
        this.parentMakerConfig = parentMakerConfig;
        this.natConfig = natGatewayConfiguration;
        this.croupierConfig = croupierConfig;
    }    

    public CroupierConfiguration getCroupierConfig() {
        return croupierConfig;
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

    public StunServerConfiguration getStunServerConfig() {
        return stunServerConfig;
    }

    public StunClientConfiguration getStunClientConfig() {
        return stunClientConfig;
    }
    
    public NatConfiguration getNatConfig() {
        return natConfig;
    }
    
}
