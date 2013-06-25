package se.sics.gvod.config;

public class HpClientConfiguration extends AbstractConfiguration<HpClientConfiguration> {

    /**
     * Fields cannot be private. Package protected, ok.
     */
    int sessionExpirationTime;
    int scanRetries;
    boolean scanningEnabled;
    int messageRetryDelay;

    /**
     * Default constructor comes first.
     */
    public HpClientConfiguration() {
        this(VodConfig.getSeed(),
                VodConfig.DEFAULT_HP_SESSION_EXPIRATION,
                VodConfig.DEFAULT_HP_SCANNING_RETRIES,
                true,
                VodConfig.DEFAULT_RTO);
    }

    /**
     * Full argument constructor comes second.
     */
    public HpClientConfiguration(int seed, int sessionExpirationTime,
            int scanRetries, boolean scanningEnabled, int messageRetryDelay) {
        super(seed);
        this.scanRetries = scanRetries;
        this.sessionExpirationTime = sessionExpirationTime;
        this.scanningEnabled = scanningEnabled;
        this.messageRetryDelay = messageRetryDelay;
    }

    public static HpClientConfiguration build() {
        return new HpClientConfiguration();
    }

    public int getMessageRetryDelay() {
        return messageRetryDelay;
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

    public HpClientConfiguration setMessageRetryDelay(int messageRetryDelay) {
        this.messageRetryDelay = messageRetryDelay;
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
//    public void store(String file) throws IOException {
//		Properties p = new Properties();
//		p.setProperty("scan.retries", "" + this.scanRetries);
//		p.setProperty("session.expiration.time", "" + this.sessionExpirationTime);
//                p.setProperty("scanning.enabled",""+this.scanningEnabled);
//                p.setProperty("message.retry.delay", ""+this.messageRetryDelay);
//		Writer writer = new FileWriter(file);
//		p.store(writer, "se.sics.kompics.nat.hp.client");
//	}
//
//	public static HpClientConfiguration load(String file) throws IOException
//        {
//		Properties p = new Properties();
//		Reader reader = new FileReader(file);
//		p.load(reader);
//
//		int scanRetries = Integer.parseInt(p.getProperty("scan.retries"));
//		int sessionExpirationTime = Integer.parseInt(p.getProperty("session.expiration.time"));
//                boolean scanningEnabled = Boolean.parseBoolean(p.getProperty("scanning.enabled"));
//                int messageRetryDelay = Integer.parseInt(p.getProperty("message.retry.delay"));
//		return new HpClientConfiguration(sessionExpirationTime, scanRetries, scanningEnabled,messageRetryDelay);
//	}
}
