/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.hp;

/**
 *
 * @author Salman
 */

public enum HPRole  // Hole Punching Role
{

    SHP_RESPONDER, SHP_INITIATOR, /* one client will do the SHP while other will wait for the SHP messages to recv and then respond*/
    PRP_RESPONDER, PRP_INITIATOR, /* same one client will initiate PRP while other will respond to that*/
    PRC_RESPONDER, PRC_INITIATOR,  /* same as above */
    PRP_INTERLEAVED, /* also for PRP_PRC_INTERLEAVED */
    PRC_INTERLEAVED,
    CONNECTION_REVERSAL_OPEN,        /* open peer mechanism */
    CONNECTION_REVERSAL_NAT,       /* natted peer mechanism */
}

