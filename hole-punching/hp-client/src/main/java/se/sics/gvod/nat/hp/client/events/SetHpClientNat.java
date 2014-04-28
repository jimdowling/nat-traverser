package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.net.Nat;
import se.sics.kompics.Request;

public final class SetHpClientNat extends Request
{
   private final Nat natType;
    public SetHpClientNat(Nat natType)
    {
        if (natType == null) {
            throw new IllegalArgumentException("Nat was null");
        }
        this.natType = natType;
    }

    public Nat getNatType()
    {
        return natType;
    }
  
}
