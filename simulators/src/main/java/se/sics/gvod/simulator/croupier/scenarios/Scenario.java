//package se.sics.gvod.simulator.croupier.scenarios;
//
//import java.util.Random;
//import se.sics.gvod.simulator.croupier.CroupierSimulationMain;
//import se.sics.gvod.simulator.gradient.GradientSimulationMain;
//import se.sics.gvod.simulator.nattraverser.NatTraverserSimulationMain;
//import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
//
///**
// * The <code>Scenario</code> class.
// * 
// * @author Amir Payberah <amir@sics.se>
// */
//public class Scenario {
//    public static int FIRST_PUBLIC = 2;
//    public static int FIRST_PRIVATE = 800;
//    public static int SECOND_PUBLIC = 199;
//    public static int COLLECT_RESULTS = 150;
//    
//	private static Random random;
//	protected SimulationScenario scenario;
//
////-------------------------------------------------------------------
//	public Scenario(SimulationScenario scenario) {
//		this.scenario = scenario;
//		this.scenario.setSeed(System.currentTimeMillis());
//		random = scenario.getRandom();
//	}
//
////-------------------------------------------------------------------
//	public void setSeed(long seed) {
//		this.scenario.setSeed(seed);
//	}
//
////-------------------------------------------------------------------
//	public void execute() {
////		this.scenario.execute(CroupierExecutionMain.class);
//	}
//
////-------------------------------------------------------------------
//        
//	public void simulateNatTraverser() {
//		this.scenario.simulate(NatTraverserSimulationMain.class);
//	}
//        
//	public void simulateCroupier() {
//		this.scenario.simulate(CroupierSimulationMain.class);
//	}
//        
//	public void simulateGradient() {
//		this.scenario.simulate(GradientSimulationMain.class);
//	}
//        
////-------------------------------------------------------------------
//	public static Random getRandom() {
//		return random;
//	}
//
////-------------------------------------------------------------------
//	public static void setRandom(Random r) {
//		random = r;
//	}
//}
