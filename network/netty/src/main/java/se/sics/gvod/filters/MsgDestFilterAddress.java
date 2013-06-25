/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.filters;

import se.sics.gvod.address.Address;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.kompics.ChannelFilter;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
    public class MsgDestFilterAddress extends ChannelFilter<RewriteableMsg, Address> {

        public MsgDestFilterAddress(Address address) {
            super(RewriteableMsg.class, address, true);
        }

    @Override
        public Address getValue(RewriteableMsg event) {
            return event.getDestination();
        }
    }
