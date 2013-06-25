package se.sics.gvod.common;

import java.io.Serializable;

/**
 * A thread-safe class used to store the node's utility value.
 * @author Jim Dowling<jdowling@sics.se>
 */
public interface Utility extends Serializable {

    public static enum Impl { VodUtility, LsUtility, Unrecognized };
    
    public int getValue();
    
    public Utility clone();
    
    public Impl getImplType();
}