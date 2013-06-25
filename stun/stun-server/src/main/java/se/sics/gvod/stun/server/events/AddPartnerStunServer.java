/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.stun.server.events;

import se.sics.gvod.stun.server.Partner;
import se.sics.kompics.Event;

/**
 *
 * @author jdowling
 */
public class AddPartnerStunServer extends Event
{

    private final Partner addr;

    public AddPartnerStunServer(Partner addr) {
        this.addr = addr;
    }

    public Partner getPartner() {
        return addr;
    }

}
