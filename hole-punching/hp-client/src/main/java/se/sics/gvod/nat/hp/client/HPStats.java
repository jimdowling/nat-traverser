/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.hp.client;

/**
 *
 * @author salman
 */
public class HPStats
{
    private int successCounter;
    private int startCounter;

    public HPStats()
    {
        this.successCounter = 0;
        this.startCounter = 0;
    }

    public void incrementStartCounter()
    {
        startCounter++;
    }

    public void incrementSuccessCounter()
    {
        successCounter++;
    }

    public int getStartCounter()
    {
        return startCounter;
    }

    public int getSuccessCounter()
    {
        return successCounter;
    }

    public void addStartCounter(int startCounter)
    {
        this.startCounter += startCounter;
    }

    public void addSuccessCounter(int successCounter)
    {
        this.successCounter += successCounter;
    }
}
