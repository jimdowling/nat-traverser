/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.timer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class UUID implements TimeoutId
{
    private static AtomicInteger globalId = new AtomicInteger(Integer.MIN_VALUE);

    private int id;
    
    public UUID(int id) {
        this.id = id;
    }

    public static TimeoutId nextUUID() {
        globalId.compareAndSet(Integer.MAX_VALUE, Integer.MIN_VALUE);
        return new UUID(globalId.incrementAndGet());
    }
    
    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TimeoutId == false) {
            return false;
        }
        TimeoutId that = (TimeoutId) obj;
        return this.getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }

    
    
}
