package se.sics.gvod.croupier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;

public class View {

    private final int size;
    private final Self self;
    private List<ViewEntry> entries;
    private HashMap<VodAddress, ViewEntry> d2e;
    private final Random random;
    private Comparator<ViewEntry> comparatorByAge = new Comparator<ViewEntry>() {
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

    //-------------------------------------------------------------------	
    public View(Self self, int size, long seed) {
        super();
        this.self = self;
        this.size = size;
        this.entries = new ArrayList<ViewEntry>();
        this.d2e = new HashMap<VodAddress, ViewEntry>();
        this.random = new Random(seed);
    }

//-------------------------------------------------------------------	
    public void incrementDescriptorAges() {
        for (ViewEntry entry : entries) {
            entry.getDescriptor().incrementAndGetAge();
        }
    }

//-------------------------------------------------------------------	
    public VodAddress selectPeerToShuffleWith(VodConfig.CroupierSelectionPolicy policy) {
        if (entries.isEmpty()) {
            return null;
        }
        ViewEntry oldestEntry = null;
        if (policy == VodConfig.CroupierSelectionPolicy.TAIL) {
            oldestEntry = Collections.max(entries, comparatorByAge);
        } else if (policy == VodConfig.CroupierSelectionPolicy.HEALER) {
            oldestEntry = Collections.max(entries, comparatorByAge);
        } else if (policy == VodConfig.CroupierSelectionPolicy.RANDOM) {
            oldestEntry = entries.get(random.nextInt(entries.size()));
        }
        // TODO - by not removing a reference to the node I am shuffling with, we
        // break the 'batched random walk' (Cyclon) behaviour. But it's more important
        // to keep the graph connected.
        if (entries.size() >= size) {
            removeEntry(oldestEntry);
        }
        return oldestEntry.getDescriptor().getVodAddress();
    }

//-------------------------------------------------------------------	
    public List<VodDescriptor> selectToSendAtActive(int count, VodAddress destinationPeer) {
        List<ViewEntry> randomEntries = generateRandomSample(count);
        List<VodDescriptor> descriptors = new ArrayList<VodDescriptor>();
        for (ViewEntry cacheEntry : randomEntries) {
            cacheEntry.sentTo(destinationPeer);
            descriptors.add(cacheEntry.getDescriptor());
        }
        return descriptors;
    }

//-------------------------------------------------------------------	
    public List<VodDescriptor> selectToSendAtPassive(int count, VodAddress destinationPeer) {
        List<ViewEntry> randomEntries = generateRandomSample(count);
        List<VodDescriptor> descriptors = new ArrayList<VodDescriptor>();
        for (ViewEntry cacheEntry : randomEntries) {
            cacheEntry.sentTo(destinationPeer);
            descriptors.add(cacheEntry.getDescriptor());
        }
        return descriptors;
    }

//-------------------------------------------------------------------	
    public void selectToKeep(VodAddress from, List<VodDescriptor> descriptors) {
        if (from.equals(self.getAddress())) {
            return;
        }
        LinkedList<ViewEntry> entriesSentToThisPeer = new LinkedList<ViewEntry>();
        ViewEntry fromEntry = d2e.get(from);
        if (fromEntry != null) {
            entriesSentToThisPeer.add(fromEntry);
        }

        for (ViewEntry cacheEntry : entries) {
            if (cacheEntry.wasSentTo(from)) {
                entriesSentToThisPeer.add(cacheEntry);
            }
        }

        for (VodDescriptor descriptor : descriptors) {
            if (self.getDescriptor().equals(descriptor)) {
                // do not keep descriptor of self
                continue;
            }
            if (d2e.containsKey(descriptor.getVodAddress())) {
                // we already have an entry for this peer. keep the youngest one
                ViewEntry entry = d2e.get(descriptor.getVodAddress());
                if (entry.getDescriptor().getAge() > descriptor.getAge()) {
                    // we keep the lowest age descriptor
                    removeEntry(entry);
                    addEntry(new ViewEntry(descriptor));
                    continue;
                } else {
                    continue;
                }
            }
            if (entries.size() < size) {
                // fill an empty slot
                addEntry(new ViewEntry(descriptor));
                continue;
            }
            // replace one slot out of those sent to this peer
            ViewEntry sentEntry = entriesSentToThisPeer.poll();
            if (sentEntry != null) {
                removeEntry(sentEntry);
                addEntry(new ViewEntry(descriptor));
            }
        }
    }

//-------------------------------------------------------------------	
    public final List<VodDescriptor> getAll() {
        List<VodDescriptor> descriptors = new ArrayList<VodDescriptor>();
        for (ViewEntry cacheEntry : entries) {
            descriptors.add(cacheEntry.getDescriptor());
        }
        return descriptors;
    }

//-------------------------------------------------------------------	
    public final List<VodAddress> getAllAddress() {
        List<VodAddress> all = new ArrayList<VodAddress>();
        for (ViewEntry cacheEntry : entries) {
            all.add(cacheEntry.getDescriptor().getVodAddress());
        }
        return all;
    }

//-------------------------------------------------------------------	
    public final List<VodAddress> getRandomPeers(int count) {
        List<ViewEntry> randomEntries = generateRandomSample(count);
        List<VodAddress> randomPeers = new ArrayList<VodAddress>();

        for (ViewEntry cacheEntry : randomEntries) {
            randomPeers.add(cacheEntry.getDescriptor().getVodAddress());
        }

        return randomPeers;
    }

//-------------------------------------------------------------------	
    private List<ViewEntry> generateRandomSample(int n) {
        List<ViewEntry> randomEntries;
        if (n >= entries.size()) {
            // return all entries
            randomEntries = new ArrayList<ViewEntry>(entries);
        } else {
            // return count random entries
            randomEntries = new ArrayList<ViewEntry>();
            // Don Knuth, The Art of Computer Programming, Algorithm S(3.4.2)
            int t = 0, m = 0, N = entries.size();
            while (m < n) {
                int x = random.nextInt(N - t);
                if (x < n - m) {
                    randomEntries.add(entries.get(t));
                    m += 1;
                    t += 1;
                } else {
                    t += 1;
                }
            }
        }
        return randomEntries;
    }

//-------------------------------------------------------------------	
    private void addEntry(ViewEntry entry) {

        // if the entry refers to a stun port, change it to the default port.
        if (entry.getDescriptor().getVodAddress().getPort() == VodConfig.DEFAULT_STUN_PORT ||
            entry.getDescriptor().getVodAddress().getPort() == VodConfig.DEFAULT_STUN_PORT_2) {
            entry.getDescriptor().getVodAddress().getPeerAddress().setPort(VodConfig.DEFAULT_PORT);
        }
        
        if (!entries.contains(entry)) {
            entries.add(entry);
            d2e.put(entry.getDescriptor().getVodAddress(), entry);
            checkSize();
        } else {
            // replace the entry if it already exists
            removeEntry(entry);
            addEntry(entry);
        }
    }

//-------------------------------------------------------------------	
    private boolean removeEntry(ViewEntry entry) {
        boolean res = entries.remove(entry);
        d2e.remove(entry.getDescriptor().getVodAddress());
        checkSize();
        return res;
    }

    public boolean timedOutForShuffle(VodAddress node) {
        ViewEntry entry = d2e.get(node);
        if (entry == null) {
            return false;
        }
        return removeEntry(entry);
    }

//-------------------------------------------------------------------	
    private void checkSize() {
        if (entries.size() != d2e.size()) {
            throw new RuntimeException("WHD " + entries.size() + " <> " + d2e.size());
        }
    }

//-------------------------------------------------------------------
    public void initialize(Set<VodDescriptor> insiders) {
        for (VodDescriptor peer : insiders) {
            if (!peer.getVodAddress().equals(self.getAddress())) {
                addEntry(new ViewEntry(peer));
            }
        }
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public int size() {
        return this.entries.size();
    }

    public void updateDescriptor(VodDescriptor d) {
    }
}
