package se.sics.gvod.simulator.common;

import java.math.BigInteger;

import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;

public final class PeerFail extends Event {

    private final Integer peerId;
    private final VodAddress.NatType peerType;
//-------------------------------------------------------------------	

    public PeerFail(Integer peerId, VodAddress.NatType peerType) {
        this.peerId = peerId;
        this.peerType = peerType;
    }

//-------------------------------------------------------------------	
    public VodAddress.NatType getPeerType() {
        return peerType;
    }

//-------------------------------------------------------------------	
    public Integer getPeerId() {
        return peerId;
    }

//-------------------------------------------------------------------	
    @Override
    public String toString() {
        return "Fail@" + peerId;
    }
}
