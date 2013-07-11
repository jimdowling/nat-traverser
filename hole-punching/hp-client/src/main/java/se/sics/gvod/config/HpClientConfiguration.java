package se.sics.gvod.config;

public class HpClientConfiguration extends AbstractConfiguration<HpClientConfiguration> {

    /**
     * Fields cannot be private. Package protected, ok.
     */
    int sessionExpirationTime;
    int scanRetries;
    boolean scanningEnabled;
    int rto;
    int rtoRetries;
    double rtoScale;

    /**
     * Default constructor comes first.
     */
    public HpClientConfiguration() {
        this(VodConfig.getSeed(),
                VodConfig.HP_SESSION_EXPIRATION,
                VodConfig.HP_SCANNING_RETRIES,
                true,
                VodConfig.DEFAULT_RTO,
                VodConfig.DEFAULT_RTO_RETRIES,
                VodConfig.DEFAULT_RTO_SCALE);
    }

    /**
     * Full argument constructor comes second.
     */
    public HpClientConfiguration(int seed, int sessionExpirationTime,
            int scanRetries, boolean scanningEnabled,
            int rto,
            int rtoRetries,
            double rtoScale) {
        super(seed);
        this.scanRetries = scanRetries;
        this.sessionExpirationTime = sessionExpirationTime;
        this.scanningEnabled = scanningEnabled;
        this.rto = rto;
    }

    public static HpClientConfiguration build() {
        return new HpClientConfiguration();
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

    public int getScanRetries() {
        return scanRetries;
    }

    public boolean isScanningEnabled() {
        return scanningEnabled;
    }

    public int getSessionExpirationTime() {
        return sessionExpirationTime;
    }

    public HpClientConfiguration setRto(int rto) {
        this.rto = rto;
        return this;
    }
    
    public HpClientConfiguration setRtoRetries(int rtoRetries) {
        this.rtoRetries = rtoRetries;
        return this;
    }
    
    public HpClientConfiguration setRtoScale(double rtoScale) {
        this.rtoScale = rtoScale;
        return this;
    }

    public HpClientConfiguration setScanRetries(int scanRetries) {
        this.scanRetries = scanRetries;
        return this;
    }

    public HpClientConfiguration setScanningEnabled(boolean scanningEnabled) {
        this.scanningEnabled = scanningEnabled;
        return this;
    }

    public HpClientConfiguration setSessionExpirationTime(int sessionExpirationTime) {
        this.sessionExpirationTime = sessionExpirationTime;
        return this;
    }
}
