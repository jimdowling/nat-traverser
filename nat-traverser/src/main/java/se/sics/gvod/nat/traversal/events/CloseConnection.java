/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.traversal.events;

import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;

/**
 *
 * @author jdowling
 */
public class CloseConnection extends Event {
    
    private final int overlayId;
    private final VodAddress dest;

    public CloseConnection(int overlayId, VodAddress dest) {
        this.overlayId = overlayId;
        this.dest = dest;
    }

    public VodAddress getDest() {
        return dest;
    }

    public int getOverlayId() {
        return overlayId;
    }
    
}
