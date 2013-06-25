package se.sics.gvod.config;

public final class ParentMakerConfiguration 
        extends AbstractConfiguration<ParentMakerConfiguration>
{
    /** 
     * Fields cannot be private. Package protected, ok.
     */
    int parentSize;
    int childSize;
    long parentUpdatePeriod;
    long childRemoveTimeout;
    long keepParentRttRange;
    int retryNum;
    long retryRto;
    double retryScaleRto;
    int numPingRetries;
    long pingRto;
    double pingScaleRto;

    /** 
     * Default constructor comes first.
     */
    public ParentMakerConfiguration() {
        this(   VodConfig.getSeed(),
                VodConfig.DEFAULT_PARENT_SIZE, 
                VodConfig.DEFAULT_CHILDREN_SIZE, 
                VodConfig.DEFAULT_PARENT_UPDATE_PERIOD, 
                VodConfig.DEFAULT_CHILDREN_REMOVE_TIMEOUT, 
                VodConfig.DEFAULT_PARENT_KEEP_RTT_TOLERANCE,
                VodConfig.DEFAULT_RTO_RETRIES, 
                VodConfig.DEFAULT_PARENT_RTO, 
                VodConfig.DEFAULT_RTO_SCALE,
                VodConfig.DEFAULT_RTO_RETRIES, 
                15*1000, 
                .5);
    }
    
    /** 
     * Full argument constructor comes second. Contains seed from base class.
     */
    public ParentMakerConfiguration(int seed, int parentSize, int childSize, 
            long parentUpdatePeriod, 
            long removeChildTimeout, long keepParentRttRange, 
            int retryNum, long retryRto, double retryScaleRto,
            int numPingRetries, long pingRto, double pingScaleRto
            ) {
        super(seed);
        this.parentSize = parentSize;
        this.childSize = childSize;
        this.parentUpdatePeriod = parentUpdatePeriod;
        this.childRemoveTimeout = removeChildTimeout;
        this.keepParentRttRange = keepParentRttRange;
        this.retryNum = retryNum;
        this.retryRto = retryRto;
        this.retryScaleRto = retryScaleRto;
        this.numPingRetries = numPingRetries;
        this.pingRto = pingRto;
        this.pingScaleRto = pingScaleRto;
    }
    
    public static ParentMakerConfiguration build() {
        return new ParentMakerConfiguration();
    }

    public int getRetryNum() {
        return retryNum;
    }

    public double getRetryScaleRto() {
        return retryScaleRto;
    }

    public long getRetryRto() {
        return retryRto;
    }

    public long getKeepParentRttRange() {
        return keepParentRttRange;
    }

    public int getParentSize() {
        return this.parentSize;
    }

    public int getChildSize() {
        return this.childSize;
    }

    public long getParentUpdatePeriod() {
        return this.parentUpdatePeriod;
    }

    public long getChildRemoveTimeout() {
        return this.childRemoveTimeout;
    }

    public ParentMakerConfiguration setChildRemoveTimeout(long childRemoveTimeout) {
        this.childRemoveTimeout = childRemoveTimeout;
        return this;
    }

    public ParentMakerConfiguration setChildSize(int childSize) {
        this.childSize = childSize;
        return this;
    }

    public ParentMakerConfiguration setKeepParentRttRange(long keepParentRttRange) {
        this.keepParentRttRange = keepParentRttRange;
        return this;
    }

    public ParentMakerConfiguration setParentSize(int parentSize) {
        this.parentSize = parentSize;
        return this;
    }

    public ParentMakerConfiguration setParentUpdatePeriod(long parentUpdatePeriod) {
        this.parentUpdatePeriod = parentUpdatePeriod;
        return this;
    }

    public ParentMakerConfiguration setRetryNum(int retryNum) {
        this.retryNum = retryNum;
        return this;
    }

    public ParentMakerConfiguration setRetryRto(long retryRto) {
        this.retryRto = retryRto;
        return this;
    }

    public ParentMakerConfiguration setRetryScaleRto(double retryScaleRto) {
        this.retryScaleRto = retryScaleRto;
        return this;
    }

    public int getNumPingRetries() {
        return numPingRetries;
    }

    public long getPingRto() {
        return pingRto;
    }

    public double getPingScaleRto() {
        return pingScaleRto;
    }

    public ParentMakerConfiguration setNumPingRetries(int numPingRetries) {
        this.numPingRetries = numPingRetries;
        return this;
    }

    public ParentMakerConfiguration setPingRto(long pingRto) {
        this.pingRto = pingRto;
        return this;
    }

    public ParentMakerConfiguration setPingScaleRto(double pingScaleRto) {
        this.pingScaleRto = pingScaleRto;
        return this;
    }

    
}
