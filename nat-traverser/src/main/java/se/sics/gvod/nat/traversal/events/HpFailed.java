package se.sics.gvod.nat.traversal.events;

import se.sics.gvod.hp.events.OpenConnectionResponseType;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Event;

/**
 *
 * @author Salman
 */

public final class HpFailed extends Event
{
    private final TimeoutId msgTimeoutId;
    private final OpenConnectionResponseType responseType;
    
    public HpFailed(TimeoutId msgTimeoutId, OpenConnectionResponseType responseType) {
        this.msgTimeoutId = msgTimeoutId;
        this.responseType = responseType;
    }

    public OpenConnectionResponseType getResponseType() {
        return responseType;
    }

    public TimeoutId getMsgTimeoutId() {
        return msgTimeoutId;
    }
}
