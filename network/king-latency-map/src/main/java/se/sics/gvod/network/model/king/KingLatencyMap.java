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
package se.sics.gvod.network.model.king;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;
import se.sics.gvod.network.model.common.NetworkModel;

import se.sics.gvod.address.Address;
import se.sics.gvod.net.msgs.RewriteableMsg;

/**
 * The <code>KingLatencyMap</code> class.
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @author Tallat Shafaat <tallat@sics.se>
 * @version $Id: KingLatencyMap.java 3382 2010-07-31 22:31:43Z jdowling $
 */
public final class KingLatencyMap implements NetworkModel {

	private final Random random;

	private long[] repeatedDiagonal;

	public KingLatencyMap(int seed) {
		random = new Random(seed);
		initRepeatedDiagonal(random);
	}

	public KingLatencyMap(long seed) {
		random = new Random(seed);
		initRepeatedDiagonal(random);
	}

	private void initRepeatedDiagonal(Random r) {
		repeatedDiagonal = new long[KingMatrix.SIZE];

		for (int i = 0; i < repeatedDiagonal.length; i++) {
			int j = r.nextInt(KingMatrix.SIZE);
			int k = r.nextInt(KingMatrix.SIZE);
			while(k == j) {
				k = r.nextInt(KingMatrix.SIZE);
			}
			repeatedDiagonal[i] = KingMatrix.KING[j][k];
		}
	}

	@Override
	public long getLatencyMs(RewriteableMsg message) {
		int s = addressToInt(message.getSource());
		int d = addressToInt(message.getDestination());

		if (s == d) {
			if (message.getSource().getId() != message.getDestination().getId()) {
				return repeatedDiagonal[s];
			}
		}

		long latency = KingMatrix.KING[s][d];
                if (latency == 0) {
                    latency = 50;
                }
                return latency;
	}

	private final int addressToInt(Address address) {
		int h = address.getId();
		h = h < 0 ? -h : h;
		return h % KingMatrix.SIZE;
	}

	private static final class KingMatrix {
		public static final int KING[][];
		public static final int SIZE = 1740;
		static {
			int king[][];
			try {
				ObjectInputStream ois = new ObjectInputStream(KingMatrix.class
						.getResourceAsStream("KingMatrix.data"));
				king = (int[][]) ois.readObject();
			} catch (IOException e) {
				king = null;
                            throw new IllegalStateException("KingMatrix.data file is missing");
			} catch (ClassNotFoundException e) {
				king = null;
                            throw new IllegalStateException("KingMatrix.class file is missing");
			}
			KING = king;
		}
	}
}
