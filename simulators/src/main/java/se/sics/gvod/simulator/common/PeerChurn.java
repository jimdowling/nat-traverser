package se.sics.gvod.simulator.common;


import se.sics.kompics.Event;

public final class PeerChurn extends Event {

    private final Integer peerId;
    private final int operation;
    private final double privateNodesRatio;
    
//-------------------------------------------------------------------	

    public PeerChurn(Integer peerId, int operation, double privateNodesRatio) {
        this.peerId = peerId;
        this.operation = operation;
        this.privateNodesRatio = privateNodesRatio;
    }

//-------------------------------------------------------------------	
    public Integer getPeerId() {
        return peerId;
    }

//-------------------------------------------------------------------	
    public int getOperation() {
        return operation;
    }

//-------------------------------------------------------------------	
    public double getPrivateNodesRatio() {
        return privateNodesRatio;
    }

//-------------------------------------------------------------------	
    @Override
    public String toString() {
        return "Fail@" + peerId;
    }
}
