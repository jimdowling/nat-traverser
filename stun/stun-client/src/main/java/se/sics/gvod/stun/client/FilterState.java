/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.client;

/**
 *
 * @author 
 */
enum FilterState {
    UDP_BLOCKED,
        //        STARTED,
        OPEN_CHECK_FIREWALL,
        NAT_UDP_OK,
        CHANGE_IP_TIMED_OUT,
        //        CHANGE_IP_TIMED_OUT_FIN, 
        CHANGE_IP_RECVD,
        //        CHANGE_IP_RECVD_FIN, 
        PORT_CHANGE_RECVD,
        //        PORT_CHANGE_RECVD_FIN,
        PORT_CHANGE_TIMED_OUT,
        //        PORT_CHANGE_TIMED_OUT_FIN,
        FINISHED
}
