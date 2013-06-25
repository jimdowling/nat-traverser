/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * CommunicationWindow is on the uploader side of a connection.
 * Pipeline is on the downloader side of a connection.
 * Pipeline is measured in #requests.
 * CommunicationWindow is measured in #bytes.
 *
 * target :           delay in ms that we would like as latency for uploading
 * currentDelaySize : to avoid noise in last N (5) measurements of delay,
 *                    we take the min latency of the last N measurements as the currentDelay.
 * GVodConfig.GAIN :             used to recalculate the size of the CommunicationWindow
 * delayBoundary:     1. Uploader sends data with his clock.
 *                    2. Downloader measures skew between his local clock and uploader's clock.
 *                       This skew value also includes the latency between them.
 *                    3. Send skew value back to uploader.
 *                    4. Choose the lowest skew received within the last "delayBoundary" in ms,
 *                       as the reference clock skew.
 *                       It is also known as the 'base delay'.
 *
 * Ref : http://bittorrent.org/beps/bep_0029.html
 *       http://tools.ietf.org/html/draft-shalunov-ledbat-congestion-00
 * 
 * @author gautier, jim
 */
public class CommunicationWindow implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationWindow.class);    
    
    private int maxWindowSize;
    private int size;
    private int flightSize;
    private List<TimeoutId> outstandingMessages;
    private LinkedList<TimeDelay> baseDelaysMins = new LinkedList<TimeDelay>();
    private LinkedList<Long> currentDelays;
    private int sizeCurrentDelays = VodConfig.CURRENT_FILTER;
    private final long target = VodConfig.DEFAULT_LEDBAT_TARGET_DELAY; // 100
    private final int numDelayIntervals = VodConfig.DEFAULT_DELAY_BOUNDARY / (60 * 1000);
    private long baseTime = 0;
    private long lastPacketLossTime = 0;

    private class TimeDelay {

        private final long time;
        private final long delay;

        public TimeDelay(long time, long delay) {
            this.time = time;
            this.delay = delay;
        }

        public long getDelay() {
            return delay;
        }

        public long getTime() {
            return time;
        }
    }

    public CommunicationWindow(int size, int maxSize) {
        outstandingMessages = new ArrayList<TimeoutId>();
        currentDelays = new LinkedList<Long>();
        this.size = size;
        if (size < VodConfig.MIN_SIZE) {
            this.size = VodConfig.MIN_SIZE;
        }
        maxWindowSize = maxSize;
    }

    public int getSize() {
        return size;
    }

    public boolean addMessage(TimeoutId id, int messageSize) {
        if (flightSize + messageSize < this.size) {
            outstandingMessages.add(id);
            flightSize += messageSize;
            return true;
        } else {
            return true;
        }
    }

    public void removeMessage(TimeoutId id, int messageSize) {
        if (outstandingMessages.contains(id)) {
            outstandingMessages.remove(id);
            flightSize -= messageSize;
        }
    }

    private long getLowestBaseDelay() {
        long lowestDelay = Long.MAX_VALUE;
        for (TimeDelay td : baseDelaysMins) {
            if (td.getDelay() < lowestDelay) {
                lowestDelay = td.getDelay();
            }
        }
        return lowestDelay;
    }
//    public void cleanDelays(long boundary) {
//        long currentTime = System.currentTimeMillis();
//        ArrayList<Long> toRemove = new ArrayList<Long>();
//        for (long t : baseDelays.keySet()) {
//            if (currentTime - t > boundary) {
//                toRemove.add(t);
//            }
//        }
//        for (long k : toRemove) {
//            baseDelays.remove(k);
//        }
//    }
    private static long counter = 0;

    public void update(long delay) {
        updateBaseDelay(delay);
        updateCurrentDelay(delay);
        long queuingDelay = currentDelay() - baseDelay();
        double offTarget = target - queuingDelay;
        int incWinSize = (int) (VodConfig.GAIN * VodConfig.MAX_SEGMENT_SIZE * offTarget / size);

//        max_allowed_cwnd = flightsize + ALLOWED_INCREASE * MSS
        size += incWinSize;
        if (size < VodConfig.MIN_SIZE) {
            size = VodConfig.MIN_SIZE;
        } else if (maxWindowSize > 0 && size > maxWindowSize) {
            size = maxWindowSize;
        }
        if (counter++ % 1000 == 0) {
            logger.debug("Comms Win Size change: "
                    + incWinSize + " off-target = " + offTarget
                    + " size = " + size //                + " maxSize = " + maxWindowSize
                    );

            logger.info("Queing delay: {}", queuingDelay);
        }
        if (counter == Long.MAX_VALUE) {
            counter = 0;
        }
    }

    private void updateCurrentDelay(long delay) {
        currentDelays.addLast(delay);
        if (currentDelays.size() >= sizeCurrentDelays) {
            currentDelays.removeFirst();
        }
    }

    public long currentDelay() {
        if (currentDelays.isEmpty()) {
            return 0;
        }
        long result = Long.MAX_VALUE;
        for (long d : currentDelays) {
            if (d < result) {
                result = d;
            }
        }
        return result;
    }

    public void updateBaseDelay(long delay) {
        long currentTime = System.currentTimeMillis();
//        cleanDelays(delayBoundary);
//        baseDelays.put(currentTime, delay);
        if (baseTime == 0) {
            baseTime = currentTime;
        }

        if (baseDelaysMins.isEmpty()) {
            TimeDelay tdNew = new TimeDelay(currentTime, delay);
            baseDelaysMins.add(tdNew);
            baseTime = currentTime;
            return;
        }

        TimeDelay tdNewest = baseDelaysMins.getLast();
        // if new minute interval crossed, add the new delay
        long mostRecentOffsetInMins = (tdNewest.getTime() - baseTime) / VodConfig.MINUTE_IN_MS;
        long currentRecentOffsetInMins = (currentTime - baseTime) / VodConfig.MINUTE_IN_MS;
        if (currentRecentOffsetInMins - mostRecentOffsetInMins > 0) {
            TimeDelay tdNew = new TimeDelay(currentTime, delay);
            baseDelaysMins.addLast(tdNew);
            if (baseDelaysMins.size() > numDelayIntervals) {
                baseDelaysMins.removeFirst();
                baseTime += VodConfig.MINUTE_IN_MS;
            }
            checkInvariants();
        } else if (delay < tdNewest.getDelay()) {
            baseDelaysMins.removeLast();
            TimeDelay tdNew = new TimeDelay(currentTime, delay);
            baseDelaysMins.addLast(tdNew);
            checkInvariants();
        }
    }

    private void checkInvariants() {
        if (baseDelaysMins.size() > numDelayIntervals) {
            throw new IllegalStateException("Invalidated varient - too many base delays ");
        }
    }

    public long baseDelay() {
        if (baseDelaysMins.isEmpty()) {
            return 0;
        }
//        if (baseDelays.isEmpty()) {
//            return 0;
//        }
//        ArrayList<Long> delays = new ArrayList(baseDelays.values());
//        long result = delays.get(0);
//        for (long d : delays) {
//            if (d < result) {
//                result = d;
//            }
//        }
//        return result;
        return getLowestBaseDelay();
    }

    public void timedout(TimeoutId id, int s) {
        removeMessage(id, s);
        long now = System.currentTimeMillis();
        if (now - lastPacketLossTime > 1000) {
            size *= VodConfig.SCALE_DOWN_SIZE_TIMEOUT;
            logger.debug("TIMEOUT. Comms Window. Size now: " + size);
            if (size < VodConfig.MIN_SIZE) {
                size = VodConfig.MIN_SIZE;
            }
        }
        lastPacketLossTime = now;
    }

    public void setMaxWindowSize(int maxWindowSize) {
        this.maxWindowSize = maxWindowSize;
        if (size > maxWindowSize) {
            size = maxWindowSize;
        }
    }
}
