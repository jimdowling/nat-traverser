/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.hp.client;

import se.sics.gvod.config.HpClientConfiguration;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.sics.gvod.common.evts.GarbageCleanupTimeout;
import se.sics.gvod.hp.events.OpenConnectionResponseType;
import se.sics.gvod.hp.msgs.DeleteConnectionMsg;
import se.sics.gvod.hp.msgs.HolePunchingMsg;
import se.sics.gvod.hp.msgs.HpConnectMsg;
import se.sics.gvod.nat.hp.client.events.DeleteConnection;
import se.sics.gvod.nat.hp.client.events.OpenConnectionRequest;
import se.sics.gvod.nat.hp.client.events.OpenConnectionResponse;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.timer.UUID;
import se.sics.kompics.Event;
import se.sics.gvod.common.hp.HPRole;
import se.sics.gvod.common.hp.HolePunching;
import se.sics.gvod.common.hp.HpFeasability;

/**
 *
 * @author Owner
 */
public class HpClientUnitTest extends VodRetryComponentTestCase {

    HpClient hpClient = null;
    int scanRetries = 1;
    boolean scanningEnabled = true;
    ConcurrentHashMap<Integer, OpenedConnection> connections;
    LinkedList<Event> events;

    public HpClientUnitTest() {
        super();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        hpClient = new HpClient(this);

        connections = new ConcurrentHashMap<Integer, OpenedConnection>();
        hpClient.handleInit.handle(new HpClientInit(this, connections, 
                        HpClientConfiguration.build().
                        setScanRetries(scanRetries).
                        setScanningEnabled(scanningEnabled).
                        setSessionExpirationTime(30*1000).
                        setRto(4000)                
                ));
        events = pollEvent(1);
        assertSequence(events, GarbageCleanupTimeout.class);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testOpenConnection() {
        privAddrs.get(0).addParent(pubAddrs.get(0).getPeerAddress());
        hpClient.handleOpenConnectionRequest.handle(
                new OpenConnectionRequest(privAddrs.get(0), true, true,
                UUID.nextUUID()));
        assert (hpClient.openedConnections.isEmpty());
        assert (hpClient.hpSessions.size() == 1);
//        events = pollEvent(1);
//        assertSequence(events, HpConnectMsg.Request.class);

        HolePunching hp = HpFeasability.isPossible(getAddress(), privAddrs.get(0));
        if (hp != null) {
            events = pollEvent(privAddrs.get(0).getParents().size());
            HPRole r = hp.getHolePunchingRoleOf(getId());
            if (r == HPRole.SHP_INITIATOR) {
                assertSequence(events, HpConnectMsg.class);
//                assertSequence(events, PortAllocRequest.class);
//                for (Event ev : events) {
//                    PortAllocRequest par = (PortAllocRequest) ev;
//                    SHP_PortResponse resp = new SHP_PortResponse(par,
//                            getId());
//                    Set<Integer> ports = new HashSet<Integer>();
//                    ports.add(50555);
//                    ports.add(50556);
//                    resp.setAllocatedPorts(ports);
//                    hpClient.handleSHP_PortResponse.handle(resp);
//                    LinkedList<Event> nestedEvents;
//                    nestedEvents = pollEvent(1);
//                    assertSequence(nestedEvents, SHP_OpenHoleMsg.Request.class);
//                }

            } else if (r == HPRole.PRP_INITIATOR) {
                assertSequence(events, HpConnectMsg.class);
//                assertSequence(events, PortAllocRequest.class);
//                for (Event ev : events) {
//                    PortAllocRequest par = (PortAllocRequest) ev;
//                    PRP_PortResponse resp = new PRP_PortResponse(par,
//                            getId());
//                    Set<Integer> ports = new HashSet<Integer>();
//                    ports.add(50555);
//                    ports.add(50556);
//                    resp.setAllocatedPorts(ports);
//                    hpClient.handlePRP_PortResponse.handle(resp);
//                    LinkedList<Event> nestedEvents;
//                    nestedEvents = pollEvent(1);
//                    assertSequence(nestedEvents,
//                            PRP_ConnectMsg.Request.class);
//                }
            } else {
//                 || r == HPRole.PRP_INTERLEAVED
                assertSequence(events, HpConnectMsg.Request.class);
            }
        } else {
            events = pollEvent(1);
            assertSequence(events, OpenConnectionResponse.class);
        }

        hpClient.handleOpenConnectionRequest.handle(
                new OpenConnectionRequest(privAddrs.get(0), false, true, UUID.nextUUID()));
        events = pollEvent(1);
        assertSequence(events, HpConnectMsg.Request.class);

        hpClient.handleHolePunchingMsgRequest.handle(
                new HolePunchingMsg.Request(privAddrs.get(0), getAddress(), 
                        UUID.nextUUID()));
        assert (hpClient.openedConnections.size() == 1);
        events = pollEvent(2);
        assertSequence(events, 
                HolePunchingMsg.Response.class
                , OpenConnectionResponse.class);
        HolePunchingMsg.Response r = (HolePunchingMsg.Response) events.get(0);
        hpClient.handleHolePunchingResponseAck.handle(
                new HolePunchingMsg.ResponseAck(privAddrs.get(0), getAddress(), 
                r.getTimeoutId(), UUID.nextUUID()));
        assert (hpClient.openedConnections.size() == 1);

        OpenConnectionResponse ocr = (OpenConnectionResponse) events.get(1);
        assert(ocr.getResponseType() == OpenConnectionResponseType.OK);

        Integer k = privAddrs.get(0).getId();
        hpClient.handleDeleteConnectionRequest.handle(new DeleteConnection(k));
        events = pollEvent(1);
        assertSequence(events, DeleteConnectionMsg.class);
        assert (hpClient.openedConnections.isEmpty());


    }

    @Test
    public void testResponderHp() {


        HolePunching hp = HpFeasability.isPossible(getAddress(), privAddrs.get(0));
//        if (hp != null) {
//
//            hpClient.handleInitiateSimpleHolePunchingRequest.handle(
//                    new SHP_InitiateSimpleHolePunchingMsg.Request(pubAddrs.get(0), getAddress(),
//                    privAddrs.get(0).getId(), HPMechanism.SHP,
//                    HPRole.SHP_INITIATOR));
//            events = pollEvent(1);
//            assertSequence(events, PortAllocRequest.class);
//            for (Event ev : events) {
//                PortAllocRequest par = (PortAllocRequest) ev;
//                SHP_PortResponse resp = new SHP_PortResponse(par,
//                        privAddrs.get(0).getId());
//                Set<Integer> ports = new HashSet<Integer>();
//                ports.add(50555);
//                ports.add(50556);
//                resp.setAllocatedPorts(ports);
//                hpClient.handleSHP_PortResponse.handle(resp);
//                LinkedList<Event> nestedEvents;
//                nestedEvents = pollEvent(1);
//                assertSequence(nestedEvents, SHP_OpenHoleMsg.Request.class);
//            }
//
//
//        }
    }

    @Test
    public void testGetStats() {
        hpClient.handleGetHPStatsRequest.handle(new GetHPStatsRequest(hpClient));
        events = pollEvent(1);
        assertSequence(events, GetHPStatsResponse.class);
    }
}
