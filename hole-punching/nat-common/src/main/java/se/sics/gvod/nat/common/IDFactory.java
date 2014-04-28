/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.common;

/**
 *
 * @author Salman
 */
public class IDFactory
{
    private int currentNatID;
    private int currentPeerID;
    public IDFactory()
    {
        currentNatID = 10000; // nat ids start from 10000 +1, +2, +3
        currentPeerID = 0;    // peer ids start form 1 2,3,4, bla bla
    }

    public int getNewNatID()
    {
        return ++currentNatID;
    }

    public int getNewPeerID()
    {
        return ++currentPeerID;
    }
}
