/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.util;

import se.sics.gvod.net.Nat;

/**
 *
 * @author jdowling
 */
public class NatStr {
    
    public static String pairAsStr(Nat n1, Nat n2) {
        return n1.toString() + "<=>" + n2.toString();
    }
    
}
