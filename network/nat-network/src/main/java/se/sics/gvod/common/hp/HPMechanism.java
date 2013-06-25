/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.hp;

/**
 *
 * @author jdowling
 */
public enum HPMechanism            
{
    SHP,
    PRP,
    PRC,
    PRP_PRP, /* Both clients use PRP*/
    PRC_PRC, /* Both clients use RPC */
    PRP_PRC, /* One client uses RPC while other client uses PRP*/
    CONNECTION_REVERSAL,  /*  used when one peer is behind the nat and the other peer is OPEN*/
    NOT_POSSIBLE /* Cannot traverse these 2 NAT Types*/, 
    NONE  /* Some other error means that we don't select a mechanism*/
}
 