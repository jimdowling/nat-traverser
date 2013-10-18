/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.net.events;

/**
 *
 * @author jdowling
 */
public abstract class DoubleDispatchResponseId<T extends DoubleDispatchRequestId> extends ResponseId
{
    private final int id;

    protected DoubleDispatchResponseId(T request) {
        super(request);
        this.id = request.getId();
        request.setResponse(this);
    }

    @Override
    public int getId() {
        return id;
    }

}
