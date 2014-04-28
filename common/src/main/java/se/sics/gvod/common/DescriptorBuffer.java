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

import se.sics.gvod.net.VodAddress;
import java.io.Serializable;
import java.util.List;

/**
 * The <code>DescriptorBuffer</code> class represents a buffer of GVod node
 * descriptors sent by one node to another during a shuffle.
 * 
 */
public class DescriptorBuffer implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4414783055393007206L;
    private final VodAddress from;
    private final List<VodDescriptor> publicDescriptors;
    private final List<VodDescriptor> privateDescriptors;

    public DescriptorBuffer(VodAddress from, List<VodDescriptor> publicDescriptors, List<VodDescriptor> privateDescriptors) {
        this.from = from;
        this.publicDescriptors = publicDescriptors;
        this.privateDescriptors = privateDescriptors;
    }

    public List<VodDescriptor> getPublicDescriptors() {
        return publicDescriptors;
    }

    public List<VodDescriptor> getPrivateDescriptors() {
        return privateDescriptors;
    }

    public VodAddress getFrom() {
        return from;
    }

}
