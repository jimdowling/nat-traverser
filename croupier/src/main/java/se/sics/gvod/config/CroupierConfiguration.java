/**
 * This file is part of the Kompics P2P Framework.
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
package se.sics.gvod.config;

import se.sics.gvod.config.VodConfig.CroupierSelectionPolicy;

/**
 *
 * @author jim
 */
public class CroupierConfiguration
        extends AbstractConfiguration<CroupierConfiguration> {

    /**
     * Fields cannot be private. Package protected, ok.
     */
    // Max Number of references to neighbours stored in a view
    int viewSize;
    // Number of references swapped with neighbours during a shuffle operation
    int shuffleLength;
    // How often a node shuffles its view
    long shufflePeriod;

    // Healer, Swapper, etc. See Jelasity's paper.
    String policy;
    
    // The expected RTO for shuffleRequests
    long rto;
    int rtoRetries;
    double rtoScale;
    

    /**
     * Default constructor comes first.
     */
    public CroupierConfiguration() {
        this(VodConfig.getSeed(),
                VodConfig.CROUPIER__VIEW_SIZE,
                VodConfig.CROUPIER_SHUFFLE_LENGTH,
                VodConfig.CROUPIER_SHUFFLE_PERIOD,
                VodConfig.CROUPIER_SELECTION_POLICY.name(),
                VodConfig.DEFAULT_RTO,
                VodConfig.DEFAULT_RTO_RETRIES,
                VodConfig.DEFAULT_RTO_SCALE);
    }

    /**
     * Full argument constructor comes second.
     */
    public CroupierConfiguration(
            int seed,
            int viewSize,
            int shuffleLength,
            long shufflePeriod,
            String policy,
            long rto,
            int rtoRetries,
            double rtoScale) {
        super(seed);
        this.viewSize = viewSize;
        this.shuffleLength = shuffleLength;
        this.shufflePeriod = shufflePeriod;
        this.policy = policy;
        this.rto = rto;
        this.rtoRetries = rtoRetries;
        this.rtoScale = rtoScale;
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
     * @return the shufflePeriod
     */
    public long getShufflePeriod() {
        return shufflePeriod;
    }

    public int getShuffleLength() {
        return shuffleLength;
    }

    /**
     * Shuffle timeout.
     * @return 
     */
    public long getRto() {
        return rto;
    }

    public int getRtoRetries() {
        return rtoRetries;
    }

    public double getRtoScale() {
        return rtoScale;
    }

    public CroupierConfiguration setPolicy(String policy) {
        this.policy = policy;
        return this;
    }

    public CroupierConfiguration setViewSize(int viewSize) {
        this.viewSize = viewSize;
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

    public CroupierConfiguration setRto(int rto) {
        this.rto = rto;
        return this;
    }

    public CroupierConfiguration setRtoRetries(int rtoRetries) {
        this.rtoRetries = rtoRetries;
        return this;
    }

    public CroupierConfiguration setRtoScale(double rtoScale) {
        this.rtoScale =rtoScale;
        return this;
    }
}
