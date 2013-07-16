package se.sics.gvod.config;

public final class StunServerConfiguration extends AbstractConfiguration<StunServerConfiguration>
{
    /** 
     * Fields cannot be private. Package protected, ok.
     */
    int maxNumPartners;
    int partnerHeartbeatPeriod;
    int minimumRtt;
    long rto;
    int rtoRetries;
    double rtoScale;

    /** 
     * Default constructor comes first.
     */
    public StunServerConfiguration()
    {
        this(
                VodConfig.STUN_MAX_NUM_PARTNERS, 
                VodConfig.STUN_PARTNER_HEARTBEAT_PERIOD, 
                VodConfig.STUN_MIN_RTT, 
                VodConfig.STUN_PARTNER_RTO, 
                VodConfig.STUN_PARTNER_RTO_RETRIES, 
                VodConfig.STUN_RTO_SCALE
                );
    }
    
    /** 
     * Full argument constructor comes second.
     */
    public StunServerConfiguration(
            int maxNumPartners,
            int partnerHeartbeatPeriod,
            int minimumRtt,
            long rto,             
            int rtoRetries,
            double rtoScale
            )
    {
        this.maxNumPartners = maxNumPartners;
        this.partnerHeartbeatPeriod = partnerHeartbeatPeriod;
        this.rto = rto;
        this.minimumRtt = minimumRtt;
        this.rtoRetries = rtoRetries;
        this.rtoScale = rtoScale;
    }

    public static StunServerConfiguration build() {
        return new StunServerConfiguration();
    }
    
    public long getRto()
    {
        return rto;
    }

    public int getMinimumRtt() {
        return minimumRtt;
    }

    public int getRtoRetries() {
        return rtoRetries;
    }

    public double getRtoScale() {
        return rtoScale;
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

    public StunServerConfiguration setRto(long rto) {
        this.rto = rto;
        return this;
    }

    public StunServerConfiguration setRtoRetries(int rtoRetries) {
        this.rtoRetries = rtoRetries;
        return this;
    }

    public StunServerConfiguration setRtoScale(double rtoScale) {
        this.rtoScale = rtoScale;
        return this;
    }


}
