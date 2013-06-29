package se.sics.gvod.config;

import se.sics.gvod.net.Nat;

public final class StunClientConfiguration extends AbstractConfiguration<StunClientConfiguration> {

    /**
     * Fields cannot be private. Package protected, ok.
     */
    // if the difference between two ports is less than tolerance value then the ports are contiguous
    int randTolerance;
    // When establishing the amount of time a binding is kept in the NAT without being
    // refreshed (NAT binding timeout), we test with a minimum value first, normally 30s.
    int ruleExpirationMinWait;
    // When establishing the NAT binding timeout, increment the test timeout for the binding,
    // normally by 30s. Smaller values give more accurate NAT binding timeouts, but at the cost
    // of significantly increased running time.
    int ruleExpirationIncrement;
    // Amount of time to wait until we have discovered a UPNP device in our proximity
    int upnpDiscoveryTimeout;
    // Amount of time to wait until we receive a response from a UPnP message (e.g., MapPorts)
    int upnpTimeout;
    // Do we test for a UPnP Internet Gateway Device (IGD)?
    boolean upnpEnable;
    // Minimum Latency added to estimates of the RTO to a server, for safety.
    // Higher values mean slower running of the protocol, but fewer timeouts and retries
    int minimumRto;
    // Do we try and establish the Nat Binding Timeout
    boolean measureNatBindingTimeout;
    // initial estimate of the RTO to a stun server
    int rto;
    // number of retries for msgs sent to stun servers
    int rtoRetries;
    // Amount to scale the RTO after a timeout.
    double rtoScale;

    /**
     * Default constructor comes first.
     */
    public StunClientConfiguration() {
        this(VodConfig.getSeed(),
                2, // Rand tolerance  
                Nat.DEFAULT_RULE_EXPIRATION_TIME,
                Nat.DEFAULT_RULE_EXPIRATION_TIME,
                VodConfig.STUN_UPNP_DISCOVERY_TIMEOUT,
                VodConfig.STUN_UPNP_TIMEOUT,
                VodConfig.STUN_UPNP_ENABLED,
                VodConfig.STUN_MIN_RTT,
                VodConfig.STUN_MEASURE_NAT_BINDING_TIMEOUT,
                VodConfig.STUN_RTO,
                VodConfig.STUN_RTO_RETRIES,
                VodConfig.STUN_RTO_SCALE);
    }

    /**
     * Full argument constructor comes second.
     */
    public StunClientConfiguration(
            int seed,
            int randTolerance,
            int ruleExpirationMinWait,
            int ruleExpirationIncrement,
            int upnpDiscoveryTimeout,
            int upnpTimeout,
            boolean upnpEnable,
            int minimumRtt,
            boolean measureNatBindingTimeout,
            int rto,
            int rtoRetries,
            double rtoScale) {
        super(seed);
        this.randTolerance = randTolerance;
        this.ruleExpirationMinWait = ruleExpirationMinWait;
        this.ruleExpirationIncrement = ruleExpirationIncrement;
        this.upnpDiscoveryTimeout = upnpDiscoveryTimeout;
        this.upnpTimeout = upnpTimeout;
        this.upnpEnable = upnpEnable;
        this.minimumRto = minimumRtt;
        this.measureNatBindingTimeout = measureNatBindingTimeout;
        this.rto = rto;
        this.rtoRetries = rtoRetries;
        this.rtoScale = rtoScale;
    }

    public static StunClientConfiguration build() {
        return new StunClientConfiguration();
    }

    public int getRto() {
        return rto;
    }

    public int getRandTolerance() {
        return randTolerance;
    }

    public int getRuleExpirationIncrement() {
        return ruleExpirationIncrement;
    }

    public int getRuleExpirationMinWait() {
        return ruleExpirationMinWait;
    }

    public int getUpnpTimeout() {
        return upnpTimeout;
    }

    public boolean isUpnpEnable() {
        return upnpEnable;
    }

    public int getUpnpDiscoveryTimeout() {
        return upnpDiscoveryTimeout;
    }

    public int getMinimumRtt() {
        return minimumRto;
    }

    public int getRtoRetries() {
        return rtoRetries;
    }

    public double getRtoScale() {
        return rtoScale;
    }

    public boolean isMeasureNatBindingTimeout() {
        return measureNatBindingTimeout;
    }
    
    public StunClientConfiguration setMinimumRtt(int minimumRtt) {
        this.minimumRto = minimumRtt;
        return this;
    }


    public StunClientConfiguration setRandTolerance(int randTolerance) {
        this.randTolerance = randTolerance;
        return this;
    }

    public StunClientConfiguration setRuleExpirationIncrement(int ruleExpirationIncrement) {
        this.ruleExpirationIncrement = ruleExpirationIncrement;
        return this;
    }

    public StunClientConfiguration setRuleExpirationMinWait(int ruleExpirationMinWait) {
        this.ruleExpirationMinWait = ruleExpirationMinWait;
        return this;
    }

    public StunClientConfiguration setUpnpDiscoveryTimeout(int upnpDiscoveryTimeout) {
        this.upnpDiscoveryTimeout = upnpDiscoveryTimeout;
        return this;
    }

    public StunClientConfiguration setUpnpEnable(boolean upnpEnable) {
        this.upnpEnable = upnpEnable;
        return this;
    }

    public StunClientConfiguration setUpnpTimeout(int upnpTimeout) {
        this.upnpTimeout = upnpTimeout;
        return this;
    }

    public StunClientConfiguration setRto(int rto) {
        this.rto = rto;
        return this;
    }

    public StunClientConfiguration setRtoRetries(int rtoRetries) {
        this.rtoRetries = rtoRetries;
        return this;
    }

    public StunClientConfiguration setRtoScale(double rtoScale) {
        this.rtoScale = rtoScale;
        return this;
    }
    
    public StunClientConfiguration setMeasureNatBindingTimeout(boolean measureNatBindingTimeout) {
        this.measureNatBindingTimeout = measureNatBindingTimeout;
        return this;
    }
}
