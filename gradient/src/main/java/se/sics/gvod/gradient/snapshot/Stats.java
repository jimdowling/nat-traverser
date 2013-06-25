/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient.snapshot;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class Stats {
    
    // Multiple readers == Gradient, Monitor; Single Write = Gradient
    private volatile int utility = 0;
    private volatile int numCroupierSamples = 0;
    private volatile int numNeighbours = 0;
    private volatile int numReqsSent = 0;
    private volatile int numReqsRecvd = 0;
    private volatile int numTimeouts = 0;
    private volatile int numResps = 0;
    private volatile long sumNeighbourUtilities = 0;

    public Stats() {
    }

    public int getNumResps() {
        return numResps;
    }

    public int getNumCroupierSamples() {
        return numCroupierSamples;
    }

    public int getNumReqsSent() {
        return numReqsSent;
    }

    public int getNumReqsRecvd() {
        return numReqsRecvd;
    }

    public int getNumNeighbours() {
        return numNeighbours;
    }
    public int getUtility() {
        return utility;
    }


    public void incSentRequest() {
        numReqsSent++;
    }
    
    public void incRecvdRequest() {
        numReqsRecvd++;
    }
    
    public void incCroupierSample(int size) {
        numCroupierSamples += size;
    }
    
    public void incResponse() {
        numResps++;
    }
    public void incTimeouts() {
        numTimeouts++;
    }
    
    public long getSumNeighbourUtilities() {
        return sumNeighbourUtilities;
    }

    public void setSumNeighbourUtilities(long sumNeighbourUtilities) {
        this.sumNeighbourUtilities = sumNeighbourUtilities;
    }
    
    public void setNumNeighbours(int numNeighbours) {
        this.numNeighbours = numNeighbours;
    }

    public void setUtility(int utility) {
        this.utility = utility;
    }

    public int getNumTimeouts() {
        return numTimeouts;
    }
    
}
