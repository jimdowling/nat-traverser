/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import se.sics.gvod.timer.TimeoutId;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.gvod.address.Address;
import se.sics.gvod.timer.Timer;

/**
 *
 * @author jdowling
 */
public class Ping extends ComponentDefinition {

    Negative<PingPort> pingPort = negative(PingPort.class);
    Positive<Timer> timer = positive(Timer.class);
    private Map<TimeoutId, PingRequest> outstandingRequests =
            new HashMap<TimeoutId, PingRequest>();

    public Ping() {


        subscribe(handlePingRequest, pingPort);
    }
    Handler<PingRequest> handlePingRequest = new Handler<PingRequest>() {

        @Override
        public void handle(PingRequest message) {
            outstandingRequests.put(message.getId(), message);

            final TimeoutId id = message.getId();
            final Address addr = message.getAddr();
            final long startTime = System.currentTimeMillis();
            final int pingTimeout = message.getPingTimeout();
            (new Thread() {

                @Override
                public void run() {

                    boolean alive = false;

                    try {
                        // This will send an ICMP packet for Windows 7
                        // For WinXP, think it will send TCP connect to port 7.
                        // For Linux, without root privileges, it will send
                        // a TCP connect to port 7.
                        alive = addr.getIp().isReachable(pingTimeout);
                    } catch (IOException ex) {
                        Logger.getLogger(Ping.class.getName()).log(Level.SEVERE, "Problem in Ping.isReachable() to "
                                + addr + " with pingTimeout=" + 
                                pingTimeout, ex);
                    }
                    long timeTaken = alive ? System.currentTimeMillis() - startTime : pingTimeout;
                    sendResponse(id, alive, timeTaken);
                }
            }).start();
        }
    };

//    In Linux/Unix you may have to suid the java executable to get ICMP Ping working,
//    ECHO REQUESTs will be fine even without suid. However on Windows you can get ICMP Ping
//    without any issues whatsoever.
    public synchronized void sendResponse(TimeoutId id, boolean alive, long timeTaken) {
        PingRequest req = outstandingRequests.get(id);
        PingResponse response = new PingResponse(req, timeTaken, alive);
        trigger(response, pingPort);
        outstandingRequests.remove(id);
    }
}
