//package se.sics.gvod.simulator.nattraverser;
//
//import java.io.IOException;
//
//import org.apache.log4j.PropertyConfigurator;
//import se.sics.gvod.config.AbstractConfiguration;
//import se.sics.gvod.config.CroupierConfiguration;
//import se.sics.gvod.config.HpClientConfiguration;
//import se.sics.gvod.config.NatConfiguration;
//import se.sics.gvod.config.NatTraverserConfiguration;
//import se.sics.gvod.config.VodConfig;
//import se.sics.gvod.net.VodNetwork;
//import se.sics.gvod.network.model.king.KingLatencyMap;
//import se.sics.gvod.p2p.simulator.P2pSimulator;
//import se.sics.gvod.p2p.simulator.P2pSimulatorInit;
//import se.sics.gvod.config.ParentMakerConfiguration;
//import se.sics.gvod.config.RendezvousServerConfiguration;
//import se.sics.gvod.config.StunClientConfiguration;
//import se.sics.gvod.config.StunServerConfiguration;
//import se.sics.gvod.timer.Timer;
//import se.sics.kompics.Component;
//import se.sics.kompics.ComponentDefinition;
//import se.sics.kompics.Kompics;
//import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
//import se.sics.kompics.simulation.SimulatorScheduler;
//
//public final class NatTraverserSimulationMain extends ComponentDefinition {
//
//    static {
//        PropertyConfigurator.configureAndWatch("log4j.properties");
//    }
//    private static SimulatorScheduler simulatorScheduler = new SimulatorScheduler();
//    private static SimulationScenario scenario
//            = SimulationScenario.load(System.getProperty("scenario"));
//
//    public static void main(String[] args) {
//        Kompics.setScheduler(simulatorScheduler);
//        Kompics.createAndStart(NatTraverserSimulationMain.class, 1);
//
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Kompics.shutdown();
//                } catch (Exception e) {
//                }
//            }
//        });
//
//    }
//
//    public NatTraverserSimulationMain() throws IOException {
//        P2pSimulator.setSimulationPortType(NatTraverserSimulatorPort.class);
//
//        VodConfig.init(new String[]{});
//
//        Component p2pSimulator = create(P2pSimulator.class);
//        Component simulator = create(NatTraverserSimulator.class);
//
//        // loading component configurations
//        final NatTraverserConfiguration ntConfig
//                = (NatTraverserConfiguration) AbstractConfiguration.load(NatTraverserConfiguration.class);
//        final HpClientConfiguration hpClientConfig = (HpClientConfiguration) HpClientConfiguration.load(HpClientConfiguration.class);
//        final ParentMakerConfiguration parentMakerConfiguration
//                = (ParentMakerConfiguration) ParentMakerConfiguration.load(ParentMakerConfiguration.class);
//        final NatConfiguration natConfiguration
//                = (NatConfiguration) NatConfiguration.load(NatConfiguration.class);
//        final StunServerConfiguration stunServerConfig
//                = (StunServerConfiguration) StunServerConfiguration.load(StunServerConfiguration.class);
//        final StunClientConfiguration stunClientConfig
//                = (StunClientConfiguration) StunClientConfiguration.load(StunClientConfiguration.class);
//        final RendezvousServerConfiguration rendezvousServerConfig
//                = (RendezvousServerConfiguration) RendezvousServerConfiguration.load(RendezvousServerConfiguration.class);
//        final CroupierConfiguration croupierConfig
//                = (CroupierConfiguration) CroupierConfiguration.load(CroupierConfiguration.class);
//
//        trigger(new P2pSimulatorInit(simulatorScheduler, scenario,
//                new KingLatencyMap(ntConfig.getSeed())), p2pSimulator.getControl());
//        trigger(new NatTraverserSimulatorInit(ntConfig, hpClientConfig,
//                rendezvousServerConfig, stunServerConfig, stunClientConfig,
//                parentMakerConfiguration, natConfiguration, croupierConfig), simulator.getControl());
//
//        connect(simulator.getNegative(VodNetwork.class), p2pSimulator.getPositive(VodNetwork.class));
//        connect(simulator.getNegative(Timer.class), p2pSimulator.getPositive(Timer.class));
//        connect(simulator.getNegative(NatTraverserSimulatorPort.class),
//                p2pSimulator.getPositive(NatTraverserSimulatorPort.class));
//    }
//}
