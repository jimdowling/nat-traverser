package se.sics.gvod.config;

public class NtCompositeConfiguration extends CompositeConfiguration {

    NatTraverserConfiguration natTraverserConfig;
    ParentMakerConfiguration parentMakerConfig;
    HpClientConfiguration hpClientConfig;
    RendezvousServerConfiguration rendezvousServerConfig;
    StunServerConfiguration stunClientConfig;
    NatConfiguration natConfig;
    
    public NtCompositeConfiguration() {
        natTraverserConfig = NatTraverserConfiguration.build();
        parentMakerConfig = ParentMakerConfiguration.build();
        hpClientConfig = HpClientConfiguration.build();
        rendezvousServerConfig = RendezvousServerConfiguration.build().setNumChildren(1000);
        stunClientConfig = StunServerConfiguration.build();
        natConfig = NatConfiguration.build();
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

    public StunServerConfiguration getStunClientConfig() {
        return stunClientConfig;
    }

    public NatConfiguration getNatConfig() {
        return natConfig;
    }
}