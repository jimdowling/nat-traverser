/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.emu.events;

import java.util.Set;
import se.sics.gvod.nat.emu.IpIntPair;
import se.sics.gvod.net.events.PortDeleteRequest;
import se.sics.gvod.net.events.PortDeleteResponse;

/**
 *
 * @author jdowling
 */
public class NatPortUnbindResponse extends PortDeleteResponse
{


    public NatPortUnbindResponse(PortDeleteRequest request, Set<Integer> ports, IpIntPair kompicsIp) {
        super(request, kompicsIp, ports);
    }

    public IpIntPair getKompicsIp() {
        return (IpIntPair) getKey();
    }

}
