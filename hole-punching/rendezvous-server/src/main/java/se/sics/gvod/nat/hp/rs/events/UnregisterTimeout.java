/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.hp.rs.events;

import se.sics.gvod.address.Address;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;

/**
 *
 * @author jdowling
 */
public class UnregisterTimeout extends Timeout {

    private final Address source;
    private final int id;
    private final TimeoutId requestId;

    public UnregisterTimeout(ScheduleTimeout request, Address source, TimeoutId requestId, int id) {
        super(request);
        this.source = source;
        this.id = id;
        this.requestId = requestId;
    }

    public Address getSource() {
        return source;
    }

    public TimeoutId getRequestId() {
        return requestId;
    }


    public int getId() {
        return id;
    }
}
