package se.sics.gvod.stun.client.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Start;

public class NetworkSimulator extends ComponentDefinition
{
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    Negative<VodNetwork> upperNet = negative(VodNetwork.class);

    public NetworkSimulator()
    {
        subscribe(handleStart, control);
        subscribe(handleUpperMessage, upperNet);
        subscribe(handleSimulatorInit, control);
    }

    private Handler<Start> handleStart = new Handler<Start>()
    {
        @Override
        public void handle(Start event)
        {
            logger.trace("Simple Network Simulator Started");
        }
    };

    private Handler<VodMsg> handleUpperMessage = new Handler<VodMsg>()
    {
        @Override
        public void handle(VodMsg msg)
        {
            logger.debug(msg.getClass().getCanonicalName() + " - " + msg.getVodSource()+" dest: "+msg.getVodDestination());
            trigger(msg, upperNet);
        }
    };

    private Handler<NetworkSimulatorInit> handleSimulatorInit = new Handler<NetworkSimulatorInit>()
    {
        @Override
        public void handle(NetworkSimulatorInit event)
        {
        }
    };
};