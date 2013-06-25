package se.sics.gvod.config;

import se.sics.gvod.net.Nat;

public final class RendezvousServerConfiguration
        extends AbstractConfiguration<RendezvousServerConfiguration> {

    /**
     * Fields cannot be private. Package protected, ok.
     */
    int sessionExpirationTime;
    int numChildren;

    /**
     * Default constructor comes first.
     */
    public RendezvousServerConfiguration() {
        super(VodConfig.getSeed());
        this.sessionExpirationTime = Nat.UPPER_RULE_EXPIRATION_TIME;
        this.numChildren = VodConfig.DEFAULT_CHILDREN_SIZE;
    }

    /**
     * Full argument constructor comes second.
     */
    public RendezvousServerConfiguration(int seed, int sessionExpirationTime,
            int numChildren) {
        super(seed);
        this.sessionExpirationTime = sessionExpirationTime;
        this.numChildren = numChildren;
    }

    public static RendezvousServerConfiguration build() {
        return new RendezvousServerConfiguration();
    }

    public int getSessionExpirationTime() {
        return sessionExpirationTime;
    }

    public RendezvousServerConfiguration setSessionExpirationTime(int sessionExpirationTime) {
        this.sessionExpirationTime = sessionExpirationTime;
        return this;
    }

    public int getNumChildren() {
        return numChildren;
    }

    public RendezvousServerConfiguration setNumChildren(int numChildren) {
        this.numChildren = numChildren;
        return this;
    }
}
