/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.hp;

/**
 *
 * @author Salman
 */
public class HPSessionKey
{

    private final int client_A_ID;
    private final int client_B_ID;

    public HPSessionKey(int client_A_ID, int client_B_ID)
    {
        if (client_A_ID < client_B_ID)
        {
            this.client_A_ID = client_A_ID;
            this.client_B_ID = client_B_ID;
        }
        else
        {
            this.client_A_ID = client_B_ID;
            this.client_B_ID = client_A_ID;
        }
    }

    public int getClient_A_ID()
    {
        return client_A_ID;
    }

    public int getClient_B_ID()
    {
        return client_B_ID;
    }

    /**
     * Symmetric equality operator.
     * (a,b) == (b,a)
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final HPSessionKey other = (HPSessionKey) obj;
        if ((this.client_A_ID == other.getClient_A_ID()
                && this.client_B_ID == other.getClient_B_ID())
                || 
                (
             this.client_B_ID == other.getClient_A_ID()
                && this.client_A_ID == other.getClient_B_ID()                
                )
                )
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    @Override
    public int hashCode()
    {
        return this.client_A_ID + this.client_B_ID;
    }

    @Override
    public String toString()
    {
        return "["+ client_A_ID + ", " + client_B_ID + "]";
    }

}
