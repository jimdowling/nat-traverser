/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.hp.msgs.HolePunchingMsg;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;

/**
 *
 * @author jdowling
 */
public class PRP_DummyMsgPortResponse extends PortBindResponse
{
    private final boolean prcPrp;
    private final HolePunchingMsg.Request msg;
    
    public PRP_DummyMsgPortResponse(PortBindRequest request, 
            boolean prcPrp, HolePunchingMsg.Request msg) {
        super(request);
        this.prcPrp = prcPrp;
        this.msg = msg;
    }

    public boolean isPrcPrp() {
        return prcPrp;
    }

    public HolePunchingMsg.Request getMsg() {
        return msg;
    }
 
}
