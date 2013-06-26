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

/**
 *
 * @author jim
 */
public class GradientConfiguration 
        extends AbstractConfiguration<GradientConfiguration>
{

    /** 
     * Fields cannot be private. Package protected, ok.
     */
    // Max Number of references to neighbours stored in a gradient view
    int viewSize;
    // Number of best similar peers that should be sent in each sets exchange response.
    int shuffleLength; //10
    // How often to exchange gradient views with a neighbour
    int shufflePeriod;
    // When searching up the Gradient, ???
    int utilityThreshold;
    // Fingers are long-range links (small world links) to nodes higher up the gradient
    int numFingers; 
    // Range is from 0-1.0. Higher temperature (nearer 1) causes the gradient to converge slower, 
    // but with lower probability of having multiple nodes thinking they are the leader.
    // Lower temperatures (nearer 0.5) cause increasingly random neighbour selection.
    double temperature; 

    // Timeout for search msgs up the Gradient
    int searchRequestTimeout;
    // Number of parallel probes to send
    int numParallelSearches; // 5
    //Time-To-Live used for Gradient search messages.
    int searchTtl; //5
    int rto;

    /** 
     * Default constructor comes first.
     */
    public GradientConfiguration() {
        this(VodConfig.getSeed(),
                VodConfig.GRADIENT_VIEW_SIZE,
                VodConfig.GRADIENT_SHUFFLE_LENGTH,
                VodConfig.GRADIENT_SHUFFLE_PERIOD,
                VodConfig.GRADIENT_UTILITY_THRESHOLD,
                VodConfig.GRADIENT_NUM_FINGERS,
                VodConfig.GRADIENT_TEMPERATURE,
                VodConfig.GRADIENT_SEARCH_TIMEOUT,
                VodConfig.GRADIENT_NUM_PARALLEL_SEARCHES,
                VodConfig.GRADIENT_SEARCH_TTL,
                VodConfig.GRADIENT_SHUFFLE_TIMEOUT
                );
    }

    /** 
     * Full argument constructor comes second.
     */
    public GradientConfiguration(
            int seed, 
            int viewSize, 
            int shuffleLength,
            int shufflePeriod,
            int utilityThreshold,
            int numFingers,
            double temperature,
            int searchRequestTimeout,
            int numParallelSearches,
            int searchTtl,
            int rto
            ) {
        super(seed);
        this.viewSize = viewSize;
        this.shuffleLength = shuffleLength;
        this.shufflePeriod = shufflePeriod;
        this.searchRequestTimeout = searchRequestTimeout;
        this.utilityThreshold = utilityThreshold;
        this.numParallelSearches = numParallelSearches;
        this.searchTtl = searchTtl;
        this.numFingers = numFingers;
        this.temperature = temperature;
        this.rto = rto;
    }

    public static GradientConfiguration build() {
        return new GradientConfiguration();
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public int getViewSize() {
        return viewSize;
    }

    public int getSearchTtl() {
        return searchTtl;
    }

    public int getRto() {
        return rto;
    }

    public int getFingers() {
        return numFingers;
    }

    public int getNumProbes() {
        return numParallelSearches;
    }

    /**
     * @return the setsExchangeDelay
     */
    public int getSetsExchangeDelay() {
        return rto;
    }

    /**
     * @return the setsExchangePeriod
     */
    public int getSetsExchangePeriod() {
        return shufflePeriod;
    }

    /**
     * @return the probeRequestTimeout
     */
    public int getSearchRequestTimeout() {
        return searchRequestTimeout;
    }

    /**
     * @return the utilityThreshold
     */
    public int getUtilityThreshold() {
        return utilityThreshold;
    }

    /**
     * @return the numOfProbes
     */
    public int getNumParallelSearches() {
        return numParallelSearches;
    }

    /**
     * @return the numberOfBestSimilarPeers
     */
    public int getNumBestSimilarPeers() {
        return shuffleLength;
    }

    public GradientConfiguration setSimilarSetSize(int similarSetSize) {
        this.viewSize = similarSetSize;
        return this;
    }

    public GradientConfiguration setShufflePeriod(int shufflePeriod) {
        this.shufflePeriod = shufflePeriod;
        return this;
    }

    public GradientConfiguration setRto(int rto) {
        this.rto = rto;
        return this;
    }

    public GradientConfiguration setSearchRequestTimeout(int searchRequestTimeout) {
        this.searchRequestTimeout = searchRequestTimeout;
        return this;
    }

    public GradientConfiguration setUtilityThreshold(int utilityThreshold) {
        this.utilityThreshold = utilityThreshold;
        return this;
    }

    public GradientConfiguration setNumParallelSearches(int numParallelSearches) {
        this.numParallelSearches = numParallelSearches;
        return this;
    }

    public GradientConfiguration setSearchTtl(int searchTtl) {
        this.searchTtl = searchTtl;
        return this;
    }

    public GradientConfiguration setNumBestSimilarPeers(int numBestSimilarPeers) {
        this.shuffleLength = numBestSimilarPeers;
        return this;
    }

    public GradientConfiguration setNumFingers(int numFingers) {
        this.numFingers = numFingers;
        return this;
    }

    public GradientConfiguration setTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }
}
