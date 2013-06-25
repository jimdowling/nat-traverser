package se.sics.gvod.stun.upnp.events;

import java.util.Map;
import se.sics.kompics.Event;
import se.sics.gvod.stun.upnp.ForwardPortStatus;

public final class MappedPortsChanged extends Event {
 
//    private final int mappedPort;
//    private final ForwardPortStatus status;

    private final Map<Integer,ForwardPortStatus> changedPorts;

    public MappedPortsChanged(Map<Integer, ForwardPortStatus> changedPorts) {
        this.changedPorts = changedPorts;
    }

    public Map<Integer, ForwardPortStatus> getChangedPorts() {
        return changedPorts;
    }

//    public MappedPortChanged(int mappedPort,
//            ForwardPortStatus status) {
//        this.mappedPort = mappedPort;
//        this.status = status;
//    }

//    public int getMappedPort() {
//        return mappedPort;
//    }
//
//    public ForwardPortStatus getStatus() {
//        return status;
//    }
    

}
