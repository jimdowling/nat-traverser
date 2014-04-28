package se.sics.gvod.simulator.common;


import se.sics.gvod.net.VodAddress;
import se.sics.kompics.Event;

public final class PeerJoin extends Event {

	private final Integer peerId;
	private final VodAddress.NatType peerType;

//-------------------------------------------------------------------	
	public PeerJoin(Integer peerId, VodAddress.NatType peerType) {
		this.peerId = peerId;
		this.peerType = peerType;
	}

//-------------------------------------------------------------------	
	public Integer getPeerId() {
		return peerId;
	}

//-------------------------------------------------------------------	
	public VodAddress.NatType getPeerType() {
		return peerType;
	}
}
