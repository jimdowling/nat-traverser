/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient;

import se.sics.gvod.common.UtilityVod;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class UtilityRange implements Comparable {

    private final int startChunk;
    private final int endChunk;

    public UtilityRange(int startChunk, int endChunk) {
        this.startChunk = startChunk;
        this.endChunk = endChunk;
    }

    public int getEndChunk() {
        return endChunk;
    }

    public int getStartChunk() {
        return startChunk;
    }

    @Override
    public int hashCode() {
        int prime = 3;
        return startChunk * prime + endChunk * prime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof UtilityRange == false) {
            return false;
        }
        UtilityRange that = (UtilityRange) obj;
        return (this.startChunk == that.startChunk && this.endChunk == that.endChunk);
    }

    public boolean inRange(UtilityVod u) {
        return startChunk <= u.getChunk() && u.getChunk() <= endChunk;
    }

    public boolean inRange(int chunk) {
        return startChunk <= chunk && chunk <= endChunk;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == null) {
            return -1;
        }
        if (obj == this) {
            return 0;
        }
        if (obj instanceof UtilityRange == false) {
            return -1;
        }
        UtilityRange that = (UtilityRange) obj;
        if (this.startChunk == that.startChunk && this.endChunk == that.endChunk) {
        return 0;
        }

        if (this.startChunk < that.startChunk) {
            return -1;
        }
        return 1;
    }
}
