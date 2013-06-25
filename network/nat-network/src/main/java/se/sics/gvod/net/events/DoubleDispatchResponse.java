/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.net.events;

import se.sics.kompics.Response;
import se.sics.kompics.Response;

/**
 *
 * @author jdowling
 */
public abstract class DoubleDispatchResponse<T extends DoubleDispatchRequest> extends Response
{

    protected DoubleDispatchResponse(T request) {
        super(request);
    }

}
