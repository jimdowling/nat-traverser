package se.sics.gvod.nat.hp.client;

import java.util.concurrent.ConcurrentHashMap;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.kompics.Init;
import se.sics.gvod.common.hp.HPSessionKey;

public final class HolePunchingClientInit extends Init {

    private final Self self;
    private final ConcurrentHashMap<HPSessionKey, OpenedConnection> openedConnections;
    private final boolean scanningEnabled;
    private final int scanRetries;
    private final int sessionExpirationTime;
    private final int messageRetryDelay;

    public HolePunchingClientInit(Self self,
            ConcurrentHashMap<HPSessionKey,OpenedConnection> openedConnections,
            int scanRetries, boolean scanningEnabled,
            int sessionExpirationTime,
            int messageRetryDelay) {
        assert self != null;
        assert openedConnections != null;
        this.self = self;
        this.openedConnections = openedConnections;
        this.scanRetries = scanRetries;
        this.scanningEnabled = scanningEnabled;
        this.sessionExpirationTime = sessionExpirationTime;
        this.messageRetryDelay = messageRetryDelay;
    }

    public int getMessageRetryDelay() {
        return messageRetryDelay;
    }

    public int getSessionExpirationTime() {
        return sessionExpirationTime;
    }

    public int getScanRetries() {
        return scanRetries;
    }

    public boolean isScanningEnabled() {
        return scanningEnabled;
    }

    public ConcurrentHashMap<HPSessionKey, OpenedConnection> getOpenedConnections() {
        return openedConnections;
    }

    public Self getSelf() {
        return self;
    }
}
