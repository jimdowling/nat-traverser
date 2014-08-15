/**
 * This file is part of the Kompics component model runtime.
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
package se.sics.gvod.nat.common;

import se.sics.kompics.Init;

/**
 * The <code>MinaNetworkInit</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @author Jim Dowling <jdowling@sics.se>
 * @version $Id: MinaNetworkInit.java -1   $
 */
public final class PortInit extends Init<PortReservoirComp> {

    private final long seed;
    private final int startRange;
    private final int endRange;

    public PortInit(long seed) {
        this.seed = seed;
        startRange = 1025;
        endRange = 65535;
    }

    public PortInit(long seed, int startRange, int endRange) {
        this.seed = seed;
        this.startRange = startRange;
        this.endRange = endRange;
    }

    public int getStartRange() {
        return startRange;
    }

    public int getEndRange() {
        return endRange;
    }

    public long getSeed() {
        return seed;
    }

}
