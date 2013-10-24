/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.events;

import se.sics.kompics.Request;

/**
 * A DoubleDispatchRequestId object is used to support double-dispatching
 * of requests - that is, components that send requests supply their own
 * handler for receiving responses to those requests.
 * 
 * Handlers h1 and h2 in the same component A send Request events to component B. 
 * Component B will send response events to component A, but B will define different
 * response event handlers for the request that came from h1 and the request that
 * came from h2. 
 * The Id here can be used to filter responses to higher level components.
 * 
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
