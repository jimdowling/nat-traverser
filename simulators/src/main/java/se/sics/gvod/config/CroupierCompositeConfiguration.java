package se.sics.gvod.config;

public class CroupierCompositeConfiguration extends CompositeConfiguration {

    public static int SNAPSHOT_PERIOD = 5000;
    public static int VIEW_SIZE = 20;
    CroupierConfiguration croupierConfig;
    ParentMakerConfiguration parentMakerConfig;
    GradientConfiguration gradientConfig;

//-------------------------------------------------------------------
    public CroupierCompositeConfiguration(int seed, int parentSize, int parentUpdatePeriod,
            int localHistorySize, int neighbourHistorySize,
            String croupierNodeSelectionPolicy) {
        super(seed);
        parentMakerConfig = ParentMakerConfiguration.build()
                .setParentSize(parentSize)
                .setChildSize(250)
                .setParentUpdatePeriod(parentUpdatePeriod)
                .setKeepParentRttRange(100)
                .setRetryNum(0)
                .setNumPingRetries(3);

        croupierConfig =
                CroupierConfiguration.build()
                .setPolicy(croupierNodeSelectionPolicy)
                .setShuffleLength(VIEW_SIZE / 2)
                .setViewSize(VIEW_SIZE);

        gradientConfig = GradientConfiguration.build()
                .setSimilarSetSize(VIEW_SIZE)
                .setSetsExchangePeriod(2000)
                .setSetsExchangeTimeout(5000)
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
