//package se.sics.gvod.simulator.croupier.scenarios;
//
//import se.sics.gvod.net.VodAddress;
//import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
//
//@SuppressWarnings("serial")
//public class ScenarioHighChurn extends Scenario {
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
//			raise(200, Operations.croupierPeerJoin(VodAddress.NatType.OPEN), uniform(0, 10000));
//			raise(800, Operations.croupierPeerJoin(VodAddress.NatType.NAT), uniform(0, 10000));
//		}};
//
//		StochasticProcess nodesChurn = new StochasticProcess() {{
//			eventInterArrivalTime(exponential(10));
//			raise(2000, Operations.croupierPeerFail(VodAddress.NatType.OPEN), uniform(0, 10000));
//			raise(2000, Operations.croupierPeerJoin(VodAddress.NatType.OPEN), uniform(0, 10000));
//		}};
//		
//		StochasticProcess startCollectData = new StochasticProcess() {{
//			eventInterArrivalTime(exponential(10));
//			raise(1, Operations.startCollectData());
//		}};
//
//		StochasticProcess stopCollectData = new StochasticProcess() {{
//			eventInterArrivalTime(exponential(10));
//			raise(1, Operations.stopCollectData());
//		}};
//		
//		firstNodeJoin.start();
//		secondNodeJoin.startAfterTerminationOf(1000, firstNodeJoin);
//		nodesJoin.startAfterTerminationOf(1000, secondNodeJoin);
//		nodesChurn.startAfterTerminationOf(100 * 1000, nodesJoin);
//		startCollectData.startAfterTerminationOf(300 * 1000, nodesJoin);
//		stopCollectData.startAfterTerminationOf(50 * 1000, startCollectData);
//	}};
//	
////-------------------------------------------------------------------
//	public ScenarioHighChurn() {
//		super(scenario);
//	} 
//}
