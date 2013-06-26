package se.sics.gvod.config;

public final class ParentMakerConfiguration 
        extends AbstractConfiguration<ParentMakerConfiguration>
{
    /** 
     * Fields cannot be private. Package protected, ok.
     */
    // number of parents per child
    int parentSize;
    // Max number of children per parent
    int childSize;
    
    // How often a child tries to update its parents
    int parentUpdatePeriod;
    
    // When a child has been removed, how long do we keep a reference to it before
    // actually removing it. The reason for this is that the NAT Binding will still
    // exist when remove the child, so we might as well maintain a reference to the
    // child until the NAT Binding expires, so any requests using the old parents for 
    // the child can still be routed to the child.
    int childRemoveTimeout;
    
    // Only replace your parent if: new Parent's RTT < current parent's RTT + keepParentRttRange
    int keepParentRttRange;
    
    // How often a child pings its parent to refresh its NAT Binding Timeout (normally 30s)
    int pingRto;
    int pingRetries;
    double pingRtoScale;

    // How often a child pings its parent to refresh its NAT Binding Timeout (normally 30s)
    int rto;
    int rtoRetries;
    double rtoScale;

    /** 
     * Default constructor comes first.
     */
    public ParentMakerConfiguration() {
        this(   VodConfig.getSeed(),
                VodConfig.PM_PARENT_SIZE, 
                VodConfig.PM_CHILDREN_SIZE, 
                VodConfig.PM_PARENT_UPDATE_PERIOD, 
                VodConfig.PM_CHILDREN_REMOVE_TIMEOUT, 
                VodConfig.PM_PARENT_KEEP_RTT_TOLERANCE,
                VodConfig.DEFAULT_RTO_RETRIES, 
                VodConfig.PM_PARENT_RTO, 
                VodConfig.DEFAULT_RTO_SCALE,
                VodConfig.DEFAULT_RTO_RETRIES, 
                15*1000, 
                .5);
    }
    
    /** 
     * Full argument constructor comes second. Contains seed from base class.
     */
    public ParentMakerConfiguration(int seed, int parentSize, int childSize, 
            int parentUpdatePeriod, 
            int removeChildTimeout, int keepParentRttRange, 
            int rtoRetries, int rto, double rtoScale,
            int numPingRetries, int pingRto, double pingRtoScale
            ) {
        super(seed);
        this.parentSize = parentSize;
        this.childSize = childSize;
        this.parentUpdatePeriod = parentUpdatePeriod;
        this.childRemoveTimeout = removeChildTimeout;
        this.keepParentRttRange = keepParentRttRange;
        this.rtoRetries = rtoRetries;
        this.rto = rto;
        this.rtoScale = rtoScale;
        this.pingRetries = numPingRetries;
        this.pingRto = pingRto;
        this.pingRtoScale = pingRtoScale;
    }
    
    public static ParentMakerConfiguration build() {
        return new ParentMakerConfiguration();
    }

    public int getRtoRetries() {
        return rtoRetries;
    }

    public double getRtoScale() {
        return rtoScale;
    }

    public int getRto() {
        return rto;
    }

    public int getKeepParentRttRange() {
        return keepParentRttRange;
    }

    public int getParentSize() {
        return this.parentSize;
    }

    public int getChildSize() {
        return this.childSize;
    }

    public int getParentUpdatePeriod() {
        return this.parentUpdatePeriod;
    }

    public int getChildRemoveTimeout() {
        return this.childRemoveTimeout;
    }

    public ParentMakerConfiguration setChildRemoveTimeout(int childRemoveTimeout) {
        this.childRemoveTimeout = childRemoveTimeout;
        return this;
    }

    public ParentMakerConfiguration setChildSize(int childSize) {
        this.childSize = childSize;
        return this;
    }

    public ParentMakerConfiguration setKeepParentRttRange(int keepParentRttRange) {
        this.keepParentRttRange = keepParentRttRange;
        return this;
    }

    public ParentMakerConfiguration setParentSize(int parentSize) {
        this.parentSize = parentSize;
        return this;
    }

    public ParentMakerConfiguration setParentUpdatePeriod(int parentUpdatePeriod) {
        this.parentUpdatePeriod = parentUpdatePeriod;
        return this;
    }

    public ParentMakerConfiguration setRtoRetries(int rtoRetries) {
        this.rtoRetries = rtoRetries;
        return this;
    }

    public ParentMakerConfiguration setRto(int rto) {
        this.rto = rto;
        return this;
    }

    public ParentMakerConfiguration setRtoScale(double rtoScale) {
        this.rtoScale = rtoScale;
        return this;
    }

    public int getPingRetries() {
        return pingRetries;
    }

    public int getPingRto() {
        return pingRto;
    }

    public double getPingRtoScale() {
        return pingRtoScale;
    }

    public ParentMakerConfiguration setPingRetries(int pingRetries) {
        this.pingRetries = pingRetries;
        return this;
    }

    public ParentMakerConfiguration setPingRto(int pingRto) {
        this.pingRto = pingRto;
        return this;
    }

    public ParentMakerConfiguration setPingRtoScale(double pingRtoScale) {
        this.pingRtoScale = pingRtoScale;
        return this;
    }

    
}
