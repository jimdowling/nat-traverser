package se.sics.gvod.simulator.nattraverser.scenarios;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.simulator.croupier.scenarios.Scenario;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

@SuppressWarnings("serial")
public class ScenarioConnectPeers extends Scenario {
	private static SimulationScenario scenario = new SimulationScenario() {{
		
            
		StochasticProcess startPublicNodes = new StochasticProcess() {{
			eventInterArrivalTime(exponential(1000));
			raise(2, Operations.peerJoin(VodAddress.NatType.OPEN), 
                                uniform(0, 10000));
		}};
                
		StochasticProcess startNodes = new StochasticProcess() {{
			eventInterArrivalTime(exponential(1000));
			raise(300, Operations.peerJoin(VodAddress.NatType.NAT), 
                                uniform(0, 10000));
		}};
                
		StochasticProcess connectNodes = new StochasticProcess() {{
			eventInterArrivalTime(constant(1000));
			raise(1500, Operations.connectPeers(), 
                                uniform(0, 10000), uniform(0, 10000));
		}};

		StochasticProcess startCollectData = new StochasticProcess() {{
			eventInterArrivalTime(constant(1000));
			raise(1, Operations.startCollectData());
		}};

		StochasticProcess stopCollectData = new StochasticProcess() {{
			eventInterArrivalTime(exponential(10));
			raise(1, Operations.stopCollectData());
		}};
		
		startPublicNodes.start();
		startNodes.startAfterTerminationOf(50*1000, startPublicNodes);
		connectNodes.startAfterTerminationOf(1000*1000, startNodes);
		startCollectData.startAfterTerminationOf(100*1000, connectNodes);
		stopCollectData.startAfterTerminationOf(10*1000, startCollectData);
	}};
	
//-------------------------------------------------------------------
	public ScenarioConnectPeers() {
		super(scenario);
	} 
}
