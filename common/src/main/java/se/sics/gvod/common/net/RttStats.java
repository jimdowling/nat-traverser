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
package se.sics.gvod.common.net;

import se.sics.gvod.net.VodAddress;


/**
 * The <code>ProbedPeerData</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: ProbedPeerData.java 1076 2009-08-28 11:05:33Z Cosmin $
 */
public final class RttStats {

	private final double avgRTT;
	private final double varRTT;
	private final double rtto;
	private final double showedRtto;
	private final double rttoMin;
	private final VodAddress overlayAddress;
        private final long lastContacted;

	public RttStats(double avgRTT, double varRTT, double rtto,
			double showedRtto, double rttoMin, long lastContacted,
                        VodAddress overlayAddress) {
		super();
		this.avgRTT = avgRTT;
		this.varRTT = varRTT;
		this.rtto = rtto;
		this.showedRtto = showedRtto;
		this.rttoMin = rttoMin;
		this.overlayAddress = overlayAddress;
                this.lastContacted = lastContacted;
	}

    public double getAvgRTT() {
        return avgRTT;
    }

    public long getLastContacted() {
        return lastContacted;
    }

    public VodAddress getOverlayAddress() {
        return overlayAddress;
    }

    public double getRtto() {
        return rtto;
    }

    public double getRttoMin() {
        return rttoMin;
    }

    public double getShowedRtto() {
        return showedRtto;
    }

    public double getVarRTT() {
        return varRTT;
    }
}
