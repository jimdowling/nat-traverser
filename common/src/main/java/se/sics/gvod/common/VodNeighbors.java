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
package se.sics.gvod.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import se.sics.gvod.net.VodAddress;
/**
 * The <code>GVodNeighbors</code> class represents a set of GVod neighbors
 * which is time-stamped with its creation time.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: GVodNeighbors.java 1127 2009-09-01 10:14:37Z Cosmin $
 */
public final class VodNeighbors implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1524921765310674054L;

	private final VodAddress self;

	/**
	 * the current GVod node randomSetDescriptors found in a node's cache.
	 */
	private final ArrayList<VodDescriptor> randomSetDescriptors;
    private final ArrayList<VodDescriptor> upperSetDescriptors;
    private final ArrayList<VodDescriptor> utilitySetDescriptors;
    private final ArrayList<VodDescriptor> neighbourhoodDescriptors;
    private final ArrayList<VodDescriptor> belowSetDescriptors;
	private final long atTime;
    private final long utilitySetNbChange, upperSetNbChange, nbCycles;

	public VodNeighbors(VodAddress self,
			ArrayList<VodDescriptor> randomSetDescriptors,
            ArrayList<VodDescriptor> upperSetDescriptors,
            ArrayList<VodDescriptor> utilitySetDescriptors,
            ArrayList<VodDescriptor> belowSetDescriptors,
            ArrayList<VodDescriptor> neighbourhoodDescriptors,
            long utilitySetNbChange, long upperSetNbChange, long nbCycles) {
		super();
		this.self = self;
		this.randomSetDescriptors = randomSetDescriptors;
        this.upperSetDescriptors=upperSetDescriptors;

        this.utilitySetDescriptors=utilitySetDescriptors;
		this.atTime = System.currentTimeMillis();
        this.neighbourhoodDescriptors=neighbourhoodDescriptors;
        this.belowSetDescriptors=belowSetDescriptors;
        this.utilitySetNbChange=utilitySetNbChange;
        this.upperSetNbChange=upperSetNbChange;
        this.nbCycles=nbCycles;
	}

	/**
	 * @return the node who has these neighbors.
	 */
	public VodAddress getSelf() {
		return self;
	}

	/**
	 * @return the list of GVod node randomSetDescriptors found this node's cache.
	 */
	public ArrayList<VodDescriptor> getRandomSetDescriptors() {
		return randomSetDescriptors;
	}

	/**
	 * @return the creation time of this neighbor set.
	 */
	public long getAtTime() {
		return atTime;
	}


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public ArrayList<VodDescriptor> getUpperSetDescriptors() {
        return upperSetDescriptors;
    }

    public ArrayList<VodDescriptor> getUtilitySetDescriptors() {
        return utilitySetDescriptors;
    }

    public ArrayList<VodDescriptor> getNeighbourhoodDescriptors() {
        return neighbourhoodDescriptors;
    }

    public ArrayList<VodDescriptor> getBelowSetDescriptors() {
        return belowSetDescriptors;
    }

    
    public ArrayList<VodDescriptor> getDescriptors(){
        HashSet<VodDescriptor> descriptors = new HashSet<VodDescriptor>();
        descriptors.addAll(randomSetDescriptors);

        descriptors.addAll(upperSetDescriptors);
        descriptors.addAll(utilitySetDescriptors);
        return new ArrayList<VodDescriptor>(descriptors);
    }

    public double getUpperSetNbChange() {
        return upperSetNbChange;
    }

    public double getUtilitySetNbChange() {
        return utilitySetNbChange;
    }

    public double getNbCycles() {
        return nbCycles;
    }

    
}
