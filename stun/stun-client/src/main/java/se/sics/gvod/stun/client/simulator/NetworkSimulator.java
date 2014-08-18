package se.sics.gvod.stun.client.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Start;

public class NetworkSimulator extends ComponentDefinition {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    Negative<VodNetwork> upperNet = negative(VodNetwork.class);

    public NetworkSimulator(NetworkSimulatorInit init) {
        doInit(init);
        subscribe(handleStart, control);
        subscribe(handleUpperMessage, upperNet);
    }

    private void doInit(NetworkSimulatorInit init) {
    }

    private Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            logger.trace("Simple Network Simulator Started");
        }
    };

    private Handler<DirectMsg> handleUpperMessage = new Handler<DirectMsg>() {
        @Override
        public void handle(DirectMsg msg) {
            logger.debug(msg.getClass().getCanonicalName() + " - " + msg.getVodSource() + " dest: " + msg.getVodDestination());
            trigger(msg, upperNet);
        }
    };

};
