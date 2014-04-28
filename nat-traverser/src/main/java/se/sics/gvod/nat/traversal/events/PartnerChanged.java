package se.sics.gvod.nat.traversal.events;

import se.sics.gvod.address.Address;
import se.sics.gvod.config.VodConfig;
import se.sics.kompics.Event;



/**
 *
 * @author Salman
 */

public final class PartnerChanged extends Event
{
   private Address newPartner;
    public PartnerChanged(Address newPartner)
    {
        this.newPartner = new Address(newPartner.getIp(),
                    VodConfig.DEFAULT_STUN_PORT,
                    newPartner.getId());
    }

    public Address getNewPartner()
    {
        return newPartner;
    }

    
}
