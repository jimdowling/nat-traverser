/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.msgs;

import se.sics.gvod.address.Address;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public abstract class NatMsg extends RewriteableMsg {

    private static final long serialVersionUID = 7542655990L;
    protected VodAddress vodSrc;
    protected VodAddress vodDest;

    protected NatMsg(VodAddress source, VodAddress destination,
            TimeoutId timeoutId) {
        this(source, destination, Transport.UDP, timeoutId);
    }

    protected NatMsg(VodAddress source, VodAddress destination,
            Transport protocol, TimeoutId timeoutId) {
        super(source.getPeerAddress(), destination.getPeerAddress(),
                protocol, timeoutId);
        this.vodSrc = source;
        this.vodDest = destination;
    }

    public VodAddress getVodSource() {
        return vodSrc;
    }

    public VodAddress getVodDestination() {
        return vodDest;
    }

    @Override
    public boolean rewritePublicSource(Address natSource) {
        if (super.rewritePublicSource(natSource)) {
            vodSrc = new VodAddress(natSource, vodSrc.getOverlayId(), vodSrc.getNat());
            return true;
        }
        return false;
    }

    // Used by NAT Emulator
    @Override
    public boolean rewriteDestination(Address newDestination) {
        if (super.rewriteDestination(newDestination)) {
            vodDest = new VodAddress(newDestination, vodDest.getOverlayId(), vodDest.getNat());
            return true;
        }
        return false;
    }
}
