/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.common;

import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;


/**
 *
 * @author jdowling
 */
public class SimplePortAllocResponse extends PortAllocResponse {

    public SimplePortAllocResponse(PortAllocRequest request, Integer key) {
        super(request, key);
    }
}

