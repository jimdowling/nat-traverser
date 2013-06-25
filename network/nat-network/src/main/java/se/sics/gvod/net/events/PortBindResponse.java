/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.events;

/**
 *
 * @author jdowling
 */
public abstract class PortBindResponse extends DoubleDispatchResponseId<PortBindRequest>
{
    public static enum Status { SUCCESS, FAIL, PORT_ALREADY_BOUND };
    private final int port;
    private Status status;
    
    
    public PortBindResponse(PortBindRequest request) {
        super(request);
        this.port = request.getPort();
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public int getPort() {
        return port;
    }
}
