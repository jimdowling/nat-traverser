package se.sics.gvod.nat.traversal.events;

import java.util.List;
import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;

/**
 *
 * @author Salman
 */
public final class StartServices extends Event {

    private final List<VodAddress> nodes;

    public StartServices(List<VodAddress> nodes) {
        assert (nodes != null);
        this.nodes = nodes;
    }

    public List<VodAddress> getNodes() {
        return nodes;
    }
}
