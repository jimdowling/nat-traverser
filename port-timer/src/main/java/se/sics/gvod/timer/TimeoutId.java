/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.timer;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public interface TimeoutId {
    public abstract boolean isSupported();
    public abstract int getId();

}
