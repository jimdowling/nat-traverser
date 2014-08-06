/**
 * This file is part of the Kompics component model runtime.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package se.sics.gvod.nat.common;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.events.*;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;

/**
 * The <code>MinaNetwork</code> class.
 *
 * @author Cosmin Arad <cosmin@sics.se>
 * @author Jim Dowling <jdowling@sics.se>
 * @version $Id: MinaGVodNetwork.java 2206 2010-04-20 12:20:15Z jdowling $
 */
public final class PortReservoirComp extends ComponentDefinition {

    Negative<NatNetworkControl> natNetworkControl = negative(NatNetworkControl.class);
    private static final Logger logger = LoggerFactory.getLogger(PortReservoirComp.class);
    private Set<Integer> allocatedPorts = null;
    private Random rand;
    int startRange, endRange;

    public PortReservoirComp(PortInit init) {

        allocatedPorts = new HashSet<Integer>();
        // set a default seed - can be overriden by init handler
        subscribe(handlePortAllocRequest, natNetworkControl);
        subscribe(handlePortDeleteRequest, natNetworkControl);
        subscribe(handlePortBindRequest, natNetworkControl);

        doInit(init);
    }

    private void doInit(PortInit event) {

        rand = new Random(event.getSeed());
        startRange = event.getStartRange();
        endRange = event.getEndRange();
    }
    Handler<PortBindRequest> handlePortBindRequest = new Handler<PortBindRequest>() {

        @Override
        public void handle(PortBindRequest message) {
            PortBindResponse response = message.getResponse();
//            if (allocatedPorts.contains(message.getPort()) == false) {
            allocatedPorts.add(message.getPort());
            response.setStatus(PortBindResponse.Status.SUCCESS);
//            } else {
//                response.setStatus(PortBindResponse.Status.FAIL);
//            }
            trigger(response, natNetworkControl);
        }
    };
    Handler<PortAllocRequest> handlePortAllocRequest = new Handler<PortAllocRequest>() {

        @Override
        public void handle(PortAllocRequest message) {
            int numPorts = message.getNumPorts();

            logger.debug("PortReservoir port allocation request received.");

            Set<Integer> setPorts = new HashSet<Integer>();

            for (int i = 0; i < numPorts; i++) {
                int randPort = -1;
                do {
                    randPort = rand.nextInt(Math.abs(endRange - startRange)) + startRange;

                } while (allocatedPorts.contains(randPort));
                allocatedPorts.add(randPort);
                setPorts.add(randPort);
                logger.debug("PortReservoir allocated port : " + randPort);
            }

            PortAllocResponse response = message.getResponse();
            response.setAllocatedPorts(setPorts);
            trigger(response, natNetworkControl);
        }
    };
    Handler<PortDeleteRequest> handlePortDeleteRequest = new Handler<PortDeleteRequest>() {

        @Override
        public void handle(PortDeleteRequest message) {
            Set<Integer> p = message.getPortsToDelete();
            Set<Integer> deletedPorts = new HashSet<Integer>();
            for (int i : p) {
                if (allocatedPorts.remove(i)) {
                    deletedPorts.add(i);
                }
            }
            if (message.getResponse() != null) {
                PortDeleteResponse response = message.getResponse();
                response.setPorts(deletedPorts);
                trigger(response, natNetworkControl);
            }
        }
    };
}
