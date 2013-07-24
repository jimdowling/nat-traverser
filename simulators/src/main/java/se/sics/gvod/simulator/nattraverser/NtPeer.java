package se.sics.gvod.simulator.nattraverser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.timer.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.util.NatStr;
import se.sics.gvod.hp.msgs.TConnectionMsg;
import se.sics.gvod.nat.traversal.NatTraverserPort;
import se.sics.gvod.nat.traversal.events.HpFailed;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Positive;

/**
 * The
 * <code>Root</code> class
 *
 */
public final class NtPeer extends ComponentDefinition {

    public static int CNT = 0;

    private static final Logger logger = LoggerFactory.getLogger(NtPeer.class);
    Positive<VodNetwork> network = positive(VodNetwork.class);
    Positive<Timer> timer = positive(Timer.class);
    Positive<NatTraverserSimulatorPort> ntsPort = positive(NatTraverserSimulatorPort.class);
    Positive<NatTraverserPort> ntPort = positive(NatTraverserPort.class);
    Map<TimeoutId, Nat> activeMsgs = new HashMap<TimeoutId, Nat>();
    Self self;

//-------------------------------------------------------------------    
    public static class HolePunch extends Timeout {

        private VodAddress dest;
        private String natPair;

        public HolePunch(ScheduleTimeout st, VodAddress dest, String natPair) {
            super(st);
            this.dest = dest;
            this.natPair = natPair;
        }

        public String getNatPair() {
            return natPair;
        }

        public VodAddress getDest() {
            return dest;
        }
    }

//-------------------------------------------------------------------    
    public NtPeer() throws IOException {
        subscribe(handleInit, control);
        subscribe(handlePing, network);
        subscribe(handlePong, network);
        subscribe(handleHolePunch, timer);
        subscribe(handleConnect, ntsPort);
        subscribe(handleHpFailed, ntPort);
    }

//-------------------------------------------------------------------    
    private Handler<NtPeerInit> handleInit = new Handler<NtPeerInit>() {
        public void handle(NtPeerInit event) {
            self = event.getSelf();
        }
    };

//-------------------------------------------------------------------    
    private Handler<Connect> handleConnect = new Handler<Connect>() {
        public void handle(Connect event) {
            ScheduleTimeout st = new ScheduleTimeout(100 * 1000);
            HolePunch hp = new HolePunch(st, event.getDest(), NatStr.pairAsStr(self.getNat(), event.getDest().getNat()));
            st.setTimeoutEvent(hp);
            trigger(st, timer);

            TConnectionMsg.Ping ping = new TConnectionMsg.Ping(self.getAddress(), event.getDest(), hp.getTimeoutId());
            trigger(ping, network);
            
            activeMsgs.put(hp.getTimeoutId(), event.getDest().getNat());
        }
    };

//-------------------------------------------------------------------    
    Handler<HolePunch> handleHolePunch = new Handler<HolePunch>() {
        public void handle(HolePunch msg) {
            trigger(new ConnectionResult(self.getAddress(), msg.getDest(), msg.getNatPair(), false), ntsPort);
            activeMsgs.remove(msg.getTimeoutId());
        }
    };

//-------------------------------------------------------------------    
    public Handler<TConnectionMsg.Ping> handlePing = new Handler<TConnectionMsg.Ping>() {
        public void handle(TConnectionMsg.Ping ping) {
            logger.info("Received ping from " + ping.getSource());
            TConnectionMsg.Pong pong = new TConnectionMsg.Pong(self.getAddress(), ping.getVodSource(), ping.getTimeoutId());
            trigger(pong, network);
        }
    };

//-------------------------------------------------------------------    
    public Handler<TConnectionMsg.Pong> handlePong = new Handler<TConnectionMsg.Pong>() {
        public void handle(TConnectionMsg.Pong pong) {
            logger.debug("pong recvd " + " from " + pong.getSource());
            trigger(new CancelTimeout(pong.getTimeoutId()), timer);
            trigger(new ConnectionResult(self.getAddress(), pong.getVodSource(), NatStr.pairAsStr(self.getNat(), pong.getVodSource().getNat()), true), ntsPort);
            activeMsgs.remove(pong.getTimeoutId());
        }
    };

//-------------------------------------------------------------------    
    private Handler<HpFailed> handleHpFailed = new Handler<HpFailed>() {
        public void handle(HpFailed event) {
            if (activeMsgs.containsKey(event.getMsgTimeoutId())) {
                trigger(new CancelTimeout(event.getMsgTimeoutId()), timer);
                logger.info("HP failed. " + event.getResponseType() + " - " + NatStr.pairAsStr(self.getNat(), activeMsgs.get(event.getMsgTimeoutId())));
                
                VodAddress dest = event.getHpFailedDestNode();
                trigger(new ConnectionResult(self.getAddress(), dest, NatStr.pairAsStr(self.getNat(), dest.getNat()), false), ntsPort);
            }
        }
    };
}
