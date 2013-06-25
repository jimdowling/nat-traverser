/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import se.sics.kompics.PortType;
import se.sics.gvod.net.events.NetworkConnectionRefused;
import se.sics.gvod.net.events.NetworkException;
import se.sics.gvod.net.events.NetworkSessionClosed;
import se.sics.gvod.net.events.NetworkSessionOpened;
import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.net.events.PortDeleteRequest;
import se.sics.gvod.net.events.PortDeleteResponse;

/**
 *
 * @author jdowling
 */
public class NatNetworkControl extends PortType {
    {
        negative(PortAllocRequest.class);
        negative(PortBindRequest.class);
        negative(PortDeleteRequest.class);
        positive(PortAllocResponse.class);
        positive(PortBindResponse.class);
        positive(PortDeleteResponse.class);

        positive(NetworkSessionOpened.class);
        positive(NetworkSessionClosed.class);
        positive(NetworkException.class);
        positive(NetworkConnectionRefused.class);
    }
}
