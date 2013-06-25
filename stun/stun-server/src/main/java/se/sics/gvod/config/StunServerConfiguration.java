package se.sics.gvod.config;

public final class StunServerConfiguration extends AbstractConfiguration<StunServerConfiguration>
{
    /** 
     * Fields cannot be private. Package protected, ok.
     */
    int maxNumPartners;
    int partnerHeartbeatPeriod;
    int minimumRtt;
    int msgRetryDelay;
    int numMsgRetries;
    double msgRetryScale;

    /** 
     * Default constructor comes first.
     */
    public StunServerConfiguration()
    {
        this(VodConfig.getSeed(), 
                VodConfig.DEFAULT_MAX_NUM_PARTNERS, 
                VodConfig.DEFAULT_PARTNER_HEARTBEAT_PERIOD, 
                VodConfig.DEFAULT_PARENT_KEEP_RTT_TOLERANCE, 
                VodConfig.DEFAULT_STUN_RTO, 
                VodConfig.DEFAULT_STUN_NUM_RETRIES, 
                VodConfig.DEFAULT_STUN_RTO_SCALE
                );
    }
    
    /** 
     * Full argument constructor comes second.
     */
    public StunServerConfiguration(
            int seed,
            int maxNumPartners,
            int partnerHeartbeatPeriod,
            int minimumRtt,
            int msgRetryDelay,             
            int numMsgRetries,
            double msgRetryScale
            )
    {
        super(seed);
        this.maxNumPartners = maxNumPartners;
        this.partnerHeartbeatPeriod = partnerHeartbeatPeriod;
        this.msgRetryDelay = msgRetryDelay;
        this.minimumRtt = minimumRtt;
        this.numMsgRetries = numMsgRetries;
        this.msgRetryScale = msgRetryScale;
    }

    public static StunServerConfiguration build() {
        return new StunServerConfiguration();
    }
    
    public int getMsgRetryDelay()
    {
        return msgRetryDelay;
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

    public int getMaxNumPartners() {
        return maxNumPartners;
    }

    public int getPartnerHeartbeatPeriod() {
        return partnerHeartbeatPeriod;
    }
    
    
    
    public StunServerConfiguration setMaxNumPartners(int maxNumPartners) {
        this.maxNumPartners = maxNumPartners;
        return this;
    }
    
    public StunServerConfiguration setPartnerHeartbeatPeriod(int partnerHeartbeatPeriod) {
        this.partnerHeartbeatPeriod = partnerHeartbeatPeriod;
        return this;
    }
    
    public StunServerConfiguration setMinimumRtt(int minimumRtt) {
        this.minimumRtt = minimumRtt;
        return this;
    }

    public StunServerConfiguration setMsgTimeout(int msgTimeout) {
        this.msgRetryDelay = msgTimeout;
        return this;
    }


    public StunServerConfiguration setMsgRetryScale(double msgRetryScale) {
        this.msgRetryScale = msgRetryScale;
        return this;
    }

    public StunServerConfiguration setNumMsgRetries(int numMsgRetries) {
        this.numMsgRetries = numMsgRetries;
        return this;
    }

}
