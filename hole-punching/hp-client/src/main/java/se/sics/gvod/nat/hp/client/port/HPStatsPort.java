/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.hp.client.port;

import se.sics.kompics.PortType;
import se.sics.gvod.nat.hp.client.GetHPStatsRequest;
import se.sics.gvod.nat.hp.client.GetHPStatsResponse;

/**
 *
 * @author salman
 */
public class HPStatsPort extends PortType
{
    {
        negative(GetHPStatsRequest.class);

        positive(GetHPStatsResponse.class);

    }
}
