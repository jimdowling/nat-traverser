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
    private final int numRetries;
    private final double scaleRto;

    public GetNatTypeRequest(Set<Address> stunServerAddresses) {
        this(stunServerAddresses, 0);
    }
    
    public GetNatTypeRequest(Set<Address> stunServerAddresses, int timeout) {
        this(stunServerAddresses, timeout, 
                StunClientConfiguration.build().getMsgRetryDelay(), 
                StunClientConfiguration.build().getNumMsgRetries(), 
                StunClientConfiguration.build().getMsgRetryDelay());
    }

    public GetNatTypeRequest(Set<Address> stunServerAddresses, int timeout,
            int msgRetryTimeout,  int numRetries, double scaleRto) {

        if (stunServerAddresses == null) {
            throw new IllegalArgumentException("Stun server address was null.");
        }

        this.stunServerAddresses = stunServerAddresses;
        this.timeout = timeout;
        this.msgRetryTimeout = msgRetryTimeout;
        this.numRetries = numRetries;
        this.scaleRto = scaleRto;
    }

    public Set<Address> getStunServerAddresses() {
        return this.stunServerAddresses;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public int getMsgRetryTimeout() {
        return msgRetryTimeout;
    }

    public int getNumRetries() {
        return numRetries;
    }

    public double getScaleRto() {
        return scaleRto;
    }
    
}
