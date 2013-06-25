package se.sics.gvod.simulator.gradient;

import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.GradientConfiguration;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.kompics.Init;

public final class GradientSimulatorInit extends Init {

    private final GradientConfiguration gradientConfiguration;
    private final CroupierConfiguration croupierConfiguration;
    private final ParentMakerConfiguration parentMakerConfiguration;

//-------------------------------------------------------------------	
    public GradientSimulatorInit(
            GradientConfiguration gradientConfiguration,
            CroupierConfiguration croupierConfiguration,
            ParentMakerConfiguration parentMakerConfiguration) {
        super();
        this.gradientConfiguration = gradientConfiguration;
        this.croupierConfiguration = croupierConfiguration;
        this.parentMakerConfiguration = parentMakerConfiguration;
    }

    public GradientConfiguration getGradientConfiguration() {
        return gradientConfiguration;
    }

//-------------------------------------------------------------------	
    public CroupierConfiguration getCroupierConfiguration() {
        return croupierConfiguration;
    }

//-------------------------------------------------------------------	
    public ParentMakerConfiguration getParentMakerConfiguration() {
        return parentMakerConfiguration;
    }
}
