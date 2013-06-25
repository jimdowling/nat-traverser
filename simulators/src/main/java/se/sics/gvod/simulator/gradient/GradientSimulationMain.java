package se.sics.gvod.simulator.gradient;

import se.sics.gvod.simulator.common.SimulatorPort;
import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;
import se.sics.gvod.config.AbstractConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.config.GradientConfiguration;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.network.model.king.KingLatencyMap;
import se.sics.gvod.p2p.simulator.P2pSimulator;
import se.sics.gvod.p2p.simulator.P2pSimulatorInit;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
import se.sics.kompics.simulation.SimulatorScheduler;

public final class GradientSimulationMain extends ComponentDefinition {

    static {
        PropertyConfigurator.configureAndWatch("log4j.properties");
    }
    private static SimulatorScheduler simulatorScheduler = new SimulatorScheduler();
    private static SimulationScenario scenario = SimulationScenario.load(System.getProperty("scenario"));

    public static void main(String[] args) {
        Kompics.setScheduler(simulatorScheduler);
        Kompics.createAndStart(GradientSimulationMain.class, 1);
    }

    public GradientSimulationMain() throws IOException {
        P2pSimulator.setSimulationPortType(SimulatorPort.class);


        VodConfig.init(new String[]{});

        // create
        Component p2pSimulator = create(P2pSimulator.class);
        Component gradientSimulator = create(GradientSimulator.class);

        final GradientConfiguration gradientConfiguration =
                (GradientConfiguration) GradientConfiguration.load(GradientConfiguration.class);
        final CroupierConfiguration croupierConfiguration =
                (CroupierConfiguration) CroupierConfiguration.load(CroupierConfiguration.class);
        final ParentMakerConfiguration parentMakerConfiguration =
                (ParentMakerConfiguration) AbstractConfiguration.load(ParentMakerConfiguration.class);

        trigger(new P2pSimulatorInit(simulatorScheduler, scenario,
                new KingLatencyMap(croupierConfiguration.getSeed())), p2pSimulator.getControl());
        trigger(new GradientSimulatorInit(gradientConfiguration, croupierConfiguration,
                parentMakerConfiguration), gradientSimulator.getControl());

        connect(gradientSimulator.getNegative(VodNetwork.class), p2pSimulator.getPositive(VodNetwork.class));
        connect(gradientSimulator.getNegative(Timer.class), p2pSimulator.getPositive(Timer.class));
        connect(gradientSimulator.getNegative(SimulatorPort.class), p2pSimulator.getPositive(SimulatorPort.class));
    }
}
