package se.sics.gvod.simulator.croupier;

import se.sics.gvod.simulator.common.PeerFail;
import se.sics.gvod.simulator.common.PeerChurn;
import se.sics.gvod.simulator.common.SimulatorPort;
import se.sics.gvod.simulator.common.StartCollectData;
import se.sics.gvod.simulator.common.PeerJoin;
import se.sics.gvod.simulator.common.StopCollectData;
import se.sics.gvod.simulator.common.GenerateReport;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.ipasdistances.AsIpGenerator;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.*;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.croupier.Croupier;
import se.sics.gvod.config.CroupierConfiguration;
import se.sics.gvod.croupier.CroupierPort;
import se.sics.gvod.croupier.events.CroupierInit;
import se.sics.gvod.common.evts.JoinCompleted;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.Nat;
import se.sics.gvod.parentmaker.ParentMaker;
import se.sics.gvod.config.ParentMakerConfiguration;
import se.sics.gvod.parentmaker.ParentMakerInit;
import se.sics.gvod.simulator.common.ConsistentHashtable;
import se.sics.gvod.config.CroupierCompositeConfiguration;
import se.sics.gvod.croupier.snapshot.CroupierStats;
import se.sics.gvod.filters.MsgDestFilterNodeId;
import se.sics.gvod.filters.MsgDestFilterOverlayId;
import se.sics.gvod.nat.common.PortInit;
import se.sics.gvod.nat.common.PortReservoirComp;
import se.sics.gvod.nat.emu.DistributedNatGatewayEmulator;
import se.sics.gvod.nat.emu.events.DistributedNatGatewayEmulatorInit;
import se.sics.gvod.config.HpClientConfiguration;
import se.sics.gvod.config.NatTraverserConfiguration;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.nat.traversal.NatTraverser;
import se.sics.gvod.nat.traversal.events.NatTraverserInit;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.config.StunServerConfiguration;
import se.sics.gvod.croupier.events.CroupierJoin;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;

public final class CroupierSimulator extends ComponentDefinition {

    private final static int EXPERIMENT_TIME = 1 * 5000 * 1000;
    Positive<SimulatorPort> simulator = positive(SimulatorPort.class);
    Positive<VodNetwork> network = positive(VodNetwork.class);
    Positive<Timer> timer = positive(Timer.class);
    Positive<CroupierPort> croupierPort = positive(CroupierPort.class);
    private static final Logger logger = LoggerFactory.getLogger(CroupierSimulator.class);
    private final HashMap<Integer, Component> publicPeers;
    private final HashMap<Integer, Component> privatePeers;
    private final HashMap<Integer, VodAddress.NatType> peerTypes;
    private final HashMap<Integer, VodAddress> privateAddress;
    private final HashMap<Integer, VodAddress> publicAddress;
    // peer initialization state
    private CroupierConfiguration croupierConfiguration;
    private ParentMakerConfiguration parentMakerConfiguration;
    private ConcurrentSkipListSet<Integer> parentPorts = 
            new ConcurrentSkipListSet<Integer>();
    private int peerIdSequence;
    private ConsistentHashtable<Integer> view;
    private Random rnd;
    private AsIpGenerator ipGenerator;
//    private PrefixMatcher pm = PrefixMatcher.getInstance();
//-------------------------------------------------------------------	

    public CroupierSimulator() {
        publicPeers = new HashMap<Integer, Component>();
        privatePeers = new HashMap<Integer, Component>();
        privateAddress = new HashMap<Integer, VodAddress>();
        publicAddress = new HashMap<Integer, VodAddress>();
        peerTypes = new HashMap<Integer, VodAddress.NatType>();
        view = new ConsistentHashtable<Integer>();

        subscribe(handleInit, control);

        subscribe(handleGenerateReport, timer);

        subscribe(handleCroupierPeerJoin, simulator);
        subscribe(handleCroupierPeerFail, simulator);
        subscribe(handleCroupierPeerChurn, simulator);

        subscribe(handleStartCollectData, simulator);
        subscribe(handleStopCollectData, simulator);

        subscribe(handleCroupierJoinCompleted, croupierPort);
    }
//-------------------------------------------------------------------	
    Handler<CroupierSimulatorInit> handleInit = new Handler<CroupierSimulatorInit>() {

        @Override
        public void handle(CroupierSimulatorInit init) {
            rnd = new Random(init.getCroupierConfiguration().getSeed());
            publicPeers.clear();
            privatePeers.clear();
            peerIdSequence = 100;
            ipGenerator = AsIpGenerator.getInstance(init.getCroupierConfiguration().getSeed());
//            bootstrapConfiguration = init.getBootstrapConfiguration();
            croupierConfiguration = init.getCroupierConfiguration();
            parentMakerConfiguration = init.getParentMakerConfiguration();

            // generate periodic report
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(CroupierCompositeConfiguration.SNAPSHOT_PERIOD, CroupierCompositeConfiguration.SNAPSHOT_PERIOD);
            spt.setTimeoutEvent(new GenerateReport(spt));
            trigger(spt, timer);
        }
    };
//-------------------------------------------------------------------	
    Handler<PeerJoin> handleCroupierPeerJoin = new Handler<PeerJoin>() {

        @Override
        public void handle(PeerJoin event) {
            Integer id = event.getPeerId();
            VodAddress.NatType peerType = event.getPeerType();
            join(id, peerType);
        }
    };
//-------------------------------------------------------------------	
    Handler<PeerFail> handleCroupierPeerFail = new Handler<PeerFail>() {

        @Override
        public void handle(PeerFail event) {
            Integer id = event.getPeerId();
            VodAddress.NatType peerType = event.getPeerType();

            fail(id, peerType);
        }
    };
//-------------------------------------------------------------------	
    Handler<PeerChurn> handleCroupierPeerChurn = new Handler<PeerChurn>() {

        @Override
        public void handle(PeerChurn event) {
            VodAddress.NatType peerType;
            Integer id = event.getPeerId();
            int operation = event.getOperation();
            double privateNodesRatio = event.getPrivateNodesRatio();
            int operationCount = Math.abs(operation);

            if (operation > 0) { // join
                for (int i = 0; i < operationCount; i++) {
                    if (rnd.nextDouble() < privateNodesRatio) {
                        peerType = VodAddress.NatType.NAT;
                    } else {
                        peerType = VodAddress.NatType.OPEN;
                    }

                    join(id, peerType);
                }
            } else { // failure				
                for (int i = 0; i < operationCount; i++) {
                    if (rnd.nextDouble() < privateNodesRatio) {
                        peerType = VodAddress.NatType.NAT;
                    } else {
                        peerType = VodAddress.NatType.OPEN;
                    }

                    fail(id, peerType);
                }
            }
        }
    };

//-------------------------------------------------------------------	
    private Component createAndStartNewPeer(Integer id, VodAddress.NatType natType) {
        long seed = System.currentTimeMillis();
        Component croupier = create(Croupier.class);
        Component parentMaker = create(ParentMaker.class);
        Component natTraverser = create(NatTraverser.class);
        Component natGateway = create(DistributedNatGatewayEmulator.class);
        Component portReservoir = create(PortReservoirComp.class);

        InetAddress ip = ipGenerator.generateIP();
        Address peerAddress = new Address(ip, 8081, id);

        Nat nat;
        InetAddress natIp = ipGenerator.generateIP();
        if (natType == VodAddress.NatType.OPEN) {
            nat = new Nat(Nat.Type.OPEN);
            publicPeers.put(id, croupier);
            trigger(new DistributedNatGatewayEmulatorInit(new Nat(Nat.Type.OPEN),
                    natIp, 10000, 65000), natGateway.control());
        } else {
            nat = new NatFactory(seed).getProbabilisticNat();
            trigger(new DistributedNatGatewayEmulatorInit(nat, natIp, 50000, 65000), natGateway.control());
            trigger(new PortInit(seed), portReservoir.control());
            privatePeers.put(id, croupier);
        }
        VodAddress croupierPeerAddress = new VodAddress(peerAddress,
                VodConfig.SYSTEM_OVERLAY_ID, nat);

        int filterId = peerAddress.getId();
        connect(natTraverser.getPositive(VodNetwork.class),
                croupier.getNegative(VodNetwork.class), 
                new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID));
        connect(natGateway.getPositive(VodNetwork.class),
                natTraverser.getNegative(VodNetwork.class));
        connect(natGateway.getPositive(VodNetwork.class),
                parentMaker.getNegative(VodNetwork.class)
//                ,new MsgDestFilterOverlayId(VodConfig.SYSTEM_OVERLAY_ID)
                );
        connect(network, natGateway.getNegative(VodNetwork.class), new MsgDestFilterNodeId(filterId));

        connect(timer, croupier.getNegative(Timer.class));
        connect(timer, natTraverser.getNegative(Timer.class));
        connect(timer, parentMaker.getNegative(Timer.class));
        connect(timer, natGateway.getNegative(Timer.class));


        connect(natGateway.getPositive(NatNetworkControl.class),
                parentMaker.getNegative(NatNetworkControl.class));
        connect(natGateway.getPositive(NatNetworkControl.class),
                natTraverser.getNegative(NatNetworkControl.class));
        connect(natGateway.getNegative(NatNetworkControl.class),
                portReservoir.getPositive(NatNetworkControl.class));

//        subscribe(handleRebootstrap, croupier.getPositive(CroupierPort.class));


        Self self = new SelfImpl(croupierPeerAddress);

        trigger(new NatTraverserInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID),
                new HashSet<Address>(),
                seed,
                NatTraverserConfiguration.build(),
                HpClientConfiguration.build(),
                RendezvousServerConfiguration.build().
                setSessionExpirationTime(30*1000),
                StunClientConfiguration.build(),
                StunServerConfiguration.build(),
                ParentMakerConfiguration.build(), false
                ), natTraverser.control());

        if (natType == VodAddress.NatType.OPEN) {
            publicAddress.put(id, croupierPeerAddress);
        } else {
            privateAddress.put(id, croupierPeerAddress);
            trigger(new ParentMakerInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID),
                    parentMakerConfiguration, parentPorts), parentMaker.control());
        }


        trigger(new CroupierInit(self.clone(VodConfig.SYSTEM_OVERLAY_ID),
                croupierConfiguration), croupier.getControl());

        return croupier;
    }

//-------------------------------------------------------------------	
    private void stopAndDestroyPeer(Integer id) {
        Component peer = privatePeers.get(id);
        if (peer == null) {
            peer = publicPeers.get(id);
        }

        trigger(new Stop(), peer.getControl());

//        disconnect(network, peer.getNegative(VodNetwork.class));
//        disconnect(timer, peer.getNegative(Timer.class));

        CroupierStats.removeNode(id, VodConfig.SYSTEM_OVERLAY_ID);

        privatePeers.remove(id);
        publicPeers.remove(id);
        privateAddress.remove(id);
        publicAddress.remove(id);
        peerTypes.remove(id);

        destroy(peer);
    }
    Handler<GenerateReport> handleGenerateReport = new Handler<GenerateReport>() {

        @Override
        public void handle(GenerateReport event) {
            CroupierStats.report(VodConfig.SYSTEM_OVERLAY_ID);
            if (System.currentTimeMillis() > EXPERIMENT_TIME) {
                Kompics.shutdown();
                System.exit(0);
            }
        }
    };
//-------------------------------------------------------------------	
    Handler<StartCollectData> handleStartCollectData = new Handler<StartCollectData>() {

        @Override
        public void handle(StartCollectData event) {
            CroupierStats.startCollectData();
        }
    };
//-------------------------------------------------------------------	
    Handler<StopCollectData> handleStopCollectData = new Handler<StopCollectData>() {

        @Override
        public void handle(StopCollectData event) {
            CroupierStats.stopCollectData();
            CroupierStats.report(VodConfig.SYSTEM_OVERLAY_ID);

            Kompics.shutdown();
            System.exit(0);
        }
    };
//    Handler<Rebootstrap> handleRebootstrap = new Handler<Rebootstrap>() {
//
//        @Override
//        public void handle(Rebootstrap event) {
//            logger.warn("Rebootstrapping...." + event.getId());
//            Component peer = publicPeers.get(event.getId());
//            if (peer == null) {
//                peer = privatePeers.get(event.getId());
//            }
//            if (peer != null) {
//                trigger(new RebootstrapResponse(event.getId(), getNodes()),
//                        peer.getPositive(CroupierPort.class));
//            } else {
//                logger.warn("Couldn't Reboot null peer with id: " + event.getId());
//            }
//        }
//    };
    Handler<JoinCompleted> handleCroupierJoinCompleted = new Handler<JoinCompleted>() {

        @Override
        public void handle(JoinCompleted event) {
            // TODO
        }
    };

//-------------------------------------------------------------------	
    private void join(Integer id, VodAddress.NatType peerType) {
        // join with the next id if this id is taken
        Integer successor = view.getNode(id);
        while (successor != null && successor.equals(id)) {
            id = (id == Integer.MAX_VALUE) ? 0 : ++peerIdSequence;
            successor = view.getNode(id);
        }

        logger.debug("JOIN@{}", id);

        Component newPeer = createAndStartNewPeer(id, peerType);
        view.addNode(id);
        peerTypes.put(id, peerType);

        trigger(new CroupierJoin(getNodes()), newPeer.getPositive(CroupierPort.class));
    }

    private List<VodDescriptor> getNodes() {
        List<VodDescriptor> nodes = new ArrayList<VodDescriptor>();
        int i = 10;
        List<VodAddress> candidates = new ArrayList<VodAddress>();
        candidates.addAll(publicAddress.values());
        Collections.shuffle(candidates);
        for (VodAddress a : candidates) {
//            int asn = pm.matchIPtoAS(a.getIp().getHostAddress());
            VodDescriptor gnd = new VodDescriptor(a, new UtilityVod(0), 0, 1500);
            nodes.add(gnd);
            if (--i == 0) {
                break;
            }
        }
        return nodes;
    }

//-------------------------------------------------------------------	
    private void fail(Integer id, VodAddress.NatType peerType) {
        id = view.getNode(id);

        logger.debug("FAIL@" + id);

        if (view.size() == 0) {
            System.err.println("Empty network");
            return;
        }

        view.removeNode(id);
        stopAndDestroyPeer(id);
    }
}
