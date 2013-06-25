package se.sics.gvod.nat.common;

import se.sics.kompics.AutoSubscribeComponent;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.gvod.net.VodNetwork;

/**
 * The <code>Gradient</code> class is component implementing the Gradient protocol.
 * 
 * 
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: Gradient.java 1218 2009-09-06 21:31:02Z Cosmin $
 */
public final class Server extends AutoSubscribeComponent {

    private Negative<VodNetwork> net = negative(VodNetwork.class);
    private int count = 0;
    private long startTime = System.currentTimeMillis();

    public Server() {
        autoSubscribe();
    }
    private Handler<TestMsg> handleTestMessage = new Handler<TestMsg>() {

        @Override
        public void handle(TestMsg event) {
            count++;

            if (count == 2) {
                System.out.println("Received TestMsgId. Count == " + count
                        + "  Time == " + ((System.currentTimeMillis() - startTime)));
                trigger(new TestMsgId(event.getVodSource(), event.getVodDestination(),
                        event.getTimeoutId()), net);
            } else {
                System.out.println("TestMsgId: Ignored count == " + count
                        + "  Time == " + (System.currentTimeMillis() - startTime));
            }
            startTime = System.currentTimeMillis();
        }
    };
    private Handler<TestMsgId> handleTestIdMessage = new Handler<TestMsgId>() {

        public void handle(TestMsgId event) {

            count++;

            if (count == 5) {
                System.out.println("Received TestMsgId. Count == " + count
                        + "  Time == " + (System.currentTimeMillis() - startTime));
                trigger(new TestMsgId(event.getVodSource(), event.getVodDestination(),
                        event.getTimeoutId()), net);
            } else {
                System.out.println("TestMsgId: Ignored count == " + count
                        + "  Time == " + (System.currentTimeMillis() - startTime));
            }
            startTime = System.currentTimeMillis();
        }
    };
}
