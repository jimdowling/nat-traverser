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
package se.sics.gvod.common;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Nat;
import se.sics.kompics.Event;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Port;
import se.sics.kompics.PortType;
import se.sics.kompics.Positive;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.ipasdistances.AsIpGenerator;
import se.sics.kompics.Channel;
import se.sics.kompics.ChannelFilter;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;

/**
 *
 * @author jim
 */
public abstract class VodComponentTestCase extends Assert implements ComponentDelegator, Self {

    private static Logger logger = LoggerFactory.getLogger(VodComponentTestCase.class);
    protected InetAddress selfAddress;
    protected VodAddress self;
    protected VodDescriptor selfDesc;
    protected LinkedList<Event> eventList;
    protected Semaphore eventSemaphore;
    protected Utility utility;
    private int nodeId = 0;
    protected AsIpGenerator ipGenerator = AsIpGenerator.getInstance(10);
    protected List<VodAddress> privAddrs = new ArrayList<VodAddress>();
    protected List<VodDescriptor> privDescs = new ArrayList<VodDescriptor>();
    protected List<VodAddress> pubAddrs = new ArrayList<VodAddress>();
    protected List<VodDescriptor> pubDescs = new ArrayList<VodDescriptor>();
    protected int numAddrs = 0;

    protected VodComponentTestCase() {
        int overlayId = 1;
        eventList = new LinkedList<Event>();
        eventSemaphore = new Semaphore(0);
        utility = new UtilityVod(0);
        selfAddress = ipGenerator.generateIP();
        self = new VodAddress(new Address(selfAddress, 8081, 0), overlayId);
        selfDesc = new VodDescriptor(self, utility, 0, 0);
        NatFactory f = new NatFactory(5);
        Nat n = f.getOpenNat();
        SelfFactory.setNat(nodeId, n);
        for (Nat nat : f.getAllNats()) {
            InetAddress ip = ipGenerator.generateIP();
            int port = 10000 + numAddrs;
            int id = 100 + numAddrs;
            VodAddress va = new VodAddress(new Address(ip, port, id), id, nat);
            assert (va.getNat().compareTo(nat) == 0);
            privAddrs.add(va);
            privDescs.add(new VodDescriptor(va, new UtilityVod(numAddrs), 0, 1500));
            ip = ipGenerator.generateIP();
            // public address
            port = 20000 + numAddrs;
            id = 20000 + numAddrs;
            va = new VodAddress(new Address(ip, port, id), id);
            pubAddrs.add(va);
            pubDescs.add(new VodDescriptor(va, new UtilityVod(numAddrs), 0, 1500));
            numAddrs++;
        }
    }

    @Before
    public void setUp() {
        try {
            VodConfig.init(new String[]{""});
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(VodComponentTestCase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @After
    public void tearDown() {
    }

    @Override
    public <P extends PortType> void doTrigger(Event event, Port<P> port) {
        if (event instanceof SchedulePeriodicTimeout) {
            SchedulePeriodicTimeout timeout = (SchedulePeriodicTimeout) event;
            event = timeout.getTimeoutEvent();
        } else if (event instanceof ScheduleTimeout) {
            ScheduleTimeout timeout = (ScheduleTimeout) event;
            event = timeout.getTimeoutEvent();
        }
        eventList.add(event);
        eventSemaphore.release();
    }

    @Override
    public <E extends Event, P extends PortType> void doSubscribe(Handler<E> handler, Port<P> port) {
    }

    @Override
    public <P extends PortType> Negative<P> getNegative(Class<P> portType) {
        return null;
    }

    @Override
    public <P extends PortType> Positive<P> getPositive(Class<P> portType) {
        return null;
    }

    /**
     * Pull a number of events from the FIFO eventBuffer
     * @param num the number of events to pull
     * @return a list containing the events
     */
    protected LinkedList<Event> pollEvent(int num) {
        try {
            if (!eventSemaphore.tryAcquire(num, 1, TimeUnit.SECONDS)) {
                logger.error("Number of expected messages was not generated.");
                assert false;
            }
        } catch (InterruptedException ex) {
            logger.error(null, ex);
        }

        LinkedList<Event> events = new LinkedList<Event>();

        for (int i = 0; i < num; i++) {
            events.add(eventList.poll());
        }

        return events;
    }

    protected LinkedList<Event> popEvents() {

        int numEvts = eventSemaphore.drainPermits();

        LinkedList<Event> events = new LinkedList<Event>();

        for (int i = 0; i < numEvts; i++) {
            events.add(eventList.poll());
        }

        return events;
    }

    public void assertSequence(LinkedList<Event> events, Class... eventTypes) {
        for (int i = 0; i < eventTypes.length; i++) {
            Class<? extends Event> clazz = events.get(i).getClass();
            Class<? extends Event> eventClass = eventTypes[i];
            if (clazz != eventClass) {
                System.err.println("Expected event " + eventClass.getName() + " while it is " + clazz.getName());
                assert (false);
            }
        }
        assert true;
    }

    @Override
    public VodAddress getAddress() {
        return self;
    }

    @Override
    public Utility getUtility() {
        return utility;
    }

    @Override
    public int getId() {
        return self.getId();
    }

    @Override
    public int getOverlayId() {
        return self.getOverlayId();
    }

    @Override
    public int getPort() {
        return self.getPort();
    }

    @Override
    public InetAddress getIp() {
        return self.getIp();
    }

    @Override
    public Nat getNat() {
        return self.getNat();
    }

    @Override
    public VodDescriptor getDescriptor() {
        return selfDesc;
    }

    @Override
    public void updateUtility(Utility utility) {
        this.utility = utility;
    }

    @Override
    public Set<Address> getParents() {
        return self.getParents();
    }

    @Override
    public boolean removeParent(Address parent) {
        return SelfFactory.removeParent(self.getId(), parent);
    }

    @Override
    public void addParent(Address parent) {
        SelfFactory.addParent(self.getId(), parent);
    }

    @Override
    public void setNat(Nat nat) {
        this.self = new VodAddress(self.getPeerAddress(), self.getOverlayId(), nat);
    }

    @Override
    public Self clone(int overlayId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isOpen() {
        return self.isOpen();
    }

    @Override
    public <P extends PortType> Channel<P> doConnect(
            Positive<P> positive, Negative<P> negative) {
        return null;
    }

    @Override
    public <P extends PortType> Channel<P> doConnect(
            Negative<P> negative, Positive<P> positive) {
        return null;
    }

    @Override
    public <P extends PortType> Channel<P> doConnect(Positive<P> positive,
            Negative<P> negative, ChannelFilter<?, ?> filter) {
        return null;
    }

    @Override
    public <P extends PortType> Channel<P> doConnect(Negative<P> negative,
            Positive<P> positive, ChannelFilter<?, ?> filter) {
        return null;
    }

    @Override
    public <P extends PortType> void doDisconnect(Negative<P> negative,
            Positive<P> positive) {
        // TODO - tell cosmin that disconnect should return a portType
        return;
    }

    @Override
    public <P extends PortType> void doDisconnect(Positive<P> positive,
            Negative<P> negative) {
        // TODO - tell cosmin that disconnect should return a portType
        return;
    }

    @Override
    public Component doCreate(Class<? extends ComponentDefinition> definition) {
        return null;
    }

    @Override
    public boolean isUnitTest() {
        return true;
    }    
    
    @Override
    public void setIp(InetAddress ip) {
        SelfFactory.setIp(getId(), ip);
    }

    @Override
    public boolean isUpnp() {
        return false;
    }

    @Override
    public void setUpnpIp(InetAddress ip) {
        SelfFactory.setUpnpIp(getId(), ip);
    }    

    @Override
    public void setUpnp(boolean enabled) {
        ; // do nothing
    }

}
