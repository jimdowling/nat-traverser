/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.common.net;

import se.sics.kompics.PortType;

/**
 *
 * @author jdowling
 */
public class PingPort extends PortType
{
    {
        negative(PingRequest.class);
        positive(PingRequest.class);
        negative(PingResponse.class);
        positive(PingResponse.class);
    }

}
