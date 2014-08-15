/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package se.sics.gvod.p2p.orchestrator.distributed;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.network.model.common.NetworkModel;
import se.sics.gvod.timer.CancelPeriodicTimeout;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.Negative;
import se.sics.kompics.PortType;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
import se.sics.kompics.p2p.experiment.dsl.events.KompicsSimulatorEvent;
import se.sics.kompics.p2p.experiment.dsl.events.SimulationTerminatedEvent;
import se.sics.kompics.p2p.experiment.dsl.events.SimulatorEvent;
import se.sics.kompics.p2p.experiment.dsl.events.StochasticProcessEvent;
import se.sics.kompics.p2p.experiment.dsl.events.StochasticProcessStartEvent;
import se.sics.kompics.p2p.experiment.dsl.events.StochasticProcessTerminatedEvent;
import se.sics.kompics.p2p.experiment.dsl.events.TakeSnapshotEvent;
import se.sics.gvod.timer.CancelPeriodicTimeout;
import se.sics.gvod.timer.CancelTimeout;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.Timer;

/**
 * The <code>P2pOrchestrator</code> class.
 * 
=======

/**
 * The <code>P2pOrchestrator</code> class.
 *
>>>>>>> ghettoNat/workAlex
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: P2pOrchestrator.java 1251 2009-09-09 13:25:13Z Cosmin $
 */
public final class DistributedOrchestratorNat extends ComponentDefinition {

    private static Class<? extends PortType> simulationPortType;

    public static void setSimulationPortType(Class<? extends PortType> portType) {
        simulationPortType = portType;
    }
    private static final Logger logger = LoggerFactory.getLogger(DistributedOrchestratorNat.class);
    Negative<?> simulationPort = negative(simulationPortType);
    Negative<VodNetwork> upperNet = negative(VodNetwork.class);
    Positive<VodNetwork> lowerNet = positive(VodNetwork.class);
    Negative<Timer> timer = negative(Timer.class);
    private SimulationScenario scenario;
    private final DistributedOrchestratorNat thisOrchestrator;
    private Random random;
    private NetworkModel networkModel;
    // set of active timers
    private final HashMap<TimeoutId, TimerSignalTaskNat> activeTimers;
    // set of active periodic timers
    private final HashMap<TimeoutId, PeriodicTimerSignalTaskNat> activePeriodicTimers;
    private final java.util.Timer javaTimer;
    private InetAddress myIp;
    private int myPort;

    public DistributedOrchestratorNat(DistributedOrchestratorInit init) {
        activeTimers = new HashMap<TimeoutId, TimerSignalTaskNat>();
        activePeriodicTimers = new HashMap<TimeoutId, PeriodicTimerSignalTaskNat>();
        javaTimer = new java.util.Timer("JavaTimer@"
                + Integer.toHexString(this.hashCode()));
        thisOrchestrator = this;
        doInit(init);

        subscribe(handleStart, control);
        subscribe(handleUpperMessage, upperNet);
        subscribe(handleLowerMessage, lowerNet);
        subscribe(handleST, timer);
        subscribe(handleSPT, timer);
        subscribe(handleCT, timer);
        subscribe(handleCPT, timer);
    }

    public void doInit(DistributedOrchestratorInit init) {
        scenario = init.getScenario();
        random = scenario.getRandom();

        myIp = init.getIp();
        myPort = init.getPort();

        networkModel = init.getNetworkModel();

    }

    public Handler<Start> handleStart = new Handler<Start>() {

        @Override
        public void handle(Start event) {
            // generate initial future events from the scenario
            LinkedList<SimulatorEvent> events = scenario.generateEventList();
            for (SimulatorEvent simulatorEvent : events) {
                long time = simulatorEvent.getTime();
                if (time == 0) {
                    handleSimulatorEvent(simulatorEvent);
                } else {
                    SimulatorEventTaskNat task = new SimulatorEventTaskNat(
                            thisOrchestrator, simulatorEvent);
                    javaTimer.schedule(task, simulatorEvent.getTime());
                }
            }

            logger.info("Orchestration started");
        }
    };
    
    private String pName(SimulatorEvent event) {
        if (event instanceof StochasticProcessEvent) {
            return ((StochasticProcessEvent) event).getProcessName();
        } else if (event instanceof StochasticProcessStartEvent) {
            return ((StochasticProcessStartEvent) event).getProcessName();
        } else if (event instanceof StochasticProcessTerminatedEvent) {
            return ((StochasticProcessTerminatedEvent) event).getProcessName();
        }
        return "";
    }

    private boolean executeEvent(SimulatorEvent event) {
        if (event instanceof StochasticProcessEvent) {
            executeStochasticProcessEvent((StochasticProcessEvent) event);
        } else if (event instanceof StochasticProcessStartEvent) {
            executeStochasticProcessStartEvent((StochasticProcessStartEvent) event);
        } else if (event instanceof StochasticProcessTerminatedEvent) {
            executeStochasticProcessTerminatedEvent((StochasticProcessTerminatedEvent) event);
        } else if (event instanceof KompicsSimulatorEvent) {
            executeKompicsEvent(((KompicsSimulatorEvent) event).getEvent());
        } else if (event instanceof TakeSnapshotEvent) {
            executeTakeSnapshotEvent((TakeSnapshotEvent) event);
        } else if (event instanceof SimulationTerminatedEvent) {
            return executeSimultationTerminationEvent((SimulationTerminatedEvent) event);
        }
        return true;
    }

    private void executeStochasticProcessStartEvent(
            StochasticProcessStartEvent event) {
        if (event.shouldHandleNow()) {
            logger.debug("Started " + pName(event));
            // trigger start events relative to this one
            LinkedList<StochasticProcessStartEvent> startEvents = event.getStartEvents();
            for (StochasticProcessStartEvent startEvent : startEvents) {
                long delay = startEvent.getDelay();
                if (delay > 0) {
                    javaTimer.schedule(new SimulatorEventTaskNat(thisOrchestrator,
                            startEvent), delay);
                } else {
                    handleSimulatorEvent(startEvent);
                }
            }
            // get the stochastic process running
            StochasticProcessEvent stochasticEvent = event.getStochasticEvent();
            handleSimulatorEvent(stochasticEvent);
        }
    }

    private void executeStochasticProcessTerminatedEvent(
            StochasticProcessTerminatedEvent event) {
        logger.debug("Terminated process " + pName(event));
        // trigger start events relative to this process termination
        LinkedList<StochasticProcessStartEvent> startEvents = event.getStartEvents();
        for (StochasticProcessStartEvent startEvent : startEvents) {
            long delay = startEvent.getDelay();
            if (delay > 0) {
                javaTimer.schedule(new SimulatorEventTaskNat(thisOrchestrator,
                        startEvent), delay);
            } else {
                handleSimulatorEvent(startEvent);
            }
        }
        // trigger simulation termination relative to this process termination
        TakeSnapshotEvent snapshotEvent = event.getSnapshotEvent();
        if (snapshotEvent != null) {
            long delay = snapshotEvent.getDelay();
            if (delay > 0) {
                javaTimer.schedule(new SimulatorEventTaskNat(thisOrchestrator,
                        snapshotEvent), delay);
            } else {
                handleSimulatorEvent(snapshotEvent);
            }
        }
        SimulationTerminatedEvent terminatedEvent = event.getTerminationEvent();
        if (terminatedEvent != null) {
            long delay = terminatedEvent.getDelay();
            if (delay > 0) {
                javaTimer.schedule(new SimulatorEventTaskNat(thisOrchestrator,
                        terminatedEvent), delay);
            } else {
                handleSimulatorEvent(terminatedEvent);
            }
        }
    }

    private void executeStochasticProcessEvent(StochasticProcessEvent event) {
        KompicsEvent e = event.generateOperation(random);

        trigger(e, simulationPort);
        logger.debug("{}: {}", pName(event), e);

        if (event.getCurrentCount() > 0) {
            // still have operations to generate, reschedule
            long delay = event.getNextTime();
            if (delay > 0) {
                javaTimer.schedule(new SimulatorEventTaskNat(thisOrchestrator,
                        event), delay);
            } else {
                handleSimulatorEvent(event);
            }
        } else {
            // no operations left. stochastic process terminated
            handleSimulatorEvent(event.getTerminatedEvent());
        }
    }

    private void executeKompicsEvent(KompicsEvent kompicsEvent) {
        // trigger other Kompics events on the simulation port
        logger.debug("KOMPICS_EVENT {}", kompicsEvent.getClass());
        trigger(kompicsEvent, simulationPort);
    }

    private void executeTakeSnapshotEvent(TakeSnapshotEvent event) {
        if (event.shouldHandleNow()) {
            trigger(event.getTakeSnapshotEvent(), simulationPort);
        }
    }

    private boolean executeSimultationTerminationEvent(
            SimulationTerminatedEvent event) {
        if (event.shouldTerminateNow()) {
            logger.info("Orchestration terminated.");
            return false;
        }
        return true;
    }
    Handler<DirectMsg> handleUpperMessage = new Handler<DirectMsg>() {

        @Override
        public void handle(DirectMsg msg) {
            random.nextInt();
            logger.debug("Upper Message recvd: {}", msg);

            if (msg.getDestination().getIp().equals(myIp) && msg.getDestination().getPort() == myPort) {
                if (networkModel != null) {
                    long latency = 300;
                    // TODO - network model for GVodNetwork
//                    long latency = networkModel.getLatencyMs(msg);
                    if (latency > 0) {
                        DelayedMessageTaskNat task = new DelayedMessageTaskNat(
                                thisOrchestrator, msg);
                        javaTimer.schedule(task, latency);
                        return;
                    }
                }
                // we just echo the message on the network port
                trigger(msg, upperNet);

                logger.debug("Loopback Message sent: {}", msg);
            } else {
                // msg is not local, send it to Mina/Netty to remote node
                trigger(msg, lowerNet);
            }
        }
    };
    Handler<DirectMsg> handleLowerMessage = new Handler<DirectMsg>() {

        @Override
        public void handle(DirectMsg event) {
            trigger(event, upperNet);
        }
    };
    Handler<ScheduleTimeout> handleST = new Handler<ScheduleTimeout>() {

        @Override
        public void handle(ScheduleTimeout event) {
            logger.debug("ScheduleTimeout@{} : {}", event.getDelay(), event.getTimeoutEvent());

            if (event.getDelay() < 0) {
                throw new RuntimeException(
                        "Cannot set a negative timeout value.");
            }

            TimeoutId id = event.getTimeoutEvent().getTimeoutId();
            TimerSignalTaskNat timeOutTask = new TimerSignalTaskNat(thisOrchestrator,
                    event.getTimeoutEvent(), id);

            synchronized (activeTimers) {
                activeTimers.put(id, timeOutTask);
            }
            javaTimer.schedule(timeOutTask, event.getDelay());
        }
    };
    Handler<SchedulePeriodicTimeout> handleSPT = new Handler<SchedulePeriodicTimeout>() {

        @Override
        public void handle(SchedulePeriodicTimeout event) {
            logger.debug("SchedulePeriodicTimeout@{} : {}", event.getPeriod(),
                    event.getTimeoutEvent());

            if (event.getDelay() < 0 || event.getPeriod() < 0) {
                throw new RuntimeException(
                        "Cannot set a negative timeout value.");
            }

            TimeoutId id = event.getTimeoutEvent().getTimeoutId();
            PeriodicTimerSignalTaskNat timeOutTask = new PeriodicTimerSignalTaskNat(
                    event.getTimeoutEvent(), thisOrchestrator);

            synchronized (activePeriodicTimers) {
                activePeriodicTimers.put(id, timeOutTask);
            }
            javaTimer.scheduleAtFixedRate(timeOutTask, event.getDelay(), event.getPeriod());
        }
    };
    Handler<CancelTimeout> handleCT = new Handler<CancelTimeout>() {

        @Override
        public void handle(CancelTimeout event) {
            TimeoutId id = event.getTimeoutId();
            logger.debug("CancelTimeout: {}", id);

            TimerSignalTaskNat task = null;
            synchronized (activeTimers) {
                task = activeTimers.get(id);
                if (task != null) {
                    task.cancel();
                    activeTimers.remove(id);
                    logger.debug("canceled timer {}", task.timeout);
                } else {
                    logger.debug("Cannot find timeout {}", id);
                }
            }
        }
    };
    Handler<CancelPeriodicTimeout> handleCPT = new Handler<CancelPeriodicTimeout>() {

        @Override
        public void handle(CancelPeriodicTimeout event) {
            TimeoutId id = event.getTimeoutId();
            logger.debug("CancelPeridicTimeout: {}", id);

            PeriodicTimerSignalTaskNat task = null;
            synchronized (activePeriodicTimers) {
                task = activePeriodicTimers.get(id);
                if (task != null) {
                    task.cancel();
                    activePeriodicTimers.remove(id);
                    logger.debug("canceled periodic timer {}", task.timeout);
                } else {
                    logger.debug("Cannot find periodic timeout {}", id);
                }
            }
        }
    };

    final void timeout(TimeoutId timerId, Timeout timeout) {
        synchronized (activeTimers) {
            activeTimers.remove(timerId);
        }
        logger.debug("trigger timeout {}", timeout);
        trigger(timeout, timer);
    }

    final void periodicTimeout(Timeout timeout) {
        logger.debug("trigger periodic timeout {}", timeout);
        trigger(timeout, timer);
    }

    final void deliverDelayedMessage(RewriteableMsg message) {
        logger.debug("trigger message {}", message);
        trigger(message, upperNet);
    }

    final synchronized void handleSimulatorEvent(SimulatorEvent event) {
        logger.debug("trigger event {}", event);
        if (executeEvent(event) == false) {
            Kompics.shutdown();
            System.exit(0);
        }
    }
}
