/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.hp.HPMechanism;
import se.sics.gvod.common.hp.HolePunching;
import se.sics.gvod.common.hp.HpFeasability;
import se.sics.gvod.net.Nat.BindingTimeoutCategory;

/**
 *
 * @author jdowling
 */
public class VodAddress implements Serializable, Comparable {
    public static enum PartitioningType {
        NEVER_BEFORE, ONCE_BEFORE, MANY_BEFORE
    }

    private static final int DEFAULT_DELTA_PC = 1;
    private static final long serialVersionUID = -7968846333L;
    protected final Address addr;
    protected int overlayId;
//    This could be used by Gradient for searching - add up to 256
    // different partitions for the search namespace.
//    protected final byte extension;

    public static enum NatType {

        OPEN,
        NAT;
    };
    // NatType(1 bit), MappingPolicy(2 bits), AllocationPolicy(2 bits), 
    // FilteringPolicy(2 bits), UdpBindingTimeout(1 bit)
    // natPolicy is an unsigned byte stored as a short in java, but
    // sent as a byte over the network and stored in SQL as an unsigned byte.
    private final short natPolicy;
    /**
     * A natted node's public relay nodes used to connect to this addr.
     */
    protected final Set<Address> parents;
    /**
     * Nat's delta value for PC prediction not serialized and sent over network
     */
    private final transient int delta;
    private transient Transport transport = Transport.UDP;

    /**
     * Only public nodes use this constructor. Constructor that doesn't supply
     * any parents, as it assumes the node is OPEN.
     *
     * @param peerAddress
     */
    public VodAddress(Address peerAddress, int overlayId) {
        this(peerAddress, overlayId,
                NatType.OPEN,
                Nat.MappingPolicy.OPEN, Nat.AllocationPolicy.OPEN,
                Nat.FilteringPolicy.OPEN,
                BindingTimeoutCategory.LOW.getBindingTimeout(),
                DEFAULT_DELTA_PC,
                null);
    }

    public VodAddress(Address peerAddress, int overlayId, Nat nat) {
        this(peerAddress, overlayId, nat, null);
    }

    public VodAddress(Address peerAddress, int overlayId, Nat nat, Set<Address> parents) {
        this(peerAddress, overlayId,
                (nat.getType() == Nat.Type.OPEN) ? VodAddress.NatType.OPEN : VodAddress.NatType.NAT,
                nat.getMappingPolicy(),
                nat.getAllocationPolicy(),
                nat.getFilteringPolicy(),
                nat.getBindingTimeout(),
                nat.getDelta(),
                parents);
    }

    /**
     * Private nodes use this constructor.
     *
     * @param address
     * @param natPolicy
     * @param parents
     */
    public VodAddress(Address address, int overlayId,
            short natPolicy, Set<Address> parents) {
        if (address == null) {
            throw new NullPointerException("Address was null when creating VodAddress");
        }
        this.addr = address;
        this.overlayId = overlayId;
        this.natPolicy = natPolicy;
        if (parents == null) {
            this.parents = new HashSet<Address>();
        } else {
            this.parents = new HashSet<Address>();
            this.parents.addAll(parents);
        }
        this.delta = DEFAULT_DELTA_PC;
    }

    /**
     *
     * @param peerAddress
     * @param natType
     * @param mp
     * @param ap
     * @param fp
     * @param parents
     */
    public VodAddress(Address peerAddress, int overlayId,
            NatType natType, Nat.MappingPolicy mp, Nat.AllocationPolicy ap,
            Nat.FilteringPolicy fp, long bindingTimeout, int delta,
            Set<Address> parents) {
        this.addr = peerAddress;
        this.overlayId = overlayId;
        this.natPolicy = encodeNatType(natType, mp, ap, fp, bindingTimeout);
        if (parents == null) {
            this.parents = new HashSet<Address>();
        } else {
            this.parents = new HashSet<Address>();
            this.parents.addAll(parents);
        }
        if (delta < 0 || delta > 10) {
            throw new IllegalArgumentException("Delta must be between 0 and 10");
        }
        this.delta = delta;
    }

    public int getDelta() {
        return delta;
    }

    public int getOverlayId() {
        return overlayId;
    }

    /**
     * Used by RTT Singleton
     *
     * @return a node-specific version of this VodAddress
     */
    public VodAddress getNodeAddress() {
        return new VodAddress(this.getPeerAddress(), 0 /*
                 * SYSTEM_OVERLAY_ID
                 */,
                natPolicy, null);
    }

    private static short encodeNatType(NatType natType,
            Nat.MappingPolicy mp, Nat.AllocationPolicy ap,
            Nat.FilteringPolicy fp,
            long bindingTimeout) {
        int ntV = natType.ordinal();
        int mpV = mp.ordinal();
        int apV = ap.ordinal();
        int fpV = fp.ordinal();
        int bt = (bindingTimeout > BindingTimeoutCategory.HIGH.getBindingTimeout())
                ? 1 : 0;

        int m = (mpV << 5) & 0x60;
        int a = (apV << 3) & 0x1a;
        int f = (fpV << 1) & 0x6;


        int res = (ntV << 7)
                | ((mpV << 5) & 0x60)
                | ((apV << 3) & 0x1a)
                | ((fpV << 1) & 0x6)
                | (bt & 0x1);

        return (short) res;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return this.addr.getId();
    }

    /**
     *
     * @return
     */
    public InetAddress getIp() {
        return this.addr.getIp();
    }

    /**
     *
     * @return
     */
    public int getPort() {
        return this.addr.getPort();
    }

    /**
     *
     * @return
     */
    public NatType getNatType() {
        int ordinal = (natPolicy >>> 7) & 0x1;
        if (ordinal < 0 || ordinal > NatType.values().length) {
            throw new IllegalStateException("Invalid NatType encoded with ordinal " + ordinal);
        }
        return NatType.values()[ordinal];
    }

    public String getNatAsString() {
        // NatType:Mapping:Allocation:Filtering:AltAllocation
        // O:EI:R:EI:C
        StringBuilder str = new StringBuilder();
        str.append(getNatType().toString()).append(":").append(getMappingPolicy().toString()).append(":").append(getAllocationPolicy().toString()).append(":").append(getFilteringPolicy().toString()).append(":").append(getNatBindingTimeout());
        return str.toString();
    }

//    public static int parseNat(String nat) {
//        String natType = nat.substring(0,1);
//        NatType nt = NatType.valueOf(natType);
//        String mappingPolicy = nat.substring(1,3);
//        MappingPolicy mp = MappingPolicy.valueOf(mappingPolicy);
//        String allocationPolicy = nat.substring(3,5);
//        AllocationPolicy ap = AllocationPolicy.valueOf(allocationPolicy);
//        String filteringPolicy = nat.substring(5,7);
//        FilteringPolicy fp = FilteringPolicy.valueOf(filteringPolicy);
//        String altAllocationPolicy = nat.substring(7,8);
//        AlternativePortAllocationPolicy altAp = 
//                AlternativePortAllocationPolicy.valueOf(altAllocationPolicy);
//        return encodeNatType(nt, mp, ap, fp, altAp);
//    }
    /**
     *
     * @return
     */
    public Nat.MappingPolicy getMappingPolicy() {
        if (getNatType() == NatType.OPEN) {
            return Nat.MappingPolicy.OPEN;
        }
        int ordinal = (natPolicy >>> 5) & 0x3;
        if (ordinal < 0 || ordinal > Nat.MappingPolicy.values().length) {
            throw new IllegalStateException("Invalid MappingPolicy encoded with ordinal " + ordinal);
        }
        return Nat.MappingPolicy.values()[ordinal];
    }

    /**
     *
     * @return
     */
    public Nat.FilteringPolicy getFilteringPolicy() {
        if (getNatType() == NatType.OPEN) {
            return Nat.FilteringPolicy.OPEN;
        }
        int ordinal = (natPolicy >>> 1) & 0x3;
        if (ordinal < 0 || ordinal > Nat.FilteringPolicy.values().length) {
            throw new IllegalStateException("Invalid FilteringPolicy encoded with ordinal " + ordinal);
        }
        return Nat.FilteringPolicy.values()[ordinal];
    }

    /**
     *
     * @return
     */
    public Nat.AllocationPolicy getAllocationPolicy() {
        if (getNatType() == NatType.OPEN) {
            return Nat.AllocationPolicy.OPEN;
        }
        int ordinal = (natPolicy >>> 3) & 0x3;
        if (ordinal < 0 || ordinal > Nat.AllocationPolicy.values().length) {
            throw new IllegalStateException("Invalid AllocationPolicy encoded with ordinal " + ordinal);
        }
        return Nat.AllocationPolicy.values()[ordinal];
    }

    public int getNatBindingTimeout() {
        int ordinal = natPolicy & 0x1;
        if (ordinal < 0 || ordinal > Nat.BindingTimeoutCategory.values().length) {
            throw new IllegalStateException("Invalid BindingTimeoutCategory encoded with ordinal " + ordinal);
        }
        return Nat.BindingTimeoutCategory.values()[ordinal].getBindingTimeout();

    }

    /**
     *
     * @return
     */
    public Address getPeerAddress() {
        return addr;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        final int prime = 7;
        int result = 1;
        result = prime + ((addr == null) ? 0 : addr.hashCode()) + (overlayId * prime);
        return result;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        if (equals(o)) {
            return 0;
        }
        VodAddress other = (VodAddress) o;
        if (getId() > other.getId()) {
            return 1;
        } else if (getId() == other.getId()) {
            if (getOverlayId() > other.getOverlayId()) {
                return 1;
            }
        }
        return -1;
    }

    /**
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
        VodAddress other = (VodAddress) obj;
        if (addr == null) {
            if (other.addr != null) {
                return false;
            }
        } else if (other.addr == null) {
            return false;
        }
        if (addr.getId() != other.addr.getId()) {
            return false;
        }
        if (overlayId != other.overlayId) {
            return false;
        }

        return true;
    }

    /**
     * returns the nat policy encoded as an int.
     *
     * @return
     */
    public short getNatPolicy() {
        return natPolicy;
    }

    /**
     *
     * @return
     */
    public List<Address> getParentsAsList() {
        List<Address> retParents = new ArrayList<Address>();
        retParents.addAll(parents);
        return retParents;
    }

    public Set<Address> getParents() {
        return parents;
    }

    public boolean hasParents() {
        return !(parents == null || parents.isEmpty());
    }

    public boolean isContactable() {
        return !(hasParents() && isOpen());
    }

    /**
     *
     * @param addr
     * @return
     */
    public boolean addParent(Address addr) {
        return parents.add(addr);
    }

    public boolean replaceParents(Set<Address> parents) {
        if (parents != null && !parents.isEmpty()) {
            this.parents.clear();
            this.parents.addAll(parents);
        } else {
            return false;
        }
        return true;
    }

    /**
     *
     * @param addr
     * @return
     */
    public boolean removeParent(Address addr) {
        return parents.remove(addr);
    }

    @Override
    public String toString() {
        return (getPeerAddress() == null ? "" : getPeerAddress())
                + ":" + overlayId + "(" + getNatType() + ")[" + getNatAsString() + "]";
    }

    public boolean isOpen() {
        return (getNatType() == NatType.OPEN);
    }

    public Nat getNat() {
        Nat.Type nt = (getNatType() == NatType.OPEN)
                ? Nat.Type.OPEN : Nat.Type.NAT;
        return new Nat(nt,
                getMappingPolicy(),
                getAllocationPolicy(),
                getFilteringPolicy(),
                delta,
                getNatBindingTimeout());
    }

    public boolean isHpPossible(VodAddress src) {
        HolePunching hp = HpFeasability.isPossible(this, src);
        return (hp == null) ? false : true;
    }

    public HPMechanism getHpMechanism(VodAddress dest) {
        HolePunching hp = HpFeasability.isPossible(this, dest);
        if (hp == null) {
            return HPMechanism.NONE;
        }
        return hp.getHolePunchingMechanism();
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

//
//    public int getCategoryId() {
//        int val = overlayId & 0xFFFF0000;
//        return val >>> 16;
//    }

//    public int getPartitionIdLength() {
//        return overlayId & 0x0000FFFF;
//    }

    public void setOverlayId(int overlayId) {
        this.overlayId = overlayId;
    }

    //
    // partitioningType - 2 bits
    // partitionIdDepth - 4 bits
    // partitionId - 10 bits
    // categoryId - 16 bits
    //

    public int getCategoryId() {
        return overlayId & 65535;
    }

//    public int getPartitionIdLength() {
//        return overlayId & 0x0000FFFF;
//    }

    public int getPartitionId() {
        return (overlayId & 67043328) >>> 16;
    }

    public int getPartitionIdDepth() {
        return (overlayId & 1006632960) >>> 26;
    }

    public PartitioningType getPartitioningType() {
        return PartitioningType.values()[(overlayId & -1073741824) >>> 30];
    }

    public static int encodePartitionDataAndCategoryIdAsInt(PartitioningType partitioningType, int partitionIdDepth,
                                                     int partitionId, int categoryId) {
        if(partitionIdDepth > 15 || partitionIdDepth < 1)
            throw new IllegalArgumentException("partitionIdDepth must be between 1 and 15");
        if(partitionId > 1023 || partitionId < 0)
            throw new IllegalArgumentException("partitionId must be between 0 and 1023");
        if(categoryId > 65535 || categoryId < 0)
            throw new IllegalArgumentException("categoryId must be between 0 and 65535");

        int result = partitioningType << 30;
        result = result | (partitionIdDepth << 21);
        result = result | (partitionId << 12);
        result = result | categoryId;


        return result;
    }
}
