package se.sics.gvod.simulator.nattraverser.scenarios;

import se.sics.gvod.simulator.croupier.scenarios.*;
import java.util.HashMap;
import java.util.Random;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.simulator.common.PeerChurn;
import se.sics.gvod.simulator.common.PeerFail;
import se.sics.gvod.simulator.common.PeerJoin;
import se.sics.gvod.simulator.common.StartCollectData;
import se.sics.gvod.simulator.common.StopCollectData;
import se.sics.gvod.simulator.nattraverser.ConnectPeers;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation1;
import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation2;

@SuppressWarnings("serial")
public class Operations {

    private static int index = 0;
    private static Random rnd = new Random();

    static Operation1<PeerJoin, Long> peerJoin(final VodAddress.NatType peerType) {
        return new Operation1<PeerJoin, Long>() {

            @Override
            public PeerJoin generate(Long id) {
                return new PeerJoin(id.intValue(), peerType);
            }
        };
    }

    static Operation1<PeerJoin, Long> peerJoin(final double privateNodesRatio) {
        return new Operation1<PeerJoin, Long>() {

            @Override
            public PeerJoin generate(Long id) {
                VodAddress.NatType peerType;
                index++;

                if (rnd.nextDouble() < privateNodesRatio) {
                    peerType = VodAddress.NatType.NAT;
                } else {
                    peerType = VodAddress.NatType.OPEN;
                }

                return new PeerJoin(id.intValue(), peerType);
            }
        };
    }

    static Operation2<ConnectPeers, Long, Long> connectPeers() {
        return new Operation2<ConnectPeers, Long, Long>() {

            @Override
            public ConnectPeers generate(Long src, Long dest) {
                return new ConnectPeers(src.intValue(), dest.intValue());
            }
        };
    }
    
    static Operation1<PeerFail, Long> peerFail(final VodAddress.NatType peerType) {
        return new Operation1<PeerFail, Long>() {

            @Override
            public PeerFail generate(Long id) {
                return new PeerFail(id.intValue(), peerType);
            }
        };
    }

    static Operation1<PeerFail, Long> peerFail(final double privateNodesRatio) {
        return new Operation1<PeerFail, Long>() {

            @Override
            public PeerFail generate(Long id) {
                VodAddress.NatType peerType;
                index++;

                if (rnd.nextDouble() < privateNodesRatio) {
                    peerType = VodAddress.NatType.NAT;
                } else {
                    peerType = VodAddress.NatType.OPEN;
                }

                return new PeerFail(id.intValue(), peerType);
            }
        };
    }

    static Operation1<PeerChurn, Long> skypeChurn(final HashMap<Integer, Integer> trace) {
        return new Operation1<PeerChurn, Long>() {

            @Override
            public PeerChurn generate(Long id) {
                index++;

                while (trace.get(index) == null) {
                    index++;
                    if (index == 1300) {
                        break;
                    }
                }

                return new PeerChurn(id.intValue(), trace.get(index), ScenarioSkype.PRIVATE_NODES_RATIO);
            }
        };
    }

    static Operation<StartCollectData> startCollectData() {
        return new Operation<StartCollectData>() {

            @Override
            public StartCollectData generate() {
                return new StartCollectData();
            }
        };
    }

    static Operation<StopCollectData> stopCollectData() {

        return new Operation<StopCollectData>() {

            @Override
            public StopCollectData generate() {
                return new StopCollectData();
            }
        };
    }
}