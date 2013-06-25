/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.events;


import java.util.Set;



/**
 *
 * @author jdowling
 */
public class PortDeleteRequest extends DoubleDispatchRequestId<PortDeleteResponse>
{

    private final Set<Integer> portsToDelete;

    public PortDeleteRequest(int id, Set<Integer> portsToDelete) {
        super(id);
        this.portsToDelete = portsToDelete;
    }


     public Set<Integer> getPortsToDelete() {
        return portsToDelete;
    }

}
