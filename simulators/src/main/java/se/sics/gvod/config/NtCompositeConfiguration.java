package se.sics.gvod.config;

public class NtCompositeConfiguration extends CompositeConfiguration {

    NatTraverserConfiguration natTraverserConfig;
    ParentMakerConfiguration parentMakerConfig;
    HpClientConfiguration hpClientConfig;
    RendezvousServerConfiguration rendezvousServerConfig;
    StunServerConfiguration stunServerConfig;
    StunClientConfiguration stunClientConfig;
    NatConfiguration natConfig;
    CroupierConfiguration croupierConfig;

    public NtCompositeConfiguration() {
        natTraverserConfig = NatTraverserConfiguration.build();
        parentMakerConfig = ParentMakerConfiguration.build().setNumParents(3);
        hpClientConfig = HpClientConfiguration.build();
        rendezvousServerConfig = RendezvousServerConfiguration.build().setNumChildren(1000);
        stunServerConfig = StunServerConfiguration.build();
        stunClientConfig = StunClientConfiguration.build();
        natConfig = NatConfiguration.build();
        croupierConfig = CroupierConfiguration.build().setShufflePeriod(10 * 1000);
    }

    public CroupierConfiguration getCroupierConfig() {
        return croupierConfig;
    }
    
    public NatTraverserConfiguration getNatTraverserConfig() {
        return natTraverserConfig;
    }

    public ParentMakerConfiguration getParentMakerConfig() {
        return parentMakerConfig;
    }

    public HpClientConfiguration getHpClientConfig() {
        return hpClientConfig;
    }

    public RendezvousServerConfiguration getRendezvousServerConfig() {
        return rendezvousServerConfig;
    }

    public StunClientConfiguration getStunClientConfig() {
        return stunClientConfig;
    }
    
    public StunServerConfiguration getStunServerConfig() {
        return stunServerConfig;
    }

    public NatConfiguration getNatConfig() {
        return natConfig;
    }
}
