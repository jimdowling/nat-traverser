/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.net.events;

import se.sics.kompics.Response;

/**
 *
 * @author jdowling
 */
public abstract class DoubleDispatchResponseId<T extends DoubleDispatchRequestId> extends Response
{
    private final int id;

    protected DoubleDispatchResponseId(T request) {
        super(request);
        this.id = request.getId();
        request.setResponse(this);
    }

    public int getId() {
        return id;
    }

}
