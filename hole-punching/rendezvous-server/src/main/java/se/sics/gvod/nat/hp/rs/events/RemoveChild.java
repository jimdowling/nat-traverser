package se.sics.gvod.nat.hp.rs.events;

import se.sics.kompics.Event;

/**
 *
 * @author Jim
 */
public final class RemoveChild extends Event {

    private final int id;

    public RemoveChild(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
