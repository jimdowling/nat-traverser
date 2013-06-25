package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Request;

public final class UnregisterFromRendezvousServer extends Request
{
   private final VodAddress rendezvousServerAddress;
   private final long delay;
    public UnregisterFromRendezvousServer(VodAddress rendezvousServerAddress, long delay)
    {
        this.rendezvousServerAddress = rendezvousServerAddress;
        this.delay = delay;
    }

    public VodAddress getRendezvousServerAddress()
    {
        return rendezvousServerAddress;
    }

    public long getDelay() {
        return delay;
    }

}
