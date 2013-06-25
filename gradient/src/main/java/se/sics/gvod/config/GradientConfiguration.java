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
    int similarSetSize;
    long setsExchangePeriod;
    long setsExchangeTimeout;
    // Timeout for search msgs up the Gradient
    long searchRequestTimeout;
    int utilityThreshold;
    // Number of parallel probes to send
    int numParallelSearches; // 5
    //Time-To-Live used for Gradient search messages.
    int searchTtl; //5
    //Number of best similar peers that should be sent in each sets exchange response.
    int numBestSimilarPeers; //10
    int numFingers; 
    double temperature; 

    /** 
     * Default constructor comes first.
     */
    public GradientConfiguration() {
        this(VodConfig.getSeed(),
                VodConfig.SIMILAR_SET_SIZE,
                VodConfig.SETS_EXCHANGE_PERIOD,
                VodConfig.SETS_EXCHANGE_TIMEOUT,
                VodConfig.PROBE_REQUEST_TIMEOUT,
                VodConfig.UTILITY_THRESHOLD,
                VodConfig.NUM_OF_PROBES,
                VodConfig.PROBE_TTL,
                VodConfig.NUM_BEST_SIMILAR_PEERS,
                500 /* default numChunks */,
                0.9d /*temperature*/
                );
    }

    /** 
     * Full argument constructor comes second.
     */
    public GradientConfiguration(
            int seed, 
            int similarSetSize, 
            long setsExchangePeriod,
            long setsExchangeTimeout, 
            long searchRequestTimeout,
            int utilityThreshold,
            int numParallelSearches,
            int searchTtl,
            int numberOfBestSimilarPeers,
            int numFingers,
            double temperature
            ) {
        super(seed);
        this.similarSetSize = similarSetSize;
        this.setsExchangeTimeout = setsExchangeTimeout;
        this.setsExchangePeriod = setsExchangePeriod;
        this.searchRequestTimeout = searchRequestTimeout;
        this.utilityThreshold = utilityThreshold;
        this.numParallelSearches = numParallelSearches;
        this.searchTtl = searchTtl;
        this.numBestSimilarPeers = numberOfBestSimilarPeers;
        this.numFingers = numFingers;
        this.temperature = temperature;
    }

    public static GradientConfiguration build() {
        return new GradientConfiguration();
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public int getSimilarSetSize() {
        return similarSetSize;
    }

    public int getSearchTtl() {
        return searchTtl;
    }

    public long getSetsExchangeTimeout() {
        return setsExchangeTimeout;
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
    public long getSetsExchangeDelay() {
        return setsExchangeTimeout;
    }

    /**
     * @return the setsExchangePeriod
     */
    public long getSetsExchangePeriod() {
        return setsExchangePeriod;
    }

    /**
     * @return the probeRequestTimeout
     */
    public long getSearchRequestTimeout() {
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
        return numBestSimilarPeers;
    }

    public GradientConfiguration setSimilarSetSize(int similarSetSize) {
        this.similarSetSize = similarSetSize;
        return this;
    }

    public GradientConfiguration setSetsExchangePeriod(long setsExchangePeriod) {
        this.setsExchangePeriod = setsExchangePeriod;
        return this;
    }

    public GradientConfiguration setSetsExchangeTimeout(long setsExchangeTimeout) {
        this.setsExchangeTimeout = setsExchangeTimeout;
        return this;
    }

    public GradientConfiguration setSearchRequestTimeout(long searchRequestTimeout) {
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
        this.numBestSimilarPeers = numBestSimilarPeers;
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
