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

import java.lang.Integer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.net.RttStats;
import se.sics.gvod.net.VodAddress;

/**
 * Stores the distribution of RTT samples to different hosts, calculates average, variance 
 * and standard variation of samples, and periodically runs decay process which throws 
 * the old samples away. 
 * 
 */
public class RTTStore {

    protected static final int DECAY_PERIOD = 10000;
    protected static final int SAMPLE_VALIDITY_PERIOD = 10000;
    protected static final int MINIMUM_SAMPLES_TO_KEEP = 5;
    private static ConcurrentHashMap<Integer, ConcurrentHashMap<Address, RTT>> publicRtts = new ConcurrentHashMap<Integer, ConcurrentHashMap<Address, RTT>>();

    static {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new DecayTask(), DECAY_PERIOD, DECAY_PERIOD);
    }

    private static final class DecayTask extends TimerTask {

        @Override
        public void run() {
            for (int i : publicRtts.keySet()) {
                for (RTT rtt : publicRtts.get(i).values()) {
                    // JIM - Are RTT objects updated anywhere else? If so....
                    // potential for ConcurrentAccessModification Exception here!!
                    rtt.decay();
                }
            }
        }
    }

    public static final class Sample {

        public static enum Order implements Comparator<Sample> {

            ByTime() {

                @Override
                public int compare(Sample s1, Sample s2) {
                    return s1.getTime().compareTo(s2.getTime());
                }
            },
            ByValue() {

                @Override
                public int compare(Sample s1, Sample s2) {
                    return s1.getValue().compareTo(s2.getValue());
                }
            };
        }
        private VodAddress address;
        private Long time;
        private Long value;

        public Sample(VodAddress address, Long time, Long value) {
            this.address = address;
            this.time = time;
            this.value = value;
        }

        public VodAddress getAddress() {
            return address;
        }

        public Long getTime() {
            return time;
        }

        public Long getValue() {
            return value;
        }
    }

    /**
     * TODO: Need a max RTO - upper bound on the RTO?
     */
    public static final class RTT {

        private static Logger log = LoggerFactory.getLogger(RTT.class);

        public static enum Order implements Comparator<RTT> {

            ByRto() {
                @Override
                public int compare(RTT rtt1, RTT rtt2) {
                    if (rtt1.getRTO() == rtt2.getRTO()) {
                        return 0;
                    } else if (rtt1.getRTO() < rtt2.getRTO()) {
                        return -1;
                    }
                    return 1;
                }
            },

        }
        private final VodAddress address;
        private volatile double avgRTT;
        private volatile double varRTT;
        private volatile double RTO;
        private volatile double showedRTO;
        private final double alpha = 0.125;
        private final double beta = 0.25;
        private final long K = 4;
        private final long minRTO;
        private volatile long lastContacted;

        public RTT(VodAddress address, long minRTO) {
            if (address == null) {
                throw new NullPointerException("Address can't be null in RTT constructor");
            }
            if (minRTO < 0) {
                throw new IllegalArgumentException("minRTO must be a positive number");
            }
            this.address = address;
            this.minRTO = minRTO;
            avgRTT = 0.0;
            varRTT = 0.0;
            RTO = -1.0;
            showedRTO = 0.0;
            lastContacted = System.currentTimeMillis();
        }

        /**
         * Updates the average RTO, we use a TCP-style calculation of the RTO
         * 
         * @param rtt
         *            The RTT of the packet
         */
        public void addSample(long rtt) {
            addSampleWithoutTimestamp(rtt);

            // log.debug("RTO before check if between max and min value " + RTO);
            if (this.RTO < minRTO) {
                this.showedRTO = minRTO;
            } else {
                this.showedRTO = RTO;
            }
            lastContacted = System.currentTimeMillis();
        }

        private void addSampleWithoutTimestamp(long rtt) {
            if (this.RTO == -1) {
                // Set RTO to RTT if it's the first time it's updated
                // this.count = 1;

                /*
                 * SRTT <- R, RTTVAR <- R/2, RTO <- SRTT + max (G, KRTTVAR)
                 */
                this.avgRTT = rtt;
                this.varRTT = rtt / 2.0;
                this.RTO = avgRTT + K * varRTT;

                log.trace("Initial RTO " + RTO);
            } else {

                // log.debug("Changing RTO " + RTO);
                // log.debug("VAR " + varRTT);
                // log.debug("AVG " + avgRTT);
                // log.debug("Beta "+beta);
                // log.debug("Alpha "+alpha);

                // RTTVAR <- (1 - beta) * RTTVAR + beta * |SRTT - R'|
                this.varRTT = (1 - beta) * varRTT + beta * Math.abs((avgRTT - rtt));
                // log.debug("Variance " + varRTT);
                // SRTT <- (1 - alpha) * SRTT + alpha * R'
                this.avgRTT = (1 - alpha) * avgRTT + alpha * rtt;
                // log.debug("Average " + avgRTT);
                // RTO = AVG + K x VAR;
                this.RTO = avgRTT + K * varRTT;

                // log.debug("Result RTO " + RTO);

                // // AVG = (((AVG * CNT) + RTT) / (CNT + 1));
                // this.avgRTT = (((avgRTT * count) + RTT) / (count + 1));

                // log.debug("Average RTT " + avgRTT);
                //
                // // DIFF = (AVG - RTT)^2;
                // this.diff = pow((avgRTT - RTT), 2);
                //
                // log.debug(" DIFF " + diff);
                //			
                // log.debug("Var RTT before "+ varRTT);
                //
                // // VAR = (((VAR * CNT) + DIFF) / (CNT + 1)); // variance of RTT
                // this.varRTT = (((varRTT * count) + diff) / (count + 1));
                //
                // log.debug("Variance " + varRTT);
                // // CNT++;
                // this.count++;
                //
                // // RTO = AVG + 4 x VAR;
                // this.RTO = avgRTT + K * varRTT;

            }
        }

        public long getRTO() {
            long r = (showedRTO < minRTO ? (long) minRTO : (long) showedRTO);

            return r;
        }

        public RttStats getRttStats() {
            return new RttStats(avgRTT, varRTT, RTO, showedRTO, minRTO, lastContacted, address);
        }

        public VodAddress getAddress() {
            return address;
        }

        public void decay() {
            if (SAMPLE_VALIDITY_PERIOD < System.currentTimeMillis() - lastContacted) {
                // TODO: is this a reasonable model for decaying rtts? 
                long rto = getRTO();
                if (this.RTO != -1) {
                    rto *= 0.01;
                    addSampleWithoutTimestamp(rto);
                }

            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RTT other = (RTT) obj;
            if (this.address != other.address && (this.address == null || !this.address.equals(other.address))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.address != null ? this.address.hashCode() : 0);
            return hash;
        }
        
        
    }

    /**
     * This method stores the rttValue for address at nodeId.
     * nodeId is required for simuation mode, as RTTStore is a singleton.
     * @param nodeId the id of the node that recorded the rtt sample (often, self.getId())
     * @param address the address of the node for which the rtt sample has been made
     * @param rttValue the round-trip latency measured
     * @return a RTT object representing the set of rtt samples for this addr at nodeId.
     */
    public static RTT addSample(int nodeId, VodAddress address, long rttValue) {
        if (address.isOpen()) {
            RTT rtt;
            Address peerAddress = address.getPeerAddress();
            if (!publicRtts.containsKey(nodeId)) {
                ConcurrentHashMap<Address, RTT> m = new ConcurrentHashMap<Address, RTT>();
                publicRtts.put(nodeId, m);
            }
            ConcurrentHashMap<Address, RTT> m = publicRtts.get(nodeId);

            if (!m.containsKey(peerAddress)) {
                rtt = new RTT(address, 10 /*minRto*/);
                m.put(peerAddress, rtt);
                rtt.addSample(rttValue);
            } else {
                rtt = m.get(peerAddress);
                rtt.addSample(rttValue);
            }
            return rtt;
        } else {
            return null;
        }
    }

    /**
     * Removes failedNode's samples from nodeId
     * @param nodeId the id of the node storing the samples
     * @param failedNode the address of the node that we want to remove samples for.
     */
    public static void removeSamples(int nodeId, VodAddress failedNode) {

        ConcurrentHashMap<Address, RTT> m = publicRtts.get(nodeId);
        if (m != null) {
            m.remove(failedNode.getPeerAddress());
        }
    }

    /**
     * Get a RTT object representing samples for address at nodeId.
     * 
     * @param nodeId id of node storing the samples (self.getId())
     * @param address address of node for which there are RTT samples
     * @return 
     */
    public static RTT getRtt(int nodeId, VodAddress address) {
        if (!publicRtts.containsKey(nodeId)) {
            return null;
        }

        ConcurrentHashMap<Address, RTT> m = publicRtts.get(nodeId);

        Address peerAddress = address.getPeerAddress();
        if (!m.containsKey(peerAddress)) {
            return null;
        } else {
            return m.get(peerAddress);
        }
    }

    public static List<RTT> getOnAvgBest(int nodeId, int numNodes) {
        return getOnAvgBest(nodeId, numNodes, new HashSet<Address>());
    }
    /**
     * TODO: Could re-implement this as a randomized alg: return a random set
     * of RTTs biased towards better RTTS instead of having to store state about
     * ignoreSet of rejected parents.
     * @param nodeId
     * @param numNodes
     * @param ignoreSet
     * @return 
     */
    public static List<RTT> getOnAvgBest(int nodeId, int numNodes,
            Set<Address> ignoreSet) {
        ConcurrentHashMap<Address, RTT> m = publicRtts.get(nodeId);
        if (m == null) {
                m = new ConcurrentHashMap<Address, RTT>();
                publicRtts.put(nodeId, m);
        }
        
        List<RTT> rtts = new ArrayList<RTT>(m.values());
        List<RTT> remove = new ArrayList<RTT>();
        for (RTT r : rtts) {
            if (ignoreSet.contains(r.getAddress().getPeerAddress())) {
                remove.add(r);
            }
        }
        rtts.removeAll(remove);

        Collections.sort(rtts, RTT.Order.ByRto);
        if (rtts.size() < numNodes) {
            numNodes = rtts.size();
        }
        if (numNodes > 0) {
            rtts = rtts.subList(0, numNodes);
        }

        return rtts;
    }

    public static List<RTT> getAllOnAvgBetterRtts(int nodeId, long value, long rttGreaterThanTolerance) {
        return RTTStore.getOnAvgBetterRtts(nodeId, publicRtts.size(), value, rttGreaterThanTolerance);
    }

    public static List<RTT> getOnAvgBetterRtts(int nodeId, int numNodes, 
            long value, long tolerance) {
        ConcurrentHashMap<Address, RTT> m = publicRtts.get(nodeId);
        if (m == null) {
                m = new ConcurrentHashMap<Address, RTT>();
                publicRtts.put(nodeId, m);
        }
        List<RTT> rtts = new ArrayList<RTT>(m.values());

        while (!rtts.isEmpty()) {
            RTT max = (RTT) Collections.max(rtts, RTT.Order.ByRto);
            if (max.getRTO() >= value - tolerance) {
                rtts.remove(max);
            } else {
                break;
            }
        }

        if (numNodes > 0 && rtts.size() > numNodes) {
            Collections.sort(rtts, RTT.Order.ByRto);
            rtts = rtts.subList(0, numNodes - 1);
        }

        return rtts;
    }

    //This is for the test case reason
    protected static void cleanup() {
        publicRtts.clear();
    }
}
