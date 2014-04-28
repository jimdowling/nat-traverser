/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.emu.events;

import se.sics.gvod.nat.emu.DistributedNatGatewayEmulator.BindingSession;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;

/**
 *
 * @author jdowling
 */
public class NatPortBindResponse extends PortBindResponse
{
    private final BindingSession session;
    private final int rtoRetries;

    public NatPortBindResponse(PortBindRequest request, BindingSession session, 
            int rtoRetries) {
        super(request);
        this.session = session;
        this.rtoRetries = rtoRetries;
    }
    public int getRtoRetries() {
        return rtoRetries;
    }

    public BindingSession getSession() {
        return session;
    }

}
