/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import se.sics.gvod.net.events.*;
import se.sics.kompics.PortType;

/**
 *
 * @author jdowling
 * @author Steffen Grohsschmiedt
 */
public class NatNetworkControl extends PortType {
    {
        negative(PortAllocRequest.class);
        negative(PortBindRequest.class);
        negative(PortDeleteRequest.class);
        negative(CloseConnectionRequest.class);
        positive(PortAllocResponse.class);
        positive(PortBindResponse.class);
        positive(PortDeleteResponse.class);
        positive(CloseConnectionResponse.class);

        positive(BandwidthStats.class);
        positive(NetworkSessionOpened.class);
        positive(NetworkSessionClosed.class);
        positive(NetworkException.class);
        positive(NetworkConnectionRefused.class);
    }
}
