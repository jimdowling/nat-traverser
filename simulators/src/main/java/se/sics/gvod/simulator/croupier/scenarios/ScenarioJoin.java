package se.sics.gvod.simulator.croupier.scenarios;

import se.sics.gvod.net.VodAddress;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

@SuppressWarnings("serial")
public class ScenarioJoin extends Scenario {
	private static SimulationScenario scenario = new SimulationScenario() {{
		
		StochasticProcess firstNodeJoin = new StochasticProcess() {{
			eventInterArrivalTime(constant(10));
			raise(Scenario.FIRST_PUBLIC, Operations.croupierPeerJoin(VodAddress.NatType.OPEN), 
                                uniform(0, 10000));
		}};
		
		StochasticProcess nodesJoin1 = new StochasticProcess() {{
			eventInterArrivalTime(exponential(10));
			raise(Scenario.SECOND_PUBLIC, Operations.croupierPeerJoin(VodAddress.NatType.OPEN), 
                                uniform(0, 10000));
			raise(Scenario.FIRST_PRIVATE, Operations.croupierPeerJoin(VodAddress.NatType.NAT), 
                                uniform(0, 10000));
		}};

		StochasticProcess startCollectData = new StochasticProcess() {{
			eventInterArrivalTime(constant(1000));
			raise(Scenario.COLLECT_RESULTS, Operations.startCollectData());
		}};

		StochasticProcess stopCollectData = new StochasticProcess() {{
			eventInterArrivalTime(exponential(10));
			raise(1, Operations.stopCollectData());
		}};
		
		firstNodeJoin.start();
		nodesJoin1.startAfterTerminationOf(15000, firstNodeJoin);
		startCollectData.startAfterTerminationOf(1000, nodesJoin1);
		stopCollectData.startAfterTerminationOf(10 * 1000, startCollectData);
	}};
	
//-------------------------------------------------------------------
	public ScenarioJoin() {
		super(scenario);
	} 
}
