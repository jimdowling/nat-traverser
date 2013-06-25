/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.events;

/**
 *
 * @author jdowling
 */
public class PortBindRequest extends DoubleDispatchRequestId<PortBindResponse>

{

    private final int port;

    public PortBindRequest(int id, int port) {
        super(id);
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
