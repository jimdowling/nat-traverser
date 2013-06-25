/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.hp;

/**
 *
 * @author Owner
 */
public enum FeasabilityStatus {

    OK, REGISTER_FIRST_THEN_CHECK_FEASIBILITY, REMOTE_PEER_NOT_REGISTERED,
    NAT_COMBINATION_NOT_TRAVERSABLE, SESSION_ALREADY_EXISTS, BOTH_PEERS_OPEN
}