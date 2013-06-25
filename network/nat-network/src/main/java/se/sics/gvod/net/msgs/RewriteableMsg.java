package se.sics.gvod.net.msgs;

import java.io.Serializable;
import se.sics.kompics.Event;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.Transport;
import se.sics.gvod.timer.NoTimeoutId;
import se.sics.gvod.timer.TimeoutId;

public abstract class RewriteableMsg extends Event implements Serializable, 
        RewriteableMessageInterface
{

    private static final long serialVersionUID = 345765672342526L;
    protected Address source;
    /**
     * Only serialize the 'id' field of the destination object
     */
    protected Address destination;
    private transient Transport protocol;
    protected TimeoutId timeoutId = null;

    public RewriteableMsg(Address source, Address destination) {
        this(source, destination, Transport.UDP);
    }

    public RewriteableMsg(Address source, Address destination, Transport protocol) {
        this(source, destination, protocol, null);
    }

    public RewriteableMsg(Address source, Address destination, Transport protocol,
            TimeoutId timeoutId) {

        if (source == null) {
            throw new NullPointerException("Source Address cannot be null.");
        }
        if (destination == null) {
            throw new NullPointerException("Destination Address cannot be null.");
        }
        if (protocol == null) {
            throw new NullPointerException("Transport protocol cannot be null.");
        }
        this.source = source;
        this.destination = destination;
        this.protocol = protocol;
        if (timeoutId != null) {
            this.timeoutId = timeoutId;
        } else {
            this.timeoutId = new NoTimeoutId();
        }
    }
    
    public boolean hasTimeout() {
        return (this.timeoutId instanceof NoTimeoutId) ? false : this.timeoutId.isSupported();
    }

     
    @Override
    public int getSize() {
        throw new UnsupportedOperationException("Not supported at this level.");
    }
    
    public Address getSource() {
        return source;
    }

    public Address getDestination() {
        return destination;
    }

    public boolean rewritePublicSource(Address natSource) {
        if (this.source.equals(natSource) == true) {
            return false;
        }
        this.source = natSource;
        return true;
    }


    // Used by NAT Emulator
    public boolean rewriteDestination(Address newDestination) {
        if (this.destination.equals(newDestination)) {
            return false;
        }
        this.destination = newDestination;
        return true;
    }

    /**
     * Sets the protocol.
     *
     * @param protocol
     *            the new protocol
     */
    public final void setProtocol(Transport protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public final Transport getProtocol() {
        return protocol;
    }

    public void setTimeoutId(TimeoutId timeoutId) {
        this.timeoutId = timeoutId;
    }

    public TimeoutId getTimeoutId() {
        return timeoutId;
    }

    @Override
    public boolean equals(Object o) {
        if ((o instanceof RewriteableMsg) == false) {
            return false;
        }
        RewriteableMsg that = (RewriteableMsg) o;

        if (this.source.equals(that.getSource()) == false
                || this.destination.equals(that.getDestination()) == false
                ) {
            return false;

        }
        return true;
    }

    public abstract RewriteableMsg copy();

    @Override
    public int hashCode() {
        return 17 * source.hashCode() + destination.hashCode();
    }
    
}