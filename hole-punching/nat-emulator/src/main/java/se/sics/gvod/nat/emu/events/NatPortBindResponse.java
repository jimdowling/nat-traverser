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
    private final int numRetries;

    public NatPortBindResponse(PortBindRequest request, BindingSession session, 
            int numRetries) {
        super(request);
        this.session = session;
        this.numRetries = numRetries;
    }
    public int getNumRetries() {
        return numRetries;
    }

    public BindingSession getSession() {
        return session;
    }

}
