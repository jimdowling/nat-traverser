//package se.sics.gvod.simulator.croupier.scenarios;
//
//import java.util.HashMap;
//
//import se.sics.gvod.net.VodAddress;
//import se.sics.gvod.simulator.croupier.utils.SkypeParse;
//import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
//
//@SuppressWarnings("serial")
//public class ScenarioSkype extends Scenario {
//	public static final double PRIVATE_NODES_RATIO = 0.8;
//	public static final int INIT_NODES = 1000;
//	static final int PRIVATE_NODES;
//	static final int PUBLIC_NODES;
//        
//        static {
//            PRIVATE_NODES = (int)(INIT_NODES * PRIVATE_NODES_RATIO);            
//            PUBLIC_NODES = INIT_NODES - PRIVATE_NODES;
//        }
//	
//	static HashMap<Integer, Integer> skypeTrace = SkypeParse.parse("skypeTrace");
//
//	private static SimulationScenario scenario = new SimulationScenario() {{
//		
//		StochasticProcess firstNodeJoin = new StochasticProcess() {{
//			eventInterArrivalTime(constant(100));
//			raise(1, Operations.croupierPeerJoin(VodAddress.NatType.OPEN), uniform(0, 10000));
//		}};
//
//		StochasticProcess secondNodeJoin = new StochasticProcess() {{
//			eventInterArrivalTime(constant(100));
//			raise(1, Operations.croupierPeerJoin(VodAddress.NatType.OPEN), uniform(0, 10000));
//		}};
//		
//		StochasticProcess nodesJoin = new StochasticProcess() {{
//			eventInterArrivalTime(exponential(10));
//			raise(PUBLIC_NODES, Operations.croupierPeerJoin(VodAddress.NatType.OPEN), uniform(0, 10000));
//			raise(PRIVATE_NODES, Operations.croupierPeerJoin(VodAddress.NatType.NAT), uniform(0, 10000));
//		}};
//
//		StochasticProcess churn = new StochasticProcess() {{
//			eventInterArrivalTime(constant(1000));
//			raise(1000, Operations.croupierSkypeChurn(skypeTrace), uniform(0, 10000));
//		}};
//               
//		firstNodeJoin.start();
//		secondNodeJoin.startAfterTerminationOf(1000, firstNodeJoin);
//		nodesJoin.startAfterTerminationOf(1000, secondNodeJoin);
//		churn.startAfterTerminationOf(20000, nodesJoin);
//	}};
//	
////-------------------------------------------------------------------
//	public ScenarioSkype() {
//		super(scenario);
//	} 
//}
