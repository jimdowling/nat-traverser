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
package se.sics.gvod.config;

import se.sics.gvod.config.VodConfig.CroupierSelectionPolicy;

/**
 *
 * @author jim
 */
public class CroupierConfiguration 
        extends AbstractConfiguration<CroupierConfiguration>
{
    /** 
     * Fields cannot be private. Package protected, ok.
     */
    int viewSize;
    int shuffleLength;
    long shufflePeriod;
    long shuffleTimeout;
    String policy;

    /** 
     * Default constructor comes first.
     */
    public CroupierConfiguration() {
        this(   VodConfig.getSeed(),
                VodConfig.RANDOM_VIEW_SIZE,
                VodConfig.SHUFFLE_LENGTH,
                VodConfig.SHUFFLE_PERIOD,
                VodConfig.SHUFFLE_TIMEOUT,
                VodConfig.SELECTION_POLICY.name()
                );
    }

    /** 
     * Full argument constructor comes second.
     */
    public CroupierConfiguration(
            int seed,
            int viewSize,
            int shuffleLength,
            long shufflePeriod,
            long shuffleTimeout,
            String policy
            ) {
        super(seed);
        this.viewSize = viewSize;
        this.shuffleLength = shuffleLength;
        this.shufflePeriod = shufflePeriod;
        this.shuffleTimeout = shuffleTimeout;
        this.policy = policy;
    }

    public static CroupierConfiguration build() {
        return new CroupierConfiguration();
    }
    
    public CroupierSelectionPolicy getPolicy() {
        return VodConfig.CroupierSelectionPolicy.create(policy);
    }
    
    public int getViewSize() {
        return viewSize;
    }

    /**
     * @return the setsExchangeDelay
     */
    public long getShuffleTimeout() {
        return shuffleTimeout;
    }

    /**
     * @return the shufflePeriod
     */
    public long getShufflePeriod() {
        return shufflePeriod;
    }

    public int getShuffleLength() {
        return shuffleLength;
    }    
    
    public CroupierConfiguration setPolicy(String policy) {
        this.policy = policy;
        return this;
    }

    public CroupierConfiguration setViewSize(int viewSize) {
        this.viewSize = viewSize;
        return this;
    }

    public CroupierConfiguration setShuffleTimeout(long shuffleTimeout) {
        this.shuffleTimeout = shuffleTimeout;
        return this;
    }

    public CroupierConfiguration setShufflePeriod(long shufflePeriod) {
        this.shufflePeriod = shufflePeriod;
        return this;
    }

    public CroupierConfiguration setShuffleLength(int shuffleLength) {
        this.shuffleLength = shuffleLength;
        return this;
    }

}
