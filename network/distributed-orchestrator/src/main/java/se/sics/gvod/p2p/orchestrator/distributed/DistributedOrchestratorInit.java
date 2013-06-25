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
package se.sics.gvod.p2p.orchestrator.distributed;

import java.net.InetAddress;
import se.sics.gvod.network.model.common.NetworkModel;
import se.sics.kompics.Init;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

/**
 * The <code>P2pOrchestratorInit</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: P2pOrchestratorInit.java 1103 2009-08-31 13:27:58Z Cosmin $
 */
public final class DistributedOrchestratorInit extends Init {

    private final SimulationScenario scenario;
    private final NetworkModel networkModel;
    private final InetAddress ip;
    private final int port;

    public DistributedOrchestratorInit(SimulationScenario scenario,
            NetworkModel networkModel, InetAddress myIp, int myPort) {
        this.scenario = scenario;
        this.networkModel = networkModel;
        this.ip = myIp;
        this.port = myPort;
    }

    public final SimulationScenario getScenario() {
        return scenario;
    }

    public final NetworkModel getNetworkModel() {
        return networkModel;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }


}
