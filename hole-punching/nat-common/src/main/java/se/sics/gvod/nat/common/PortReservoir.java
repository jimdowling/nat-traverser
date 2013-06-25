/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.common;

/**
 *
 * @author jdowling
 */
public interface PortReservoir {

    int getAvailableRandomPort();

    void releasePort(int port);

}
