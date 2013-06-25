/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.util;

import java.util.Random;
import se.sics.gvod.config.VodConfig;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class PortSelector {
    private static Random r;
    
    static {
        r = new Random(VodConfig.getSeed());
    }
    
    public static int selectRandomPortOver50000() {
        return r.nextInt(15535) + 50000;
    }
}
