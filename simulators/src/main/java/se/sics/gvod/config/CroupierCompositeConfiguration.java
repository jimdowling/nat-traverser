package se.sics.gvod.config;

public class CroupierCompositeConfiguration extends CompositeConfiguration {

    public static int SNAPSHOT_PERIOD = 5000;
    public static int VIEW_SIZE = 20;
    CroupierConfiguration croupierConfig;
    ParentMakerConfiguration parentMakerConfig;
    GradientConfiguration gradientConfig;

//-------------------------------------------------------------------
    public CroupierCompositeConfiguration(int parentSize, int parentUpdatePeriod,
            int localHistorySize, int neighbourHistorySize,
            String croupierNodeSelectionPolicy) {
        super();
        parentMakerConfig = ParentMakerConfiguration.build()
                .setNumParents(parentSize)
                .setParentUpdatePeriod(parentUpdatePeriod)
                .setKeepParentRttRange(100)
                .setRtoRetries(0)
                .setPingRetries(3);

        croupierConfig =
                CroupierConfiguration.build()
                .setPolicy(croupierNodeSelectionPolicy)
                .setShuffleLength(VIEW_SIZE / 2)
                .setViewSize(VIEW_SIZE)
                .setShufflePeriod(10*1000);

        gradientConfig = GradientConfiguration.build()
                .setSimilarSetSize(VIEW_SIZE)
                .setShufflePeriod(2000)
                .setRto(5000)
                .setSearchRequestTimeout(20 * 1000)
                .setUtilityThreshold(2000)
                .setNumParallelSearches(4)
                .setSearchTtl(15)
                .setNumBestSimilarPeers(5)
                .setNumFingers(5000)
                .setTemperature(0.75d);
    }

    public CroupierConfiguration getCroupierConfig() {
        return croupierConfig;
    }

    public ParentMakerConfiguration getParentMakerConfig() {
        return parentMakerConfig;
    }

    public GradientConfiguration getGradientConfig() {
        return gradientConfig;
    }

}
