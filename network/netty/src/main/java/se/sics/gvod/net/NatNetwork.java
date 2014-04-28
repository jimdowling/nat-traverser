/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import se.sics.gvod.common.msgs.RelayMsgNetty;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.kompics.PortType;

/**
 *
 * @author jdowling
 */
public final class NatNetwork extends PortType {

    {
        positive(RewriteableMsg.class);
        negative(RewriteableMsg.class);

        negative(RelayMsgNetty.Request.class);
        negative(RelayMsgNetty.Response.class);
        negative(RelayMsgNetty.Oneway.class);
        positive(RelayMsgNetty.Request.class);
        positive(RelayMsgNetty.Response.class);
        positive(RelayMsgNetty.Oneway.class);
    }
}
