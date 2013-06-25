/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics;

import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.msgs.RewriteableMsg;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class Evt extends PortType {

    {
        positive(Ping.class);
        positive(Pong.class);
        negative(Ping.class);
        negative(Pong.class);
        negative(RewriteableMsg.class);
        negative(VodMsg.class);
    }
}    
