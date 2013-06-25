/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.croupier.msgs;

import java.io.Serializable;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class HitStats implements Serializable 
{

    private final int nodeId;
    private final int pubCount;
    private final int privCount;
    private int timestamp;

    public HitStats(int nodeId, int publicCount, int privateCount) {
        this(nodeId, publicCount, privateCount, 0);
    }
    public HitStats(int nodeId, int publicCount, int privateCount, int timestamp) {
        this.nodeId = nodeId;
        this.pubCount = publicCount;
        this.privCount = privateCount;
        this.timestamp = timestamp;    
    }

    public int getTimestamp() {
        return timestamp;
    }
    
    public void incrementTimestamp() {
        timestamp++;
    }

    public int getNodeId() {
        return nodeId;
    }
    
    public int getPubCount() {
        return pubCount;
    }
    
    public int getPrivCount() {
        return privCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof HitStats == false) {
            return false;
        }
        HitStats that = (HitStats) obj;
        return this.nodeId == that.nodeId;
    }

    @Override
    public int hashCode() {
        return this.nodeId;
    }
}
