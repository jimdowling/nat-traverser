/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;

/**
 *
 * @author jdowling
 */
public class InterleavedPRC_PortResponse extends PortAllocResponse
{
    private VodAddress zServer;
    public InterleavedPRC_PortResponse(PortAllocRequest request, Integer key, VodAddress zServer) {
        super(request,key);
        this.zServer = zServer;
    }

    public VodAddress getzServer() {
        return zServer;
    }
}
