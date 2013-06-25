/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.gvod.nat.hp.clientmain;

import se.sics.gvod.config.NatConfiguration;
import se.sics.gvod.nat.emu.NatGatewayConfiguration;

import se.sics.gvod.config.HpClientConfiguration;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.config.VodConfig;

/**
 * The <code>Configuration</code> class.
 *
 */
public class Configuration {

    int sec = 1000;
    NatGatewayConfiguration natGatewayConfiguration;

    public Configuration(int port, int ruleExpiration, int delta, long seed,
            int natType, int mp, int ap, int fp, int altAp, boolean upnp,
            int startPortRange, int endPortRange, int natId) {
        natGatewayConfiguration = new NatGatewayConfiguration(ruleExpiration,
                delta, seed, natType, mp, ap, fp, altAp, upnp, startPortRange,
                endPortRange, natId);
    }
    // used by RealHpClient
    public Configuration() {
    }

    //
    // format: MappingPolicy_AllocationPolicy_FilteringPolicy_AlternativeAllocationPolicy_Percentage
    // percentage cant have more than one decimal place
    String nats = "m(EI)_a(PP)_f(PD)_alt(PC)_25.2$" // roberto's statistics
            + "OPEN_21.4$"
            + "m(PD)_a(RD)_f(PD)_alt(PC)_12.4$"
            + "m(EI)_a(RD)_f(PD)_alt(PC)_9.9$"
            + "m(EI)_a(PP)_f(EI)_alt(PC)_9.3$"
            + "m(EI)_a(RD)_f(EI)_alt(PC)_7.7$"
            + "m(EI)_a(PP)_f(HD)_alt(PC)_4.6$"
            + "m(EI)_a(PC)_f(EI)_alt(PC)_4.4$"
            + "m(PD)_a(PP)_f(PD)_alt(PC)_2.6$"
            + "m(PD)_a(PP)_f(EI)_alt(PC)_0.7$"
            + "m(PD)_a(RD)_f(EI)_alt(PC)_0.6$"
            + "m(EI)_a(PC)_f(PD)_alt(PC)_0.6$"
            + "m(EI)_a(RD)_f(HD)_alt(PC)_0.4$"
            + "m(HD)_a(RD)_f(PD)_alt(PC)_0.2$";
    String nats2 = "m(EI)_a(PP)_f(PD)_alt(PC)_25.2$" // my modified stats . no hd filtering
            + "OPEN_21.4$"
            + "m(PD)_a(RD)_f(PD)_alt(PC)_12.4$"
            + "m(EI)_a(RD)_f(PD)_alt(PC)_9.9$"
            + "m(EI)_a(PP)_f(EI)_alt(PC)_9.3$"
            + "m(EI)_a(RD)_f(EI)_alt(PC)_7.7$"
            + "m(EI)_a(PP)_f(EI)_alt(PC)_4.6$"
            + "m(EI)_a(PC)_f(EI)_alt(PC)_4.4$"
            + "m(PD)_a(PP)_f(PD)_alt(PC)_2.6$"
            + "m(PD)_a(PP)_f(EI)_alt(PC)_0.7$"
            + "m(PD)_a(RD)_f(EI)_alt(PC)_0.6$"
            + "m(EI)_a(PC)_f(PD)_alt(PC)_0.6$"
            + "m(EI)_a(RD)_f(EI)_alt(PC)_0.4$"
            + "m(HD)_a(RD)_f(PD)_alt(PC)_0.2$";
    String nats1 = "m(EI)_a(PP)_f(PD)_alt(PC)_50$"
            + "OPEN_50$";
    NatConfiguration natConfiguration = new NatConfiguration(
            VodConfig.getSeed(),
            nats1,
            0f,
            60 * sec,
//            new int[]{
//                30 * sec,
//                60 * sec,
//                90 * sec,
//                120 * sec,
//                150 * sec,
//                180 * sec // randomly one of the values will be selected
//            }, 
            1
            );
    StunClientConfiguration stunClientConfiguration =
            new StunClientConfiguration(
            VodConfig.getSeed(),
            10/*rand tolerance*/,
            30 * sec/*rule expiration min wait*/,
            10 * sec/*rule expiration increment*/,
            2 * sec /*upnp  discovery timeout*/,
            5 * sec /*upnp  timeout*/,
            true /*upnp  enabled*/,
            50 /* min rtt */,
            3 * sec/*msg timeout*/,
            1 * sec/*number of retries*/,
            1.5 * sec/*scale retry timeout*/
            );


    int sessionExpirationTime = 30 * sec; // 30 secs. it depends on the scan retries.
    // more scan retries means greater session expiration time.
    HpClientConfiguration hpClientConfig =
            new HpClientConfiguration(VodConfig.getSeed(),
            sessionExpirationTime,
            5/* scanning retries.*/,
            true/*enable port scanning*/,
            2 * sec);
    RendezvousServerConfiguration rendezvousServerConfig = 
            RendezvousServerConfiguration.build().
            setSessionExpirationTime(sessionExpirationTime).
            setNumChildren(10);

}
