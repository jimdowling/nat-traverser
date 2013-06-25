/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient.events;

import se.sics.gvod.common.Utility;
import se.sics.kompics.Event;

/**
 *
 * @author jim
 */
public class UtilityChanged extends Event{
    
    private final Utility newUtility;
    
    public UtilityChanged(Utility newUtility)
    {
        this.newUtility = newUtility;
    }

    /**
     * @return the newUtility
     */
    public Utility getNewUtility() {
        return newUtility;
    }
}
