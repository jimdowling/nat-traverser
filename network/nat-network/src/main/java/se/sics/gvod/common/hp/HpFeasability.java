/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.hp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Nat;

/**
 *
 * @author jdowling
 */
public class HpFeasability {

    private static Logger logger = LoggerFactory.getLogger(HpFeasability.class);

    /**
     * If punching is possible then return the HolePunching object that tells which
     * client will use which stratagy to punch holes in the nat.
     * @param clientA source node
     * @param clientB destination node
     * @return HolePunching object if feasible, else null 
     */
    public static HolePunching isPossible(VodAddress clientA, VodAddress clientB) {
        HolePunching holePunchingData;

        // Test the different possible mechanisms in order of increasing chance
        // of failure (i.e., easiest mechanisms first).

        if ((holePunchingData = isConnectionReversalFeasible(clientA, clientB)) != null) {
            return holePunchingData;
        } else if ((holePunchingData = isShpFeasible(clientA, clientB)) != null) {
            return holePunchingData;
        } else if ((holePunchingData = isPrpFeasible(clientA, clientB)) != null) {
            return holePunchingData;
        } else if ((holePunchingData = isPrcFeasible(clientA, clientB)) != null) {
            return holePunchingData;
        } else if ((holePunchingData = isPrpPrpFeasible(clientA, clientB)) != null) {
            return holePunchingData;
        } else if ((holePunchingData = isPrcPrcFeasible(clientA, clientB)) != null) {
            return holePunchingData;
        } else if ((holePunchingData = isPrpPrcFeasible(clientA, clientB)) != null) {
            return holePunchingData;
        } else {
            return null;
        }
    }

    // Simple Holepunching Test
    // Checking simple hole punching feasibility. Theorm 6.1 -- NatCracker: Combinations Matter Paper.
    // WHEN TO USE SIMPLE HOLE PUNCHING SPH
    // 1. When both the clients have f:EI
    // 2. When one of the clients have f:EI and m:EI
    // 3. One of the clients has to have f:EI policy. if the mapping of such client is stricter than
    // EI then filtering policy of other client has to be more relaxed than f:PD
    //
    // if SHP is feasible then this function will return the HashKey of the client that should initiate the SHP
    private static HolePunching isShpFeasible(VodAddress clientA, VodAddress clientB) {
        int initiator = 0;
        if (clientA.getFilteringPolicy() == Nat.FilteringPolicy.ENDPOINT_INDEPENDENT
                && clientB.getFilteringPolicy() == Nat.FilteringPolicy.ENDPOINT_INDEPENDENT) {
            // any one can initiate the SHP but choose the client who initiated it. 
//            if (clientA.getId() < clientB.getId()) {
                initiator = clientA.getId();
//            } else {
//                initiator = clientB.getId();
//            }
        } // check if any one of the clients has f:EI
        else if (clientA.getFilteringPolicy() == Nat.FilteringPolicy.ENDPOINT_INDEPENDENT
                || clientB.getFilteringPolicy() == Nat.FilteringPolicy.ENDPOINT_INDEPENDENT) {
            // SHP might be possible
            // any one of the clients has f:EI and m:EI then SHP is possible
            if (clientA.getFilteringPolicy() == Nat.FilteringPolicy.ENDPOINT_INDEPENDENT
                    && clientA.getMappingPolicy() == Nat.MappingPolicy.ENDPOINT_INDEPENDENT) {
                initiator = clientA.getId();
            } else if (clientB.getFilteringPolicy() == Nat.FilteringPolicy.ENDPOINT_INDEPENDENT
                    && clientB.getMappingPolicy() == Nat.MappingPolicy.ENDPOINT_INDEPENDENT) {
                initiator = clientB.getId();
            } else if (clientA.getFilteringPolicy() == Nat.FilteringPolicy.ENDPOINT_INDEPENDENT
                    && clientA.getMappingPolicy() != Nat.MappingPolicy.ENDPOINT_INDEPENDENT
                    && clientB.getFilteringPolicy() != Nat.FilteringPolicy.PORT_DEPENDENT) {
                initiator = clientA.getId();
            } else if (clientB.getFilteringPolicy() == Nat.FilteringPolicy.ENDPOINT_INDEPENDENT
                    && clientB.getMappingPolicy() != Nat.MappingPolicy.ENDPOINT_INDEPENDENT
                    && clientA.getFilteringPolicy() != Nat.FilteringPolicy.PORT_DEPENDENT) {
                initiator = clientB.getId();
            } else {
                initiator = 0;
            }
        } 

        if (initiator == 0) {
            logger.trace("SHP is not possible between [" + clientA + ", " + clientB + "]");
            return null;
        } else {
            logger.debug("SHP is feasible between [" + clientA + ", " + clientB + "] initiator is (" + initiator + ")");

            HolePunching holePunching = null;
            if (initiator == clientA.getId()) /* if A is initiator then B is responder */ {
                holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                        HPMechanism.SHP, /*Overall mechanism used*/
                        HPRole.SHP_INITIATOR, /*client A will initiate SHP*/
                        HPRole.SHP_RESPONDER); /*client B will only repond to messages from A*/
            } else {
                holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                        HPMechanism.SHP, /*Overall mechanism used*/
                        HPRole.SHP_RESPONDER, /*client B will only repond to messages from A*/
                        HPRole.SHP_INITIATOR);/*client A will initiate SHP*/
            }
            return holePunching;
        }
    }

    private static HolePunching isPrpFeasible(VodAddress clientA, VodAddress clientB) {

        int initiator = 0;

        // prp not possible when neither of the clients have an allocation policy of (PP)
        if (clientA.getAllocationPolicy() != Nat.AllocationPolicy.PORT_PRESERVATION
                && clientB.getAllocationPolicy() != Nat.AllocationPolicy.PORT_PRESERVATION) {
            return null;
        } // checking if A can be the initiator
        boolean canABeTheInitiator = false;
        if (clientA.getAllocationPolicy() == Nat.AllocationPolicy.PORT_PRESERVATION
                && clientA.getFilteringPolicy() != Nat.FilteringPolicy.PORT_DEPENDENT) {
            if (clientA.getMappingPolicy() != Nat.MappingPolicy.PORT_DEPENDENT ||
                    clientB.getFilteringPolicy() != Nat.FilteringPolicy.PORT_DEPENDENT) {
                canABeTheInitiator = true;
            }
        }
        boolean canBBeTheInitiator = false;
        if (clientB.getAllocationPolicy() == Nat.AllocationPolicy.PORT_PRESERVATION
                && clientB.getFilteringPolicy() != Nat.FilteringPolicy.PORT_DEPENDENT) {
            if (clientB.getMappingPolicy() != Nat.MappingPolicy.PORT_DEPENDENT ||
                    clientA.getFilteringPolicy() != Nat.FilteringPolicy.PORT_DEPENDENT) {
                canBBeTheInitiator = true;
            }
        }

        // prefer A to be the initiator, as it can allocate ports locally first,
        // send them to the z-server.
        if (canABeTheInitiator) {
            initiator = clientA.getId();
        } else if (canBBeTheInitiator) {
            initiator = clientB.getId();
        }

        if (initiator == 0) {
            logger.trace("PRP is not possible between [" + clientA + ", " + clientB + "]");
            return null;
        } else {
            logger.debug("PRP is feasible between [" + clientA + ", " + clientB + "] initiator is (" + initiator + ")");

            HolePunching holePunching = null;
            if (initiator == clientA.getId()) /* if A is initiator then B is responder */ {
                holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                        HPMechanism.PRP, /*Overall mechanism used*/
                        HPRole.PRP_INITIATOR, /*client A will initiate PRP*/
                        HPRole.PRP_RESPONDER); /*client B will only repond to messages from A*/
            } else {
                holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                        HPMechanism.PRP, /*Overall mechanism used*/
                        HPRole.PRP_RESPONDER, /*client A will only repond to messages from B*/
                        HPRole.PRP_INITIATOR);/*client B will initiate PRP*/
            }
            return holePunching;
        }
    }

    private static HolePunching isPrcFeasible(VodAddress clientA, VodAddress clientB) {

        int initiator = 0;
        // prc not possible when neither of the clients have an allocation policy of (PC)
        if (clientA.getAllocationPolicy() != Nat.AllocationPolicy.PORT_CONTIGUITY
                && clientB.getAllocationPolicy() != Nat.AllocationPolicy.PORT_CONTIGUITY) {
            return null;
        } // checking if A can be the initiator
        boolean canABeTheInitiator = false;
        if (clientA.getAllocationPolicy() == Nat.AllocationPolicy.PORT_CONTIGUITY
                && clientA.getFilteringPolicy() != Nat.FilteringPolicy.PORT_DEPENDENT) {
            if (clientA.getMappingPolicy() != Nat.MappingPolicy.PORT_DEPENDENT ||
                    clientB.getFilteringPolicy() != Nat.FilteringPolicy.PORT_DEPENDENT) {
                canABeTheInitiator = true;
            }
        }
        boolean canBBeTheInitiator = false;
        if (clientB.getAllocationPolicy() == Nat.AllocationPolicy.PORT_CONTIGUITY
                && clientB.getFilteringPolicy() != Nat.FilteringPolicy.PORT_DEPENDENT) {
            if (clientB.getMappingPolicy() != Nat.MappingPolicy.PORT_DEPENDENT ||
                    clientA.getFilteringPolicy() != Nat.FilteringPolicy.PORT_DEPENDENT) {
                canBBeTheInitiator = true;
            }
        }

        if (canABeTheInitiator && canBBeTheInitiator) // both can be the initiator. choose the one with lowest id
        {
            if (clientB.getId() < clientA.getId()) {
                initiator = clientB.getId();
            } else {
                initiator = clientA.getId();
            }
        } else if (canABeTheInitiator) {
            initiator = clientA.getId();
        } else if (canBBeTheInitiator) {
            initiator = clientB.getId();
        }


        if (initiator == 0) {
            logger.trace("PRC is not possible between [" + clientA + ", " + clientB + "]");
            return null;
        } else {
            logger.debug("PRC is feasible between [" + clientA + ", " + clientB + "] initiator is (" + initiator + ")");

            HolePunching holePunching = null;
            if (initiator == clientA.getId()) /* if A is initiator then B is responder */ {
                holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                        HPMechanism.PRC, /*Overall mechanism used*/
                        HPRole.PRC_INITIATOR, /*client A will initiate PRP*/
                        HPRole.PRC_RESPONDER); /*client B will only repond to messages from A*/
            } else {
                holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                        HPMechanism.PRC, /*Overall mechanism used*/
                        HPRole.PRC_RESPONDER, /*client B will only repond to messages from A*/
                        HPRole.PRC_INITIATOR);/*client A will initiate PRP*/
            }
            return holePunching;
        }
    }

    private static HolePunching isPrpPrpFeasible(VodAddress clientA, VodAddress clientB) {

        // PRP-PRP only possible when both of the clients have an allocation policy of (pp)
        if (clientA.getAllocationPolicy() == Nat.AllocationPolicy.PORT_PRESERVATION
                && clientB.getAllocationPolicy() == Nat.AllocationPolicy.PORT_PRESERVATION) {
            logger.debug("PRP-PRP is feasible between [" + clientA + ", " + clientB + "]");
            // Both client have a(PP)
            HolePunching holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                    HPMechanism.PRP_PRP, /*Overall mechanism used*/
                    HPRole.PRP_INTERLEAVED,
                    HPRole.PRP_INTERLEAVED);
            return holePunching;
        } else {
            return null;
        }
    }

    private static HolePunching isPrcPrcFeasible(VodAddress clientA, VodAddress clientB) {
        // PRC-PRC not possible when both clients dont have a(pp)
        if (clientA.getAllocationPolicy() == Nat.AllocationPolicy.PORT_CONTIGUITY
                && clientB.getAllocationPolicy() == Nat.AllocationPolicy.PORT_CONTIGUITY) {
            // Both client have a(PP)
            logger.debug("PRC-PRC is feasible between [" + clientA + ", " + clientB + "]");
            HolePunching holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                    HPMechanism.PRC_PRC, /*Overall mechanism used*/
                    HPRole.PRC_INTERLEAVED,
                    HPRole.PRC_INTERLEAVED);
            return holePunching;
        } else {
            return null;
        }
    }

    private static HolePunching isPrpPrcFeasible(VodAddress clientA, VodAddress clientB) {

        // PRP-PRP not possible when both clients dont have a(pp)
        if ((clientA.getAllocationPolicy() == Nat.AllocationPolicy.PORT_PRESERVATION
                && clientB.getAllocationPolicy() == Nat.AllocationPolicy.PORT_CONTIGUITY)
                || (clientB.getAllocationPolicy() == Nat.AllocationPolicy.PORT_PRESERVATION
                && clientA.getAllocationPolicy() == Nat.AllocationPolicy.PORT_CONTIGUITY)) {
            if (clientA.getAllocationPolicy() == Nat.AllocationPolicy.PORT_PRESERVATION) {
                logger.debug("PRP-PRC is feasible between [" + clientA + ", " + clientB + "]");
                HolePunching holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                        HPMechanism.PRP_PRC, /*Overall mechanism used*/
                        HPRole.PRP_INTERLEAVED,
                        HPRole.PRC_INTERLEAVED);
                return holePunching;
            } else if (clientB.getAllocationPolicy() == Nat.AllocationPolicy.PORT_PRESERVATION) {
                // Both client have a(PP)
                logger.debug("PRP-PRC is feasible between [" + clientA + ", " + clientB + "]");
                HolePunching holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                        HPMechanism.PRP_PRC, /*Overall mechanism used*/
                        HPRole.PRC_INTERLEAVED,
                        HPRole.PRP_INTERLEAVED);
                return holePunching;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static boolean isDirectConnectionOverNatPossible(VodAddress client) {
        if (client.isOpen() == false) {
            Nat n = client.getNat();
            return (n.getMappingPolicy() == Nat.MappingPolicy.ENDPOINT_INDEPENDENT &&
                    n.getFilteringPolicy() == Nat.FilteringPolicy.ENDPOINT_INDEPENDENT);
        }
        return false;
    }
    
    private static HolePunching isConnectionReversalFeasible(VodAddress clientA, VodAddress clientB) {

        if (clientA.isOpen() || clientB.isOpen() ||
                isDirectConnectionOverNatPossible(clientA) ||
                isDirectConnectionOverNatPossible(clientB)
                ) {
            if (clientA.isOpen() || isDirectConnectionOverNatPossible(clientA)) {
                logger.debug("One sided HP is feasible between [" + clientA + ", " + clientB + "]");
                HolePunching holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                        HPMechanism.CONNECTION_REVERSAL, /*Overall mechanism used*/
                        HPRole.CONNECTION_REVERSAL_OPEN,
                        HPRole.CONNECTION_REVERSAL_NAT);
                return holePunching;
            } else if (clientB.isOpen() || isDirectConnectionOverNatPossible(clientB)) {
                logger.debug("One sided HP is feasible between [" + clientA + ", " + clientB + "]");
                HolePunching holePunching = new HolePunching(clientA.getId(), clientB.getId(),
                        HPMechanism.CONNECTION_REVERSAL, /*Overall mechanism used*/
                        HPRole.CONNECTION_REVERSAL_NAT,
                        HPRole.CONNECTION_REVERSAL_OPEN);
                return holePunching;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}