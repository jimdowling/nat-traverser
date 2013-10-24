/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.hp.client.util;

import java.util.concurrent.ConcurrentHashMap;
import se.sics.gvod.nat.hp.client.OpenedConnection;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author jdowling
 */
public class NatConnection {
    public static void refreshConnection(ConcurrentHashMap<Integer, OpenedConnection> openedConnections,
            VodAddress remote, int myPort) {
            int remoteId = remote.getId();
            OpenedConnection oc = openedConnections.get(remoteId);
            if (oc != null) {
                oc.setLastUsed(System.currentTimeMillis());
                oc.incNumTimesUsed();
            } else {
                oc = new OpenedConnection(myPort, false,
                        remote.getPeerAddress(), remote.getNatBindingTimeout(), true);
                openedConnections.put(remoteId, oc);
            }
    }
    
}
