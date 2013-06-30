/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.Nat.AllocationPolicy;
import se.sics.gvod.net.Nat.AlternativePortAllocationPolicy;
import se.sics.gvod.net.Nat.FilteringPolicy;
import se.sics.gvod.net.Nat.MappingPolicy;
import se.sics.gvod.address.Address;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.net.VodAddress;

/**
 *
 * @author Jim
 */
 class Session {
    private Logger Logger = LoggerFactory.getLogger(getClass().getName());

    private SessionState state = SessionState.UDP_BLOCKED;
    private final long transactionId;
    private Address publicNatAddrServer1;
    private Address server1;
    private int server1Port2;
    private MappingAllocState mapAllocState = MappingAllocState.NOT_STARTED;
    private FilterState filterState = FilterState.UDP_BLOCKED;
    private Nat.MappingPolicy mappingPolicy = Nat.MappingPolicy.ENDPOINT_INDEPENDENT;
    private Nat.FilteringPolicy filteringPolicy = Nat.FilteringPolicy.ENDPOINT_INDEPENDENT;
    private Nat.AllocationPolicy allocationPolicy = Nat.AllocationPolicy.PORT_CONTIGUITY;
    private Nat.AlternativePortAllocationPolicy alternativeAllocationPolicy =
            Nat.AlternativePortAllocationPolicy.PORT_CONTIGUITY;
    private Stack<Address> partnerServers;
    private int bestPartnerRtt;
    private Address partnerServer;
    private boolean finishedFilter = false;
    private boolean finishedMapping = false;
    private boolean finishedAllocation = false;
    private boolean recvdEchoS1 = false;
    private boolean recvdEchoS2 = false;
    private final boolean measureNatBindingTimeout;
    private int delta = 1;
    private Map<Integer, Address> tries = new HashMap<Integer, Address>(); // [NUM_PING_TRIES];
    private int clientFirstRandomPort;
    private int clientSecondRandomPort;
    private long ruleDeterminationStartTime;
    private long ruleLifeTime;
    private final long startTime;
    private int natRuleExpirationTime;
    private final Address privateAddress;

    public Session(long transactionId, Address privateAddress, Address stunServer, 
            boolean measureNatBindingTimeout) {
        this.transactionId = transactionId;
        this.privateAddress = privateAddress;
        this.server1 = stunServer;
        this.measureNatBindingTimeout = measureNatBindingTimeout;
        startTime = System.currentTimeMillis();
    }

    public long getStartTime() {
        return startTime;
    }
    
    public boolean isMeasureNatBindingTimeout() {
        return measureNatBindingTimeout;
    }
    
    public int getServer1Port2() {
        return server1Port2;
    }

    public void setServer1Port2(int server1Port2) {
        this.server1Port2 = server1Port2;
    }

    public VodAddress getServer1() {
        return ToVodAddr.stunServer(server1);
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public SessionState getState() {
        return state;
    }

    public int getNatRuleExpirationTime() {
        return natRuleExpirationTime;
    }

    public void setNatRuleExpirationTime(int natRuleExpirationTime) {
        this.natRuleExpirationTime = natRuleExpirationTime;
    }

    public MappingAllocState getMapAllocState() {
        return mapAllocState;
    }

    public void setMapAllocState(MappingAllocState mapAllocState) {
        this.mapAllocState = mapAllocState;
    }

    public FilterState getFilterState() {
        return filterState;
    }

    public void setFilterState(FilterState filterState) {
        this.filterState = filterState;
    }

    public long getRuleLifeTime() {
        return ruleLifeTime;
    }

    public void setRuleLifeTime(long ruleLifeTime) {
        this.ruleLifeTime = ruleLifeTime;
    }

    public void setRuleDeterminationStartTime(long ruleDeterminationStartTime) {
        this.ruleDeterminationStartTime = ruleDeterminationStartTime;
    }

    public long getRuleDeterminationStartTime() {
        return ruleDeterminationStartTime;
    }

    public int getClientFirstRandomPort() {
        return clientFirstRandomPort;
    }

    public int getClientSecondRandomPort() {
        return clientSecondRandomPort;
    }

    public void setClientFirstRandomPort(int clientFirstRandomPort) {
        this.clientFirstRandomPort = clientFirstRandomPort;
    }

    public void setClientSecondRandomPort(int clientSecondRandomPort) {
        this.clientSecondRandomPort = clientSecondRandomPort;
    }

    public int getTotalTryMessagesFinished() {
        return tries.size();
    }
    
    public int getTotalTryMessagesReceived() {
        int sz = 0;
        for (Address a : tries.values()) {
            if (a != null) {
                sz++;
            }
        }
        return sz;
    }

    public Address getTry(int i) {
        return tries.get(i);
    }

    public void setTry(int i, Address publicAddress) {
        Logger.debug("Set try " + i + " : " + publicAddress);
        tries.put(i, publicAddress);
    }

    public int getDelta() {
        return delta;
    }

    public boolean isFinishedAllocation() {
        return finishedAllocation;
    }

    public void setAlternativeAllocationPolicy(AlternativePortAllocationPolicy alternativeAllocationPolicy) {
        this.alternativeAllocationPolicy = alternativeAllocationPolicy;
    }

    public void setDelta(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("Delta must be positive");
        }
        this.delta = delta;
    }

    public void setFinishedAllocation(boolean finishedAllocation) {
        this.finishedAllocation = finishedAllocation;
    }

    public Address getPrivateAddress() {
        return privateAddress;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setPartnerServers(Stack<Address> changeIpServers, int bestPartnerRtt) {
        this.partnerServers = changeIpServers;
        this.bestPartnerRtt = bestPartnerRtt;
    }

    public boolean isFinishedFilter() {
        return finishedFilter;
    }

    public void setFinishedFilter(boolean finishedFilter) {
        this.finishedFilter = finishedFilter;
    }

    public boolean isFinishedMapping() {
        return finishedMapping;
    }

    public void setFinishedMapping(boolean finishedMapping) {
        this.finishedMapping = finishedMapping;
    }

    /**
     *
     * @return null if no server available
     */
    public Address popPartnerServer() {
        if (partnerServers.empty()) {
            return null;
        }
        partnerServer = partnerServers.pop();
        return partnerServer;
    }

    public int getBestPartnerRtt() {
        return bestPartnerRtt;
    }

    /**
     *
     * @return null if no server available
     */
    public VodAddress getPartnerServer() {

        if (partnerServer == null) {
            partnerServer = popPartnerServer();
        }
        return new VodAddress(partnerServer, VodConfig.STUN_OVERLAY_ID);
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

    public void setAllocationPolicy(AllocationPolicy allocationPolicy) {
        this.allocationPolicy = allocationPolicy;
    }

    public void setFilteringPolicy(FilteringPolicy filteringPolicy) {
        this.filteringPolicy = filteringPolicy;
    }

    public void setMappingPolicy(MappingPolicy mappingPolicy) {
        this.mappingPolicy = mappingPolicy;
    }

    public Address getPublicAddrTest1() {
        return publicNatAddrServer1;
    }

    public void setPublicAddrTest1(Address lastPublicAddr) {
        this.publicNatAddrServer1 = lastPublicAddr;
    }

    public boolean comparePublicAddr(Address recvdAddr) {
        if (recvdAddr.equals(publicNatAddrServer1)) {
            return true;
        }
        return false;
    }

    public boolean isRecvdEchoS1() {
        return recvdEchoS1;
    }

    public boolean isRecvdEchoS2() {
        return recvdEchoS2;
    }

    public void setRecvdEchoS1(boolean recvdEchoS1) {
        this.recvdEchoS1 = recvdEchoS1;
    }

    public void setRecvdEchoS2(boolean recvdEchoS2) {
        this.recvdEchoS2 = recvdEchoS2;
    }

    public AlternativePortAllocationPolicy getAlternativeAllocationPolicy() {
        return alternativeAllocationPolicy;
    }
    
}
