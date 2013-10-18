package se.sics.gvod.simulator.nattraverser;

import java.util.HashMap;
import java.util.Map;
import se.sics.kompics.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.Self;
import se.sics.gvod.common.util.NatStr;
import se.sics.gvod.hp.msgs.TConnectionMsg;
import se.sics.gvod.nat.common.MsgRetryComponent;
import se.sics.gvod.nat.traversal.NatTraverserPort;
import se.sics.gvod.nat.traversal.events.HpFailed;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;

/**
 * The
 * <code>Root</code> class
 *
 */
public final class NtPeer extends MsgRetryComponent {

    public static int CNT = 0;

    private static final Logger logger = LoggerFactory.getLogger(NtPeer.class);
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
    public NtPeer() {
        this(null);
    }
    
    public NtPeer(RetryComponentDelegator delegator) {
        super(delegator);
        this.delegator.doAutoSubscribe();
    }

//-------------------------------------------------------------------    
    private Handler<NtPeerInit> handleInit = new Handler<NtPeerInit>() {
        @Override
        public void handle(NtPeerInit event) {
            self = event.getSelf();
        }
    };

//-------------------------------------------------------------------    
    private Handler<Connect> handleConnect = new Handler<Connect>() {
        @Override
        public void handle(Connect event) {
            ScheduleTimeout st = new ScheduleTimeout(100 * 1000);
            HolePunch hp = new HolePunch(st, event.getDest(), NatStr.pairAsStr(self.getNat(), event.getDest().getNat()));
            st.setTimeoutEvent(hp);
            delegator.doTrigger(st, timer);

            TConnectionMsg.Ping ping = new TConnectionMsg.Ping(self.getAddress(), event.getDest(), hp.getTimeoutId());
            delegator.doRetry(ping);
            
            activeMsgs.put(hp.getTimeoutId(), event.getDest().getNat());
        }
    };

//-------------------------------------------------------------------    
    Handler<HolePunch> handleHolePunch = new Handler<HolePunch>() {
        @Override
        public void handle(HolePunch msg) {
            delegator.doTrigger(new ConnectionResult(self.getAddress(), msg.getDest(), msg.getNatPair(), false), ntsPort);
            activeMsgs.remove(msg.getTimeoutId());
        }
    };

//-------------------------------------------------------------------    
    public Handler<TConnectionMsg.Ping> handlePing = new Handler<TConnectionMsg.Ping>() {
        @Override
        public void handle(TConnectionMsg.Ping ping) {
            logger.debug("Received ping from " + ping.getSource());
            TConnectionMsg.Pong pong = new TConnectionMsg.Pong(self.getAddress(), ping.getVodSource(), ping.getTimeoutId());
            delegator.doTrigger(pong, network);
        }
    };

//-------------------------------------------------------------------    
    public Handler<TConnectionMsg.Pong> handlePong = new Handler<TConnectionMsg.Pong>() {
        @Override
        public void handle(TConnectionMsg.Pong pong) {
            logger.debug("pong recvd " + " from " + pong.getSource());
            delegator.doTrigger(new CancelTimeout(pong.getTimeoutId()), timer);
            delegator.doTrigger(new ConnectionResult(self.getAddress(), pong.getVodSource(), NatStr.pairAsStr(self.getNat(), pong.getVodSource().getNat()), true), ntsPort);
            activeMsgs.remove(pong.getTimeoutId());
        }
    };

//-------------------------------------------------------------------    
    private Handler<HpFailed> handleHpFailed = new Handler<HpFailed>() {
        @Override
        public void handle(HpFailed event) {
            if (activeMsgs.containsKey(event.getMsgTimeoutId())) {
                delegator.doTrigger(new CancelTimeout(event.getMsgTimeoutId()), timer);
                logger.debug("HP failed. " + event.getResponseType() + " - " 
                        + NatStr.pairAsStr(self.getNat(), activeMsgs.get(event.getMsgTimeoutId())));
                
                VodAddress dest = event.getHpFailedDestNode();
                delegator.doTrigger(new ConnectionResult(self.getAddress(), dest, NatStr.pairAsStr(self.getNat(), dest.getNat()), false), ntsPort);
            }
        }
    };

    @Override
    public void stop(Stop event) {
        return;
    }

}
