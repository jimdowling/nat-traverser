package se.sics.gvod.nat.common;

import se.sics.gvod.net.VodNetwork;
import se.sics.kompics.AutoSubscribeComponent;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;

/**
 * The <code>Gradient</code> class is component implementing the Gradient protocol.
 * 
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: Gradient.java 1218 2009-09-06 21:31:02Z Cosmin $
 */
public final class TimeoutTestServerFailure extends AutoSubscribeComponent {

    private Negative<VodNetwork> net = negative(VodNetwork.class);
    private int count = 0;
    private long startTime = System.currentTimeMillis();

    public TimeoutTestServerFailure() {
        autoSubscribe();
    }
    private Handler<TMessage.RequestMsg> handleTMessage = new Handler<TMessage.RequestMsg>() {

        @Override
        public void handle(TMessage.RequestMsg event)
        {
            System.out.println("Server recvd Test Message. Time sec: "
                    +((double)(System.currentTimeMillis()-startTime))/1000d
                    +" timeout id "+event.getTimeoutId());
            TMessage.ResponseMsg response = new TMessage.ResponseMsg(
                    event.getVodDestination(), event.getVodSource(), event.getTimeoutId());
            // dont send the response back.
            //trigger(response, net);
        }
    };
   
}
