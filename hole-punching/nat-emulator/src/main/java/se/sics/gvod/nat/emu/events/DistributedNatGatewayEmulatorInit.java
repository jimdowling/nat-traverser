package se.sics.gvod.nat.emu.events;

import java.net.InetAddress;
import se.sics.gvod.net.Nat;
import se.sics.kompics.Init;


/**
 * @author Jim Dowling <jdowling@sics.se>
 */
public class DistributedNatGatewayEmulatorInit extends Init {

    private final Nat.MappingPolicy mp;
    private final Nat.AllocationPolicy ap;
    private final Nat.FilteringPolicy fp;
    private final Nat.AlternativePortAllocationPolicy altAp;
    private final Nat.Type natType; // i.e. nat, upnp or open
    private final int ruleCleanupPeriod;
    private final InetAddress natIP;
    private final int maxPort;
    private final boolean clashingOverrides;
    private final int ruleLifeTime;
    private final int randomPortSeed;
    private final int startPortRange;
    private final int endPortRange;
    private final boolean enableUpnp;

    public DistributedNatGatewayEmulatorInit(Nat nat, InetAddress natIp, int startPortRange,
            int endPortRange) {
        assert(natIp != null);
        assert(nat != null);
        this.natType = nat.getType();
        this.mp = nat.getMappingPolicy();
        this.ap = nat.getAllocationPolicy();
        this.fp = nat.getFilteringPolicy();
        this.altAp = nat.getAlternativePortAllocationPolicy();
        this.natIP = natIp;
        this.ruleCleanupPeriod = 30 * 1000;
        this.maxPort = 55000;
        this.clashingOverrides = false;
        this.randomPortSeed = 10;
        this.enableUpnp = nat.isUpnp();
        this.ruleLifeTime = 90 * 1000;
        this.startPortRange = startPortRange;
        this.endPortRange = endPortRange;
    }
                
    public DistributedNatGatewayEmulatorInit(
            int startPortRange, 
            Nat.MappingPolicy mp, 
            Nat.AllocationPolicy ap,
            Nat.AlternativePortAllocationPolicy alternativePolicy,
            Nat.FilteringPolicy fp, 
            Nat.Type natType,
            int ruleCleanupPeriod, 
            InetAddress natIP, 
            int maxPort,
            boolean clashingOverrides, 
            int ruleLifeTime,
            int randomPortSeed,
            boolean enableUpnp) {
        this.startPortRange = startPortRange;
        this.endPortRange = maxPort;
        this.mp = mp;
        this.ap = ap;
        this.fp = fp;
        this.altAp = alternativePolicy;
        this.natType = natType;
        this.ruleCleanupPeriod = ruleCleanupPeriod;
        this.natIP = natIP;
        this.maxPort = maxPort;
        this.clashingOverrides = clashingOverrides;
        this.ruleLifeTime = ruleLifeTime;
        this.randomPortSeed = randomPortSeed;
        this.enableUpnp = enableUpnp;
    }

    public DistributedNatGatewayEmulatorInit(int startPortRange, 
            Nat nat, int ruleCleanupPeriod,
            InetAddress natIP, int maxPort, boolean clashingOverrides,
            int randomPortSeed, boolean enableUpnp)
    {
        this.startPortRange = startPortRange;
        this.endPortRange = maxPort;
        this.mp = nat.getMappingPolicy();
        this.ap = nat.getAllocationPolicy();
        this.altAp = nat.getAlternativePortAllocationPolicy();
        this.fp = nat.getFilteringPolicy();
        this.ruleLifeTime = (int) nat.getBindingTimeout();
        this.natType = nat.getType();
        this.ruleCleanupPeriod = ruleCleanupPeriod;
        this.natIP = natIP;
        this.maxPort = maxPort;
        this.clashingOverrides = clashingOverrides;
        this.randomPortSeed = randomPortSeed;
        this.enableUpnp = enableUpnp;
    }

    public int getEndPortRange() {
        return endPortRange;
    }

    public int getStartPortRange() {
        return startPortRange;
    }

    
    public int getRandomPortSeed() {
        return randomPortSeed;
    }

    public int getRuleLifeTime() {
        return ruleLifeTime;
    }

    public boolean isClashingOverrides() {
        return clashingOverrides;
    }

    public int getMaxPort() {
        return maxPort;
    }

    public Nat.AlternativePortAllocationPolicy getAltAp() {
        return altAp;
    }

    public int getRuleCleanupPeriod() {
        return ruleCleanupPeriod;
    }

    public Nat.Type getNatType() {
        return natType;
    }

    public Nat.AllocationPolicy getAp() {
        return ap;
    }

    public Nat.FilteringPolicy getFp() {
        return fp;
    }

    public Nat.MappingPolicy getMp() {
        return mp;
    }

    public InetAddress getNatIP() {
        return natIP;
    }

    public boolean isEnableUpnp() {
        return enableUpnp;
    }


}
