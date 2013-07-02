package se.sics.gvod.common;

import java.io.Serializable;

/**
 * An interface that must be implemented by a thread-safe class 
 * that stores   the node's utility value.
 * @author Jim Dowling<jdowling@sics.se>
 */
public interface Utility extends Serializable {

    public static enum Impl { VodUtility, LsUtility, Unrecognized };
    
    public int getValue();
    
    public Utility clone();
    
    public Impl getImplType();
}