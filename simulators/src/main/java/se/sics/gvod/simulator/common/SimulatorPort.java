package se.sics.gvod.simulator.common;

import se.sics.kompics.PortType;
import se.sics.kompics.p2p.experiment.dsl.events.TerminateExperiment;

public class SimulatorPort extends PortType {{
	positive(PeerJoin.class);
	positive(PeerFail.class);
	positive(PeerChurn.class);
	positive(StartCollectData.class);
	positive(StopCollectData.class);
	
	negative(TerminateExperiment.class);
}}
