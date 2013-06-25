/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.events;

import se.sics.kompics.Request;
import se.sics.kompics.Request;

/**
 *
 * @author jdowling
 */
public abstract class DoubleDispatchRequest<T extends DoubleDispatchResponse> extends Request {

    protected T response;

    protected DoubleDispatchRequest() {
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public T getResponse() {
        return response;
    }
}
