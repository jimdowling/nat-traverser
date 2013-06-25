package se.sics.gvod.config;

import se.sics.gvod.net.Nat;

public final class StunClientConfiguration extends AbstractConfiguration<StunClientConfiguration>
{
    /** 
     * Fields cannot be private. Package protected, ok.
     */
    int randTolerance;
    int ruleExpirationMinWait;
    int ruleExpirationIncrement;
    int upnpDiscoveryTimeout;
    int upnpTimeout;
    boolean upnpEnable;
    int minimumRtt;
    int msgRetryDelay;
    int numMsgRetries;
    double msgRetryScale;

    /** 
     * Default constructor comes first.
     */
    public StunClientConfiguration()
    {
        this(VodConfig.getSeed(), 
                2, 
                Nat.DEFAULT_RULE_EXPIRATION_TIME, 
                Nat.DEFAULT_RULE_EXPIRATION_TIME, 
                VodConfig.DEFAULT_UPNP_DISCOVERY_TIMEOUT, 
                VodConfig.DEFAULT_UPNP_TIMEOUT, 
                VodConfig.DEFAULT_UPNP_ENABLED, 
                VodConfig.DEFAULT_PARENT_KEEP_RTT_TOLERANCE, 
                VodConfig.DEFAULT_STUN_RTO, 
                VodConfig.DEFAULT_STUN_NUM_RETRIES, 
                VodConfig.DEFAULT_STUN_RTO_SCALE
                );
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
            int msgRetryDelay,             
            int numMsgRetries,
            double msgRetryScale
            )
    {
        super(seed);
        this.randTolerance = randTolerance;
        this.ruleExpirationMinWait = ruleExpirationMinWait;
        this.ruleExpirationIncrement = ruleExpirationIncrement;
        this.msgRetryDelay = msgRetryDelay;
        this.upnpDiscoveryTimeout = upnpDiscoveryTimeout;
        this.upnpTimeout = upnpTimeout;
        this.upnpEnable = upnpEnable;
        this.minimumRtt = minimumRtt;
        this.numMsgRetries = numMsgRetries;
        this.msgRetryScale = msgRetryScale;
    }

    public static StunClientConfiguration build() {
        return new StunClientConfiguration();
    }
    
    public int getMsgRetryDelay()
    {
        return msgRetryDelay;
    }

    public int getRandTolerance()
    {
        return randTolerance;
    }

    public int getRuleExpirationIncrement()
    {
        return ruleExpirationIncrement;
    }

    public int getRuleExpirationMinWait()
    {
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
        return minimumRtt;
    }

    public int getNumMsgRetries() {
        return numMsgRetries;
    }

    public double getMsgRetryScale() {
        return msgRetryScale;
    }
    
    public StunClientConfiguration setMinimumRtt(int minimumRtt) {
        this.minimumRtt = minimumRtt;
        return this;
    }

    public StunClientConfiguration setMsgTimeout(int msgTimeout) {
        this.msgRetryDelay = msgTimeout;
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

    public StunClientConfiguration setMsgRetryScale(double msgRetryScale) {
        this.msgRetryScale = msgRetryScale;
        return this;
    }

    public StunClientConfiguration setNumMsgRetries(int numMsgRetries) {
        this.numMsgRetries = numMsgRetries;
        return this;
    }

}
