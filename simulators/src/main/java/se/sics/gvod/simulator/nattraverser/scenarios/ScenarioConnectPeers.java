//package se.sics.gvod.simulator.nattraverser.scenarios;
//
//import se.sics.gvod.net.VodAddress;
//import se.sics.gvod.simulator.croupier.scenarios.Scenario;
//import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
//
//@SuppressWarnings("serial")
//public class ScenarioConnectPeers extends Scenario {
//    public static final int NUM_NODES=300;
//    public static final int NUM_PARENTS=2;
//    public static final long NUM_DISCONNECT=4;
//    
//	private static SimulationScenario scenario = new SimulationScenario() {{
//		
//            
//		StochasticProcess startPublicNodes = new StochasticProcess() {{
//			eventInterArrivalTime(exponential(1000));
//			raise(NUM_PARENTS, Operations.peerJoin(VodAddress.NatType.OPEN), 
//                                uniform(0, 10000));
//		}};
//                
//		StochasticProcess startPrivateNodes = new StochasticProcess() {{
//			eventInterArrivalTime(constant(5000));
//			raise(NUM_NODES, Operations.peerJoin(VodAddress.NatType.NAT), 
//                                uniform(0, 10000));
//		}};
//                
//		StochasticProcess connectNodes = new StochasticProcess() {{
//			eventInterArrivalTime(constant(500));
//			raise(NUM_NODES*5, Operations.connectPeers(), 
//                                uniform(0, 10000), uniform(0, 10000));
//		}};
//                
//		StochasticProcess killParents = new StochasticProcess() {{
//			eventInterArrivalTime(constant(5000));
//			raise(NUM_PARENTS/2, Operations.peerFail(VodAddress.NatType.OPEN), 
//                                uniform(0, 10000));
//		}};
//                
//		StochasticProcess disconnectNodes = new StochasticProcess() {{
//			eventInterArrivalTime(constant(500));
//			raise(NUM_NODES, Operations.disconnectPeers(), 
//                                uniform(0, 10000), constant(NUM_DISCONNECT));
//		}};                
//
//		StochasticProcess startCollectData = new StochasticProcess() {{
//			eventInterArrivalTime(constant(1000));
//			raise(1, Operations.startCollectData());
//		}};
//
//		StochasticProcess stopCollectData = new StochasticProcess() {{
//			eventInterArrivalTime(exponential(10));
//			raise(1, Operations.stopCollectData());
//		}};
//		
//		startPublicNodes.start();
//		startPrivateNodes.startAfterTerminationOf(30*1000, startPublicNodes);
//		connectNodes.startAfterTerminationOf(100*1000, startPrivateNodes);
//		killParents.startAfterTerminationOf(10*1000, connectNodes);
//		disconnectNodes.startAfterTerminationOf(10*1000, killParents);
//		startCollectData.startAfterTerminationOf(10*1000, disconnectNodes);
//		stopCollectData.startAfterTerminationOf(10*1000, startCollectData);
//	}};
//	
////-------------------------------------------------------------------
//	public ScenarioConnectPeers() {
//		super(scenario);
//	} 
//}