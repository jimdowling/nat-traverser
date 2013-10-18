/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.events;

import se.sics.kompics.Request;

/**
 *
 * @author jdowling
 */
public abstract class DoubleDispatchRequestId<T extends DoubleDispatchResponseId> extends Request {

    private final int id;
    protected T response;

    protected DoubleDispatchRequestId(int id) {
        this.id = id;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public T getResponse() {
        return response;
    }

    public int getId() {
        return id;
    }
}
