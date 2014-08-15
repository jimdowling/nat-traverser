//package se.sics.gvod.simulator.gradient;
//
//import se.sics.gvod.config.CroupierCompositeConfiguration;
//import se.sics.gvod.simulator.croupier.main.*;
//import se.sics.gvod.config.VodConfig;
//import se.sics.gvod.simulator.croupier.scenarios.*;
//
//public class Main {
//
//    public static int SEED;
//    public static int PARENT_SIZE = 4;
//    public static int PARENT_UPDATE_ROUND_TIME = 2000;
//
//    public static void main(String[] args) throws Throwable {
//        if (args.length < 8) {
//            System.err.println("");
//            System.err.println("usage: <prog> seed exp numPub numPriv local-history-size neighbour-history-size expLength nodeSelection");
//            System.err.println("");
//
//            System.exit(1);
//        }
//        SEED = Integer.parseInt(args[0]);
//
//        VodConfig.init(new String[]{"-seed", args[0]});
//
//
//        Scenario scenario = null;
//        String scenarioName = args[1];
//        int numPub = (int) Double.parseDouble(args[2]);
//        int numPriv = (int) Double.parseDouble(args[3]);
//        int localHistorySize = (int) Double.parseDouble(args[4]);
//        int neighbourHistorySize = (int) Double.parseDouble(args[5]);
//        int expLength = (int) Integer.parseInt(args[6]);
//        String nodeSelection = args[7];
//    
//
//        Scenario.FIRST_PRIVATE = numPriv;
//        Scenario.SECOND_PUBLIC = numPub;
//        Scenario.COLLECT_RESULTS = expLength;
//
//
//
//        CroupierCompositeConfiguration configuration = new CroupierCompositeConfiguration(
//                PARENT_SIZE, 
//                PARENT_UPDATE_ROUND_TIME,
//                localHistorySize, neighbourHistorySize, nodeSelection);
//        configuration.store();
//
//        if (scenarioName.equalsIgnoreCase("join")) {
//            scenario = new ScenarioJoin();
//        } else if (scenarioName.equalsIgnoreCase("churn")) {
//            scenario = new ScenarioLowChurn();
//        } else if (scenarioName.equalsIgnoreCase("high-churn")) {
//            scenario = new ScenarioHighChurn();
//        } else if (scenarioName.equalsIgnoreCase("failure")) {
//            scenario = new ScenarioFailure();
//        }
//
//        if (scenario == null) {
//            System.err.println("");
//            System.err.println("invalid scenario name.");
//            System.err.println("the scenarios: join-only, low-churn, high-churn, and failure.");
//            System.err.println("");
//
//            System.exit(1);
//        }
//
//        scenario.setSeed(System.currentTimeMillis());
//        scenario.simulateGradient();
//    }
//}
