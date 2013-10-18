/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.filters;

import se.sics.gvod.net.events.ResponseId;
import se.sics.kompics.ChannelFilter;

/**
 * Filters msgs on the Positive side of the Port.
 * 
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class ResponseIdFilter extends ChannelFilter<ResponseId, Integer> {

    public ResponseIdFilter(int id) {
        super(ResponseId.class, id, true);
    }


    @Override
    public Integer getValue(ResponseId event) {
        return event.getId();
    }
}
