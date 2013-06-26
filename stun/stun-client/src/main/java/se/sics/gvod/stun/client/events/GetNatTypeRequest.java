package se.sics.gvod.stun.client.events;

import java.util.Set;
import se.sics.kompics.Event;
import se.sics.gvod.address.Address;
import se.sics.gvod.config.StunClientConfiguration;

public final class GetNatTypeRequest extends Event {

    //Stun server list
    private final Set<Address> stunServerAddresses;
    private final int timeout;
    private final int msgRetryTimeout;
    private final int rtoRetries;
    private final double rtoScale;
    private final boolean measureNatBindingTimeout;

    public GetNatTypeRequest(Set<Address> stunServerAddresses, boolean measureNatBindingTimeout) {
        this(stunServerAddresses, 0, measureNatBindingTimeout);
    }
    
    public GetNatTypeRequest(Set<Address> stunServerAddresses, int timeout, 
            boolean measureNatBindingTimeout) {
        this(stunServerAddresses, 
                timeout,
                measureNatBindingTimeout,
                StunClientConfiguration.build().getRto(), 
                StunClientConfiguration.build().getRtoRetries(), 
                StunClientConfiguration.build().getRto()
                );
    }

    public GetNatTypeRequest(Set<Address> stunServerAddresses, int timeout,
            boolean measureNatBindingTimeout,
            int msgRetryTimeout,  int rtoRetries, double rtoScale) {

        if (stunServerAddresses == null) {
            throw new IllegalArgumentException("Stun server address was null.");
        }

        this.stunServerAddresses = stunServerAddresses;
        this.timeout = timeout;
        this.measureNatBindingTimeout = measureNatBindingTimeout;
        this.msgRetryTimeout = msgRetryTimeout;
        this.rtoRetries = rtoRetries;
        this.rtoScale = rtoScale;
    }

    public boolean isMeasureNatBindingTimeout() {
        return measureNatBindingTimeout;
    }
    
    public Set<Address> getStunServerAddresses() {
        return this.stunServerAddresses;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public int getRto() {
        return msgRetryTimeout;
    }

    public int getRtoRetries() {
        return rtoRetries;
    }

    public double getRtoScale() {
        return rtoScale;
    }
    
}
