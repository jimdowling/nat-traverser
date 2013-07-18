package se.sics.gvod.net.events;

/**
 *
 * @author: Steffen Grohsschmiedt
 */
public class CloseConnectionResponse extends DoubleDispatchResponseId<CloseConnectionRequest> {

    protected CloseConnectionResponse(CloseConnectionRequest request) {
        super(request);
    }
}
