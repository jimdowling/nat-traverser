package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Request;

public final class RegisterWithRendezvousServerRequest extends Request
{
   private final VodAddress rendezvousServerAddress;
   private final int delta;
   private final long rtt;
   
    public RegisterWithRendezvousServerRequest(VodAddress rendezvousServerAddress, int delta,
            long rtt)
    {
        this.rendezvousServerAddress = rendezvousServerAddress;
        this.delta = delta;
        this.rtt = rtt;
    }

    public long getRtt() {
        return rtt;
    }

    
    public int getDelta() {
        return delta;
    }
    
    public VodAddress getRendezvousServerAddress()
    {
        return rendezvousServerAddress;
    }

}
