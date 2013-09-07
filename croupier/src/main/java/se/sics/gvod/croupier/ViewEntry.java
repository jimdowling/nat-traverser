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
package se.sics.gvod.croupier;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.Utility;

/**
 * The <code>RandomViewEntry</code> class represents an entry in a GVod node's
 * randomView. It contains a node descriptor and it marks when and to what peer this
 * entry was last sent to. This information is used in the process of updating
 * the randomView during a shuffle, so that the first randomView entries removed are those
 * that were sent to the peer from whom we received the current shuffle
 * response.
 * 
 * @author Cosmin Arad <cosmin@sics.se>, Gautier Berthou
 */
public class ViewEntry {

    public static enum Order implements Comparator<ViewEntry> {

        ByAge() {
            @Override
            public int compare(ViewEntry o1, ViewEntry o2) {
                if (o1.getDescriptor().getAge() > o2.getDescriptor().getAge()) {
                    return 1;
                } else if (o1.getDescriptor().getAge() < o2.getDescriptor().getAge()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
    }
    /**
     * the node descriptor held by this entry.
     */
    private final VodDescriptor descriptor;
    private final long addedAt;
    private long sentAt;
    /**
     * remembers all peers that this node descriptor was sent to.
     */
    private final Set<VodAddress> sentTo = new HashSet<VodAddress>();

    public ViewEntry(VodDescriptor descriptor) {
        if (descriptor == null) {
            throw new NullPointerException("GVodNodeDescriptor was null");
        }
        this.descriptor = descriptor;
        this.addedAt = System.currentTimeMillis();
        this.sentAt = 0;
    }

    public boolean isEmpty() {
        return descriptor == null;
    }

    public void sentTo(VodAddress peer) {
        sentTo.add(peer);
        sentAt = System.currentTimeMillis();
    }

    public VodDescriptor getDescriptor() {
        return descriptor;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public long getSentAt() {
        return sentAt;
    }

    public boolean wasSentTo(VodAddress peer) {
        return sentTo == null ? false : sentTo.contains(peer);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((descriptor == null) ? 0 : descriptor.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ViewEntry other = (ViewEntry) obj;
        if (descriptor == null) {
            if (other.descriptor != null) {
                return false;
            }
        } else if (descriptor.getId() != other.descriptor.getId()
                || descriptor.getVodAddress().getOverlayId() !=
                    other.descriptor.getVodAddress().getOverlayId()
                ) {
            return false;
        }
        return true;
    }

    public void updateAge(int age) {
        descriptor.setAge(age);
    }

    public void updateUtility(Utility utility) {
        descriptor.setUtility(utility);
    }

}
