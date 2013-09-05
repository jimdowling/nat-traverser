package se.sics.gvod.common;

import java.io.Serializable;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.PartitioningType;
import se.sics.gvod.net.VodAddress;

public class VodDescriptor implements Comparable<VodDescriptor>, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(VodDescriptor.class);
    private static final long serialVersionUID = 1906679375438244117L;
    private final VodAddress vodAddress;
    /**
     * age is 2 bytes in length: 32767
     */
    private int age;
    /**
     * serialize utility
     */
    private Utility utility;
    /**
     * 2 bytes
     */
    /**
     * don't serialize
     */
    private transient int refs;
    /**
     * 2 bytes in length
     */
    private int uploadRate;
    private long numberOfIndexEntries;
    /**
     * Don't serialize. Sender side of data request. Determines number of
     * outstanding requests that can be in-flight.
     */
    private transient final LinkedList<Block> requestPipeline;
    /**
     * Don't serialize. Receiver side of data request. Measures the latency of
     * sending data responses, and adapts its communication window size to
     * utilize as much of the connection b/w as possible.
     */
    private transient final CommunicationWindow window;
    /**
     * Don't serialize. 2 bytes
     */
    private transient int pipeSize;
    /**
     * Don't serialize. 2 bytes
     */
    private transient final int mtu;
    /**
     * Don't serialize.
     */
    private transient boolean connected;

    public VodDescriptor(VodAddress vodAddress, Utility utility,
            int uploadRate,
            LinkedList<Block> requestPipeline,
            CommunicationWindow window,
            int pipeSize, int mtu) {
        this(vodAddress, 0,
                utility, 0, uploadRate, requestPipeline,
                window, pipeSize, mtu);
    }

    public VodDescriptor(VodAddress vodAddress, Utility utility,
            /*int bitfieldSize*/ int windowSize, int pipeSize, int maxWindowSize,
            int mtu) {
        this(vodAddress, 0,
                utility, 0,
                0,
                new LinkedList<Block>(),
                new CommunicationWindow(windowSize, maxWindowSize),
                pipeSize, mtu);
    }

    public VodDescriptor(VodAddress vodAddress, int age,
            Utility utility, int refs, int uploadRate,
            LinkedList<Block> requestPipeline,
            CommunicationWindow window, int pipeSize, int mtu) {
        assert (vodAddress != null);
//        assert (utility != null);
        this.vodAddress = vodAddress;
        this.age = age;
        this.utility = utility;
        this.refs = refs;
        this.uploadRate = uploadRate;
        this.requestPipeline = requestPipeline;
        this.window = window;
        this.pipeSize = pipeSize;
        if (mtu < BaseCommandLineConfig.MIN_MTU) {
            this.mtu = BaseCommandLineConfig.MIN_MTU;
        } else if (mtu > BaseCommandLineConfig.DEFAULT_MTU) {
            this.mtu = BaseCommandLineConfig.DEFAULT_MTU;
        } else {
            this.mtu = mtu;
        }
    }

    public VodDescriptor(VodAddress vodAddress) {
        this(vodAddress, 0,
                new UtilityVod(0), 0, 0, new LinkedList<Block>(),
                new CommunicationWindow(VodConfig.LB_WINDOW_SIZE,
                VodConfig.LB_MAX_WINDOW_SIZE),
                VodConfig.LB_DEFAULT_PIPELINE_SIZE, VodConfig.DEFAULT_MTU);
    }

    public VodDescriptor(VodAddress vodAddress, long numberOfIndexEntries) {
        this(vodAddress, new UtilityVod(0), 0, 0, numberOfIndexEntries);
    }

    public VodDescriptor(VodAddress vodAddress, Utility utility, int age, int mtu) {
        this(vodAddress, age,
                utility, 0, 0, new LinkedList<Block>(),
                new CommunicationWindow(VodConfig.LB_WINDOW_SIZE,
                VodConfig.LB_MAX_WINDOW_SIZE),
                VodConfig.LB_DEFAULT_PIPELINE_SIZE, mtu);
    }

    public VodDescriptor(VodAddress vodAddress, Utility utility, int age, int mtu,
            long numberOfIndexEntries) {
        this(vodAddress, age,
                utility, 0, 0, new LinkedList<Block>(),
                new CommunicationWindow(VodConfig.LB_WINDOW_SIZE,
                VodConfig.LB_MAX_WINDOW_SIZE),
                VodConfig.LB_DEFAULT_PIPELINE_SIZE, mtu);
        this.numberOfIndexEntries = numberOfIndexEntries;

    }

    public VodDescriptor(VodDescriptor descriptor, int age) {
        this(descriptor.vodAddress, age,
                descriptor.getUtility(), descriptor.refs, descriptor.uploadRate,
                descriptor.requestPipeline,
                descriptor.window, descriptor.pipeSize, descriptor.mtu);
    }

    public VodDescriptor(VodDescriptor descriptor, VodAddress newAddr) {
        this(newAddr, descriptor.age,
                descriptor.getUtility(), descriptor.refs, descriptor.uploadRate,
                descriptor.requestPipeline,
                descriptor.window, descriptor.pipeSize, descriptor.mtu);
    }

    public VodDescriptor(VodDescriptor descriptor, Utility utility, int piece) {
        this(descriptor.vodAddress, descriptor.age,
                utility,
                descriptor.refs, descriptor.uploadRate,
                descriptor.requestPipeline,
                descriptor.window, descriptor.pipeSize, descriptor.mtu);
    }

    public VodDescriptor clone(int newOverlayId) {
        VodAddress o = new VodAddress(this.vodAddress.getPeerAddress(), newOverlayId,
                this.vodAddress.getNat(), this.vodAddress.getParents());
        return new VodDescriptor(o, age, utility, refs, uploadRate,
                requestPipeline, window, pipeSize, mtu);
    }

    public int getMtu() {
        return mtu;
    }

    public int getPipeSize() {
        return pipeSize;
    }

    public CommunicationWindow getWindow() {
        return window;
    }
    private static long counter = 0;

    public void setPipeSize(int pipeSize) {
        if (pipeSize > VodConfig.LB_MAX_PIPELINE_SIZE) {
            pipeSize = VodConfig.LB_MAX_PIPELINE_SIZE;
        }
        this.pipeSize = (pipeSize < VodConfig.LB_DEFAULT_PIPELINE_SIZE)
                ? VodConfig.LB_DEFAULT_PIPELINE_SIZE : pipeSize;
        if (counter++ % 1000 == 0) {
            logger.info("Pipesize is : " + this.pipeSize);
        }
        if (counter == Long.MAX_VALUE) {
            counter = 0;
        }
    }

    public LinkedList<Block> getRequestPipeline() {
        return requestPipeline;
    }

    public void discardPiece(int piece) {

        // TODO - can we have a Map indexed by piece number, pointing to Blocks?
        LinkedList<Block> toRemove = new LinkedList<Block>();
        for (Block block : requestPipeline) {
            if (block.getPieceIndex() == piece) {
                toRemove.add(block);
            }
        }
        for (Block block : toRemove) {
            requestPipeline.remove(block);
        }
    }

    public LinkedList<Block> cleanupPipeline() {

        long currentTime = System.currentTimeMillis();
        LinkedList<Block> toRemove = new LinkedList<Block>();
        for (Block b : requestPipeline) {
            if (currentTime - b.getTimeRequested() > VodConfig.DATA_REQUEST_TIMEOUT) {
                toRemove.add(b);
            }
        }
        for (Block block : toRemove) {
            requestPipeline.remove(block);
        }
        return toRemove;
    }

    public void clearPipeline() {
        requestPipeline.clear();
    }

    public int incrementAndGetAge() {
        return ++age;
    }

    public int getAge() {
        return age;
    }

    public void resetAge() {
        age = 0;
    }

    public VodAddress getVodAddress() {
        return vodAddress;
    }

    public Utility getUtility() {
        return utility;
    }

    public void setUtility(Utility utility) {
        this.utility = utility;
    }

    public int getRefs() {
        return refs;
    }

    public void setRefs(int ref) {
        this.refs = ref;
    }

    public void addRef() {
        refs++;
        if (refs > 1) {
            System.err.println("BUG IN REFERENCE COUNTING GVodNodeDescriptors");
            int test = getRefs();
        }
    }

    public void supRef() {
        refs--;
    }

    public int getUploadRate() {
        return uploadRate;
    }

    public void setUploadRate(int UploadRate) {
        this.uploadRate = UploadRate;
    }

    @Override
    public int compareTo(VodDescriptor that) {
        if (this.age > that.age) {
            return 1;
        }
        if (this.age < that.age) {
            return -1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 87;
        int result = 1;
        result = prime * result + ((vodAddress == null) ? 0 : vodAddress.hashCode());
        return result;
    }

    /**
     * Two ViewEntries are equivalent if their VodAddresses are the same.
     *
     * @param obj
     * @return
     */
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
        VodDescriptor other = (VodDescriptor) obj;
        if (vodAddress == null) {
            if (other.vodAddress != null) {
                return false;
            }
        } else if (other.vodAddress == null) {
            return false;
        }
        if (!vodAddress.equals(other.getVodAddress())) {
            return false;
        }

        return true;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getWindowSize() {
        return window.getSize();
    }

    public long getBaseDelay() {
        return window.baseDelay();
    }

    public long getCurrentDelay() {
        return window.currentDelay();
    }

    public long getNumberOfIndexEntries() {
        return numberOfIndexEntries;
    }

    public int getId() {
        return vodAddress.getId();
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

//    public void setPartitionsNumber(int partitionsNumber) {
//        this.partitionsNumber = partitionsNumber;
//    }
//    public LinkedList<Boolean> getPartitionId() {
//        return partitionId;
//    }
//    public void setPartitionId(LinkedList<Boolean> partitionId) {
//        this.partitionId = partitionId;
//    }
//
//    public int getPartitionsNumber() {
//        return partitionsNumber;
//    }
    @Override
    public String toString() {
        return vodAddress.toString() + ":u(" + utility + ")";
    }
}