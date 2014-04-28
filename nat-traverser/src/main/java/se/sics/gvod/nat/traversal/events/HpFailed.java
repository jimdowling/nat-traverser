package se.sics.gvod.nat.traversal.events;

import se.sics.gvod.hp.events.OpenConnectionResponseType;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Event;

/**
 *
 * @author Salman
 */

public final class HpFailed extends Event {
    private final TimeoutId msgTimeoutId;
    private final OpenConnectionResponseType responseType;
    private VodAddress dest;
    
    public HpFailed(TimeoutId msgTimeoutId, OpenConnectionResponseType responseType, VodAddress dest) {
        this.msgTimeoutId = msgTimeoutId;
        this.responseType = responseType;
        this.dest = dest;
    }

    public OpenConnectionResponseType getResponseType() {
        return responseType;
    }

    public TimeoutId getMsgTimeoutId() {
        return msgTimeoutId;
    }

    public VodAddress getHpFailedDestNode() {
        return dest;
    }
}
