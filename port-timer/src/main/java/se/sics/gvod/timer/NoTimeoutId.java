/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.timer;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class NoTimeoutId implements TimeoutId 
{

    public NoTimeoutId() {
    }
    

    @Override
    public boolean isSupported() {
        return false;
    }

    @Override
    public int getId() {
        throw new UnsupportedOperationException("NoTimeoutId: Should never call getId() on it.");
    }

    @Override
    public String toString() {
        return "NoTimoutId";
    }

}
