/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.timer;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * This class gives out a pseudo-random timeoutId, where the 
 * timeoutId is not in the set of the last X timeoutIds that have been allocated.
 * 
 * @author Jim Dowling<jdowling@sics.se>
 */
public class UUID implements TimeoutId
{
    private static final int RECENT_IDS_SIZE=100;
//    private static AtomicInteger globalId = new AtomicInteger(Integer.MIN_VALUE);

    /**
     * Ordered Index over recent time of creation of indexIds: <timeCreated, recentId>
     */
    private static TreeMap<Long,Integer> idxRecentIds = new TreeMap<Long,Integer>();
    private static Set<Integer> recentIds = new HashSet<Integer>();
    private static Random r = null;
    
    
    private int id;
    
    public UUID(int id) {
        this.id = id;
    }

    /** 
     * This static method is used to create a new TimeoutId.
     * @return timeoutId
     */
    public static synchronized TimeoutId nextUUID() {
        if (r == null) {
            // TODO - should really get this from - BaseCommandLineConfig.getSeed()
            r = new Random(System.currentTimeMillis());
        }
        int id = r.nextInt();
        while (recentIds.contains(id)) {
            id = r.nextInt();
        }
        if (recentIds.size() >= RECENT_IDS_SIZE) {
            Map.Entry<Long,Integer> toRemove = idxRecentIds.firstEntry();
            idxRecentIds.remove(toRemove.getKey());
            recentIds.remove(toRemove.getValue());
        }
        recentIds.add(id);
        idxRecentIds.put(System.nanoTime(), id);
//        globalId.compareAndSet(Integer.MAX_VALUE, Integer.MIN_VALUE);
//        return new UUID(globalId.incrementAndGet());
        return new UUID(id);
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
