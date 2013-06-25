package se.sics.gvod.simulator.nattraverser;

import se.sics.gvod.config.NtCompositeConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.simulator.croupier.scenarios.*;
import se.sics.gvod.simulator.nattraverser.scenarios.ScenarioConnectPeers;

public class Main {

    public static int SEED;

    public static void main(String[] args) throws Throwable {
        if (args.length < 1) {
            System.err.println("");
            System.err.println("usage: <prog> seed");
            System.err.println("");
            System.exit(1);
        }
        SEED = Integer.parseInt(args[0]);

        VodConfig.init(new String[]{"-seed", args[0]});

        NtCompositeConfiguration configuration = new NtCompositeConfiguration(SEED);
        configuration.store();

        Scenario scenario = new ScenarioConnectPeers();

        scenario.setSeed(SEED);
        scenario.simulateNatTraverser();
    }
}
