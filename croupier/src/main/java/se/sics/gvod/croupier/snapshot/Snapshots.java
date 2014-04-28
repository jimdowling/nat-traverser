
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.croupier.snapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import se.sics.gvod.common.Self;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class Snapshots <T extends Snapshot>  { 
    
    private Map<Integer,Map<Integer,T>> snapshots = 
            new ConcurrentHashMap<Integer,Map<Integer,T>>();
    // package protected 
    Snapshots() {
    }

    
    // TODO: problem - parentmaker and croupier share the same snapshot
    // but have different overlayIds.
    // Maybe separate snaphot objects?
    public T get(Self self) {
        Map<Integer,T> m = snapshots.get(self.getId());
        assert(m != null);
        return m.get(self.getOverlayId());
    }
    
    public void put(Self self, T t) {
        Map<Integer,T> m = snapshots.get(self.getId());
        if (m == null) {
            m = new HashMap<Integer,T>();
            snapshots.put(self.getId(), m);
        }
        m.put(self.getOverlayId(), t);
    }
}
