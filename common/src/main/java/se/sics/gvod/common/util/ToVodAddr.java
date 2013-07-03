/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.util;

import se.sics.gvod.address.Address;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class ToVodAddr {

    public static VodAddress stunServer(Address addr) {
        return new VodAddress(new Address(addr.getIp(), VodConfig.DEFAULT_STUN_PORT,
                addr.getId()), VodConfig.SYSTEM_OVERLAY_ID);
    }
    
    public static VodAddress stunServer2(Address addr) {
        return new VodAddress(new Address(addr.getIp(), VodConfig.DEFAULT_STUN_PORT_2,
                addr.getId()), VodConfig.SYSTEM_OVERLAY_ID);
    }
    
    public static VodAddress stunClient(Address addr) {
        return new VodAddress(addr, VodConfig.SYSTEM_OVERLAY_ID);
    }
    
    // If I want to reduce size of parents in VodAddress, use VodConfig.DEFAULT_PORT,
    // for Address here.
    public static VodAddress hpServer(Address addr) {
        return new VodAddress(addr, VodConfig.SYSTEM_OVERLAY_ID);
    }
    
    public static VodAddress hpClient(Address addr) {
        return new VodAddress(addr, VodConfig.SYSTEM_OVERLAY_ID);
    }
    
    public static VodAddress systemAddr(Address addr) {
        return new VodAddress(addr, VodConfig.SYSTEM_OVERLAY_ID);
    }
    
    public static VodAddress monitor(Address addr) {
        return new VodAddress(addr, VodConfig.SYSTEM_OVERLAY_ID);
    }
    
    public static VodAddress overlay(Address addr, int overlayId) {
        return new VodAddress(addr, overlayId);
    }
}
