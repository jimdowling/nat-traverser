package se.sics.gvod.config;

public final class NatTraverserConfiguration 
extends AbstractConfiguration<NatTraverserConfiguration>{

    /** 
     * Fields cannot be private. Package protected, ok.
     */
    int stunRetries;
    int connectionEstablishmentWaitTime;
    int responsiblePeerUpdateTime;
    int maxOpenedConnections;

    /** 
     * Default constructor comes first.
     */
    public NatTraverserConfiguration() {
        this(
                VodConfig.NT_STUN_RETRIES, 
                VodConfig.DEFAULT_RTO, 
                VodConfig.DEFAULT_NT_CONNECTION_ESTABLISHMENT_TIMEOUT, 
                VodConfig.NT_MAX_NUM_OPENED_CONNECTIONS);
    }

    /** 
     * Full argument constructor comes second.
     */
    public NatTraverserConfiguration(
            int stunRetries,
            int connectionEstablishmentWaitTime,
            int responsiblePeerUpdateTime,
            int maxOpenedConnections) {
        this.stunRetries = stunRetries;
        this.connectionEstablishmentWaitTime = connectionEstablishmentWaitTime;
        this.responsiblePeerUpdateTime = responsiblePeerUpdateTime;
        this.maxOpenedConnections = maxOpenedConnections;
    }

    public static NatTraverserConfiguration build() {
        return new NatTraverserConfiguration();
    }

    public int getMaxOpenedConnections() {
        return maxOpenedConnections;
    }

    public int getResponsiblePeerUpdateTime() {
        return responsiblePeerUpdateTime;
    }

    public int getStunRetries() {
        return stunRetries;
    }

    public int getConnectionEstablishmentWaitTime() {
        return connectionEstablishmentWaitTime;
    }

    public NatTraverserConfiguration setConnectionEstablishmentWaitTime(int connectionEstablishmentWaitTime) {
        this.connectionEstablishmentWaitTime = connectionEstablishmentWaitTime;
        return this;
    }

    public NatTraverserConfiguration setConnectionRetries(int connectionRetries) {
        this.stunRetries = connectionRetries;
        return this;
    }

    public NatTraverserConfiguration setMaxOpenedConnections(int maxOpenedConnections) {
        this.maxOpenedConnections = maxOpenedConnections;
        return this;
    }

    public NatTraverserConfiguration setResponsiblePeerUpdateTime(int responsiblePeerUpdateTime) {
        this.responsiblePeerUpdateTime = responsiblePeerUpdateTime;
        return this;
    }

}
