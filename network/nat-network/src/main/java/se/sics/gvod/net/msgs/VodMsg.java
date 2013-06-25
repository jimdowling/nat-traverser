/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.msgs;

import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public abstract class VodMsg extends NatMsg {

    private static final long serialVersionUID = 43434242850L;

    protected VodMsg(VodAddress source, VodAddress destination) {
        this(source, destination, null);
    }

    protected VodMsg(VodAddress source, VodAddress destination,
            TimeoutId timeoutId) {
        this(source, destination, Transport.UDP, timeoutId);
    }

    protected VodMsg(VodAddress source, VodAddress destination,
            Transport protocol, TimeoutId timeoutId) {
        super(source, destination, protocol, timeoutId);
        this.vodSrc = source;
        this.vodDest = destination;
    }

}
