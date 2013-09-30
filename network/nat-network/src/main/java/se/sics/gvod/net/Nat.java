/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import se.sics.gvod.address.Address;

/**
 *
 * @author jdowling
 */
public class Nat implements Serializable, Comparable {

    private static final long serialVersionUID = 453132554678L;

    /* For info on expected UDP Nat binding timeouts, see :
     * http://www.ietf.org/proceedings/78/slides/behave-8.pdf
     * From these slides, we measure UDP-2, but a NAT will refresh with UDP-1.
     * Therefore, we need to be conservative in setting the NAT binding timeout.
     */
    public static final int DEFAULT_RULE_EXPIRATION_TIME = 30 * 1000;
    public static final int UPPER_RULE_EXPIRATION_TIME = 90 * 1000;
    public static final String[] NAT_COMBINATIONS = {
        "NAT_EI_PC_EI", "NAT_EI_PC_HD", "NAT_EI_PC_PD",
        "NAT_EI_PP_EI_AltPC", "NAT_EI_PP_HD", "NAT_EI_PP_PD",
        "NAT_EI_RD_EI", "NAT_EI_RD_HD", "NAT_EI_RD_PD",
        "NAT_HD_PC_HD", "NAT_HD_PP_HD_AltPC", "NAT_HD_PP_HD_AltRD",
        "NAT_HD_RD_HD",
        "NAT_PD_PC_EI", "NAT_PD_PC_PD",
        "NAT_PD_RD_PD", "NAT_PD_PP_EI", "NAT_PD_PP_PD"
    };

    public static enum Type {

        OPEN("OP"), NAT("NAT"), UPNP("UPNP"), UDP_BLOCKED("UB");
        String code;

        private Type(String code) {
            this.code = code;
        }

        public static Type decode(String ap) {

            for (Type mp : Type.values()) {
                if (mp.code.equals(ap)) {
                    return mp;
                }
            }

            return null;
        }
    };

    public static enum MappingPolicy {
        // Ordering of policies is from least restrictive to most restrictive

        OPEN("OP"), ENDPOINT_INDEPENDENT("EI"),
        HOST_DEPENDENT("HD"), PORT_DEPENDENT("PD");
        String code;

        private MappingPolicy(String code) {
            this.code = code;
        }

        public static MappingPolicy decode(String ap) {

            for (MappingPolicy mp : MappingPolicy.values()) {
                if (mp.code.equals(ap)) {
                    return mp;
                }
            }

            return null;
        }
    };

    public static enum AllocationPolicy {
        // Ordering of policies is from least restrictive to most restrictive

        OPEN("OP"), PORT_PRESERVATION("PP"), PORT_CONTIGUITY("PC"), RANDOM("RD");
        String code;

        private AllocationPolicy(String code) {
            this.code = code;
        }

        public static AllocationPolicy decode(String ap) {

            for (AllocationPolicy mp : AllocationPolicy.values()) {
                if (mp.code.equals(ap)) {
                    return mp;
                }
            }

            return null;
        }
    };

    public static enum AlternativePortAllocationPolicy {
        // Ordering of policies is from least restrictive to most restrictive

        PORT_CONTIGUITY("AltPC"), RANDOM("AltRD"), OPEN("AltOP");
        String code;

        private AlternativePortAllocationPolicy(String code) {
            this.code = code;
        }

        public static AlternativePortAllocationPolicy decode(String ap) {

            for (AlternativePortAllocationPolicy mp : AlternativePortAllocationPolicy.values()) {
                if (mp.code.equals(ap)) {
                    return mp;
                }
            }

            return null;
        }
    }

    public static enum FilteringPolicy {
        // Ordering of policies is from least restrictive to most restrictive

        OPEN("OP"), ENDPOINT_INDEPENDENT("EI"), HOST_DEPENDENT("HD"), PORT_DEPENDENT("PD");
        String code;

        private FilteringPolicy(String code) {
            this.code = code;
        }

        public static FilteringPolicy decode(String ap) {

            for (FilteringPolicy mp : FilteringPolicy.values()) {
                if (mp.code.equals(ap)) {
                    return mp;
                }
            }

            return null;
        }
    };

    public static enum BindingTimeoutCategory {

        LOW(Nat.DEFAULT_RULE_EXPIRATION_TIME), HIGH(Nat.UPPER_RULE_EXPIRATION_TIME);
        int bindingTimeout;

        private BindingTimeoutCategory(int timeout) {
            this.bindingTimeout = timeout;
        }

        public static BindingTimeoutCategory create(long timeout) {
            if (timeout < LOW.bindingTimeout) {
                return LOW;
            }
            return HIGH;
        }

        public int getBindingTimeout() {
            return bindingTimeout;
        }
    }
    private final Type type;
    private final MappingPolicy mappingPolicy;
    private final AllocationPolicy allocationPolicy;
    private final FilteringPolicy filteringPolicy;
    private final AlternativePortAllocationPolicy alternativePortAllocationPolicy;
    private long bindingTimeout;
    private final int delta;
    private final Address publicUPNPAddress;

    public Nat(Type type) {
        this.type = type;
        if (type == Type.NAT || type == Type.UPNP) {
            throw new IllegalStateException("Only use this constructor if your NAT is OPEN or UDP-BLOCKED");
        }
        this.mappingPolicy = MappingPolicy.OPEN;
        this.allocationPolicy = AllocationPolicy.OPEN;
        this.filteringPolicy = FilteringPolicy.OPEN;
        this.alternativePortAllocationPolicy = AlternativePortAllocationPolicy.OPEN;
        this.delta = 0;
        this.publicUPNPAddress = null;
        this.bindingTimeout = BindingTimeoutCategory.LOW.getBindingTimeout();
    }

    public Nat(Type type, Address publicUPNPAddress,
            MappingPolicy mappingPolicy, AllocationPolicy allocationPolicy,
            FilteringPolicy filteringPolicy) {
        this.type = type;
        if (type != Type.UPNP) {
            throw new IllegalStateException("Only use this constructor if your NAT is upnp");
        }
        if (publicUPNPAddress == null) {
            throw new NullPointerException("publicUPnPAddress was null");
        }
        this.mappingPolicy = mappingPolicy;
        this.allocationPolicy = allocationPolicy;
        this.filteringPolicy = filteringPolicy;
        this.alternativePortAllocationPolicy = AlternativePortAllocationPolicy.RANDOM;
        this.delta = 0;
        this.publicUPNPAddress = publicUPNPAddress;
        this.bindingTimeout = BindingTimeoutCategory.LOW.getBindingTimeout();
    }

    public Nat(Type type, MappingPolicy mappingPolicy, AllocationPolicy allocationPolicy,
            FilteringPolicy filteringPolicy,
            int delta, long udpNatBindingTimeout) {
        assert mappingPolicy != null;
        assert allocationPolicy != null;
        assert filteringPolicy != null;
        assert delta >= 0;

        this.type = type;
        this.mappingPolicy = mappingPolicy;
        this.allocationPolicy = allocationPolicy;
        this.filteringPolicy = filteringPolicy;
        this.alternativePortAllocationPolicy = AlternativePortAllocationPolicy.RANDOM;
        this.delta = delta;
        this.publicUPNPAddress = null;
        this.bindingTimeout = (udpNatBindingTimeout < BindingTimeoutCategory.LOW.getBindingTimeout())
                ? BindingTimeoutCategory.LOW.getBindingTimeout() : udpNatBindingTimeout;
    }

    public Address getPublicUPNPAddress() {
        return publicUPNPAddress;
    }

    public int getDelta() {
        return delta;
    }

    public AlternativePortAllocationPolicy getAlternativePortAllocationPolicy() {
        return alternativePortAllocationPolicy;
    }

    public Type getType() {
        return type;
    }

    public AllocationPolicy getAllocationPolicy() {
        return allocationPolicy;
    }

    public FilteringPolicy getFilteringPolicy() {
        return filteringPolicy;
    }

    public MappingPolicy getMappingPolicy() {
        return mappingPolicy;
    }

    public long getBindingTimeout() {
        return bindingTimeout;
    }

    private boolean isEndpointIndependent() {
        if (filteringPolicy == null || mappingPolicy == null) {
            return false;
        }
        return (filteringPolicy == FilteringPolicy.ENDPOINT_INDEPENDENT)
                && (mappingPolicy == MappingPolicy.ENDPOINT_INDEPENDENT);
    }

    public boolean isOpen() {
//            TODO: || isEndpointIndependentFiltering() && isEndpointIndependentMapping()
        if (type == Type.OPEN) {
            return true;
        }
        return false;
    }

    public boolean isUpnp() {
        if (type == Type.UPNP) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {

        StringBuilder msg = new StringBuilder();
        if (type != Type.NAT) {
            msg.append(type.toString());
            return msg.toString();
        } else {
            if (type == Type.NAT) {
                String mp;
                if (mappingPolicy == MappingPolicy.ENDPOINT_INDEPENDENT) {
                    mp = "m(EI)";
                } else if (mappingPolicy == MappingPolicy.HOST_DEPENDENT) {
                    mp = "m(HD)";
                } else if (mappingPolicy == MappingPolicy.PORT_DEPENDENT) {
                    mp = "m(PD)";
                } else {
                    mp = "m(??)";
                }
                String ap;
                if (allocationPolicy == AllocationPolicy.PORT_PRESERVATION) {
                    ap = "a(PP)";
                } else if (allocationPolicy == AllocationPolicy.PORT_CONTIGUITY) {
                    ap = "a(PC)";
                } else if (allocationPolicy == AllocationPolicy.RANDOM) {
                    ap = "a(RA)";
                } else {
                    ap = "a(??)";
                }
                String fp;
                if (filteringPolicy == FilteringPolicy.ENDPOINT_INDEPENDENT) {
                    fp = "f(EI)";
                } else if (filteringPolicy == FilteringPolicy.HOST_DEPENDENT) {
                    fp = "f(HD)";
                } else if (filteringPolicy == FilteringPolicy.PORT_DEPENDENT) {
                    fp = "f(PD)";
                } else {
                    fp = "f(??)";
                }
                msg.append(mp).append("_").append(ap).append("_").append(fp);
            } else {
                msg.append("OPEN");
            }

            return msg.toString();
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        if (o instanceof Nat == false) {
            return -1;
        }
        Nat that = (Nat) o;

        int thisFiltering = this.filteringPolicy.ordinal();
        int thatFiltering = that.filteringPolicy.ordinal();
        int thisMapping = this.mappingPolicy.ordinal();
        int thatMapping = that.mappingPolicy.ordinal();
        int thisAllocation = this.allocationPolicy.ordinal();
        int thatAllocation = that.allocationPolicy.ordinal();
        int thisAltAllocation = this.alternativePortAllocationPolicy.ordinal();
        int thatAltAllocation = that.alternativePortAllocationPolicy.ordinal();

        if (thisFiltering == thatFiltering && thisMapping == thatMapping
                && thisAllocation == thatAllocation && thisAltAllocation == thatAltAllocation) {
            return 0;
        }

        if (thisFiltering < thatFiltering && thisMapping <= thatMapping
                && thisAllocation <= thatAllocation && thisAltAllocation <= thatAltAllocation) {
            return -1;
        } else if (thisFiltering <= thatFiltering && thisMapping < thatMapping
                && thisAllocation <= thatAllocation && thisAltAllocation <= thatAltAllocation) {
            return -1;
        } else if (thisFiltering <= thatFiltering && thisMapping <= thatMapping
                && thisAllocation < thatAllocation && thisAltAllocation <= thatAltAllocation) {
            return -1;
        } else if (thisFiltering <= thatFiltering && thisMapping <= thatMapping
                && thisAllocation <= thatAllocation && thisAltAllocation < thatAltAllocation) {
            return -1;
        }


        return 1;
    }

    public static List<Nat> getAllNatCombinations() {
        List<Nat> nats = new ArrayList<Nat>();

        for (String natType : NAT_COMBINATIONS) {
            nats.add(parseToNat(natType));
        }

        return nats;
    }

    public static Nat parseToNat(String natType) {
        StringTokenizer stz = new StringTokenizer(natType, "_");
        String token = stz.nextToken();
        Nat.Type type = Nat.Type.decode(token);
        Nat.MappingPolicy mappingPolicy = Nat.MappingPolicy.decode((String) stz.nextElement());
        Nat.AllocationPolicy allocationPolicy = Nat.AllocationPolicy.decode((String) stz.nextElement());
        Nat.FilteringPolicy filteringPolicy = Nat.FilteringPolicy.decode((String) stz.nextElement());
        Nat.AlternativePortAllocationPolicy alternativePortAllocationPolicy = null;
        if (stz.hasMoreElements()) {
            alternativePortAllocationPolicy = Nat.AlternativePortAllocationPolicy.decode((String) stz.nextElement());
            if (alternativePortAllocationPolicy == null) {
                return null;
            }
        }
        if (type == null || mappingPolicy == null || allocationPolicy == null || filteringPolicy == null) {
            return null;
        }
        
        return new Nat(type, mappingPolicy, allocationPolicy, filteringPolicy,
                0, 3000);
    }

    public void setBindingTimeout(long bindingTimeout) {
        this.bindingTimeout = (bindingTimeout < BindingTimeoutCategory.LOW.getBindingTimeout())
                ? BindingTimeoutCategory.LOW.getBindingTimeout() : bindingTimeout;
    }

    /**
     * A node that requires PRP-PRP or PRP-PRC or PRP (with PD filtering) will
     * need to allocate a new port for each new connection. Pre-allocated ports
     * are sent to the zServer.
     */
    public boolean preallocatePorts() {
        if (this.allocationPolicy == AllocationPolicy.PORT_PRESERVATION) {
            return true;
        }
        return false;
    }

    public boolean moreRestrictiveThan(MappingPolicy other) {
        return mappingPolicy.ordinal() > other.ordinal();
    }

    public boolean moreRestrictiveThan(AllocationPolicy other) {
        return allocationPolicy.ordinal() > other.ordinal();
    }

    public boolean moreRestrictiveThan(FilteringPolicy other) {
        return filteringPolicy.ordinal() > other.ordinal();
    }
}
