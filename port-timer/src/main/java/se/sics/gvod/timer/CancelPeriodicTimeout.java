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
package se.sics.gvod.timer;



import se.sics.kompics.Event;

/**
 * The <code>CancelPeriodicTimeout</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @author Jim Dowling <jdowling@sics.se>
 * @version $Id: CancelPeriodicTimeout.java 481 2009-01-28 01:10:41Z cosmin $
 */
public final class CancelPeriodicTimeout extends Event {

	private TimeoutId timeoutId;

	/**
	 * Instantiates a new cancel periodic timeout.
	 * 
	 * @param timeoutId
	 *            the timeout id
	 */
	public CancelPeriodicTimeout(TimeoutId timeoutId) {
		this.timeoutId = timeoutId;
	}

	/**
	 * Gets the timeout id.
	 * 
	 * @return the timeout id
	 */
	public final TimeoutId getTimeoutId() {
		return timeoutId;
	}
}
