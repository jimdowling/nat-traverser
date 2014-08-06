/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.hp.rs;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.NatFactory;
import se.sics.gvod.common.SelfNoUtility;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.common.evts.GarbageCleanupTimeout;
import se.sics.gvod.config.RendezvousServerConfiguration;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.hp.events.OpenConnectionResponseType;
import se.sics.gvod.hp.msgs.GoMsg;
import se.sics.gvod.hp.msgs.HpConnectMsg;
import se.sics.gvod.hp.msgs.HpRegisterMsg;
import se.sics.gvod.hp.msgs.HpUnregisterMsg;
import se.sics.gvod.hp.msgs.Interleaved_PRC_OpenHoleMsg;
import se.sics.gvod.hp.msgs.Interleaved_PRC_ServersRequestForPredictionMsg;
import se.sics.gvod.hp.msgs.Interleaved_PRP_ConnectMsg;
import se.sics.gvod.hp.msgs.PRC_OpenHoleMsg;
import se.sics.gvod.hp.msgs.PRC_ServerRequestForConsecutiveMsg;
import se.sics.gvod.hp.msgs.PRP_ConnectMsg;
import se.sics.gvod.hp.msgs.PRP_PreallocatedPortsMsg;
import se.sics.gvod.hp.msgs.SHP_OpenHoleMsg;
import se.sics.gvod.nat.hp.rs.RendezvousServer.NoPortsException;
import se.sics.gvod.nat.hp.rs.events.UnregisterTimeout;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.UUID;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.Start;

/**
 *
 * @author Jim
 */
public class RendezvousServerUnitTest extends VodRetryComponentTestCase {

    private static Logger logger = LoggerFactory.getLogger(RendezvousServerUnitTest.class);
    RendezvousServer zServer = null;
    NatFactory nf = new NatFactory(100);

    public RendezvousServerUnitTest() {
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
        try {
            VodConfig.init(new String[]{});
        } catch (IOException ex) {
            logger.error(null, ex);
        }
        zServer = new RendezvousServer(this,
                new RendezvousServerInit(new SelfNoUtility(getAddress()),
                        new ConcurrentHashMap<Integer, RendezvousServer.RegisteredClientRecord>(),
                        RendezvousServerConfiguration.build().
                        setSessionExpirationTime(30 * 1000).
                        setNumChildren(1)));

        zServer.handleStart.handle(Start.event);
        LinkedList<KompicsEvent> events = pollEvent(1);
        assertSequence(events, GarbageCleanupTimeout.class);

    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
        zServer.stop(null);
    }

    private HpRegisterMsg.Response addPrivateChild(VodAddress client,
            HpRegisterMsg.RegisterStatus expectedStatus, long rtt, Set<Integer> prpPorts) {
        HpRegisterMsg.Request req = new HpRegisterMsg.Request(client, getAddress(),
                rtt, prpPorts);
        zServer.handleHpRegisterRequest.handle(req);
        LinkedList<KompicsEvent> events = pollEvent(1);
        assertSequence(events, HpRegisterMsg.Response.class);
        HpRegisterMsg.Response res = (HpRegisterMsg.Response) events.get(0);
        assert (res.getResponseType() == expectedStatus);
        assert (zServer.registeredClients.size() == 1);
        return res;
    }

    @Test
    public void testZserver() {
        // Register a private node with a parent
        VodAddress src = privAddrs.get(0);
        addPrivateChild(src, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, null);

        // send a request from a node with too high RTT, which will
        // not replace the existing registered client.
        VodAddress src1 = privAddrs.get(1);
        src1.addParent(pubAddrs.get(0).getPeerAddress());
        addPrivateChild(src1, HpRegisterMsg.RegisterStatus.REJECT, 2000, null);

        // kick out worst child, and accept this request
        VodAddress src2 = privAddrs.get(2);
        HpRegisterMsg.Request req2 = new HpRegisterMsg.Request(src2, getAddress(), 500);
        zServer.handleHpRegisterRequest.handle(req2);
        LinkedList<KompicsEvent> events = pollEvent(2);
        assertSequence(events, HpUnregisterMsg.Request.class, HpRegisterMsg.Response.class);
        HpRegisterMsg.Response res = (HpRegisterMsg.Response) events.get(1);
        assert (res.getResponseType() == HpRegisterMsg.RegisterStatus.ACCEPT);
        assert (zServer.registeredClients.size() == 1);

        HpUnregisterMsg.Request un = new HpUnregisterMsg.Request(src2, getAddress(),
                0, HpRegisterMsg.RegisterStatus.BETTER_CHILD);
        zServer.handleHpUnregisterRequestMsg.handle(un);
        events = pollEvent(2);
        assertSequence(events, UnregisterTimeout.class, HpUnregisterMsg.Response.class);
        HpUnregisterMsg.Response unRes = (HpUnregisterMsg.Response) events.get(1);
        assert (unRes.getStatus() == HpUnregisterMsg.Response.Status.SUCCESS);

        un = new HpUnregisterMsg.Request(src1, getAddress(),
                0, HpRegisterMsg.RegisterStatus.BETTER_PARENT);
        zServer.handleHpUnregisterRequestMsg.handle(un);
        events = pollEvent(1);
        assertSequence(events, HpUnregisterMsg.Response.class);
        unRes = (HpUnregisterMsg.Response) events.get(0);
        assert (unRes.getStatus() == HpUnregisterMsg.Response.Status.NOT_REGISTERED);
    }

    @Test
    public void testConnectionReversal() {
        VodAddress client = pubAddrs.get(0);
        VodAddress responder = new VodAddress(privAddrs.get(0).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getEiPpEi());
        addPrivateChild(responder, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, null);
        HpConnectMsg.Request c = new HpConnectMsg.Request(client, getAddress(), responder.getId(),
                1, 1000, UUID.nextUUID());
        zServer.handleHpConnect.handle(c);
        LinkedList<KompicsEvent> events = popEvents();
        assertSequence(events, HpConnectMsg.Response.class, GoMsg.Request.class);
        HpConnectMsg.Response e1 = (HpConnectMsg.Response) events.get(0);
        assert (e1.getVodDestination().equals(client));
        assert (e1.getResponseType() == OpenConnectionResponseType.HP_WILL_START);
        GoMsg.Request e2 = (GoMsg.Request) events.get(1);
        assert (e2.getRemoteId() == client.getId());
        assert (e2.getVodDestination().equals(responder));
    }

    @Test
    public void testShpResponder() {
        VodAddress client = new VodAddress(privAddrs.get(0).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getHdPpEi());
        addPrivateChild(client, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, null);
        VodAddress responder = new VodAddress(privAddrs.get(1).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getPdPcHd());

        HpConnectMsg.Request c = new HpConnectMsg.Request(responder, getAddress(), client.getId(),
                1, 1000, UUID.nextUUID());
        zServer.handleHpConnect.handle(c);

        LinkedList<KompicsEvent> events = popEvents();
        assertSequence(events, HpConnectMsg.Response.class, SHP_OpenHoleMsg.Initiator.class,
                GoMsg.Request.class);

        HpConnectMsg.Response e0 = (HpConnectMsg.Response) events.get(0);
        assert (e0.getVodDestination().equals(responder));
        assert (e0.getResponseType() == OpenConnectionResponseType.HP_WILL_START);

        SHP_OpenHoleMsg.Initiator e1 = (SHP_OpenHoleMsg.Initiator) events.get(1);
        assert (e1.getVodDestination().equals(client));

        GoMsg.Request e2 = (GoMsg.Request) events.get(2);
        assert (e2.getRemoteId() == client.getId());
        assert (e2.getVodDestination().equals(responder));
    }

    @Test
    public void testShpInitiator() {
        VodAddress responder = new VodAddress(privAddrs.get(1).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getHdPpEi());
        addPrivateChild(responder, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, null);
        LinkedList<KompicsEvent> events;

        VodAddress client = new VodAddress(privAddrs.get(0).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getPdPcHd());
        HpConnectMsg.Request c = new HpConnectMsg.Request(client, getAddress(), responder.getId(),
                1, 1000, UUID.nextUUID());
        zServer.handleHpConnect.handle(c);
        events = popEvents();
        assertSequence(events, HpConnectMsg.Response.class, SHP_OpenHoleMsg.Initiator.class,
                GoMsg.Request.class);
        HpConnectMsg.Response e1 = (HpConnectMsg.Response) events.get(0);
        assert (e1.getResponseType() == OpenConnectionResponseType.HP_WILL_START);
        GoMsg.Request e2 = (GoMsg.Request) events.get(2);
        assert (e2.getRemoteId() == responder.getId());
        assert (e2.getVodDestination().equals(client));
    }

    @Test
    public void testPrpResponder() {
        Set<Integer> prpPorts = new HashSet<Integer>();
        prpPorts.add(23445);
        prpPorts.add(55555);
        VodAddress client = new VodAddress(privAddrs.get(0).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getEiPpHd());
        addPrivateChild(client, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, prpPorts);
        LinkedList<KompicsEvent> events;
        try {
            assert (zServer.registeredClients.get(privAddrs.get(0).getId()).popPrpPort() == 55555);
        } catch (NoPortsException ex) {
            assert (false);
        }

        VodAddress responder = new VodAddress(privAddrs.get(1).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getPdRdPd());
        HpConnectMsg.Request c = new HpConnectMsg.Request(responder, getAddress(), client.getId(),
                1, 1000, UUID.nextUUID());

        zServer.handleHpConnect.handle(c);
        events = popEvents();
        assertSequence(events, HpConnectMsg.Response.class,
                PRP_ConnectMsg.Response.class, GoMsg.Request.class,
                PRP_PreallocatedPortsMsg.Request.class);
        HpConnectMsg.Response e1 = (HpConnectMsg.Response) events.get(0);
        assert (e1.getResponseType() == OpenConnectionResponseType.HP_WILL_START);
        PRP_ConnectMsg.Response e3 = (PRP_ConnectMsg.Response) events.get(1);
        assert (e3.getPortToUse() == 23445);
        GoMsg.Request e4 = (GoMsg.Request) events.get(2);
        assert (e4.getRemoteId() == client.getId());
    }

    @Test
    public void testPrpInitiator() {
        VodAddress responder = new VodAddress(privAddrs.get(1).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getPdRdPd());
        addPrivateChild(responder, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, null);
        LinkedList<KompicsEvent> events;

        VodAddress client = new VodAddress(privAddrs.get(0).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getEiPpHd());

        Set<Integer> prpPorts = new HashSet<Integer>();
        prpPorts.add(23445);
        prpPorts.add(55555);
        PRP_ConnectMsg.Request c = new PRP_ConnectMsg.Request(client, getAddress(), responder.getId(),
                prpPorts, UUID.nextUUID());

        zServer.handlePRP_ConnectMsgRequest.handle(c);
        events = pollEvent(2);
        assertSequence(events, PRP_ConnectMsg.Response.class, GoMsg.Request.class);
        PRP_ConnectMsg.Response e1 = (PRP_ConnectMsg.Response) events.get(0);
        assert (e1.getPortToUse() == 55555);
        GoMsg.Request e2 = (GoMsg.Request) events.get(1);
        assert (e2.getRemoteId() == client.getId());
    }

    @Test
    public void testPRP_normal() {
        VodAddress client = privAddrs.get(0);
        VodAddress remote = privAddrs.get(1);
        Set<Integer> setPorts = new HashSet<Integer>();
        setPorts.add(55555);
        PRP_ConnectMsg.Request req
                = new PRP_ConnectMsg.Request(getAddress(), client,
                        remote.getId(), setPorts, UUID.nextUUID());
    }

    @Test
    public void testPrpInterleaved() {
        Set<Integer> prpPorts = new HashSet<Integer>();
        prpPorts.add(23445);
        prpPorts.add(55555);
        VodAddress client = new VodAddress(privAddrs.get(0).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getEiPpPd());
        addPrivateChild(client, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, prpPorts);
        LinkedList<KompicsEvent> events;
        try {
            assert (zServer.registeredClients.get(privAddrs.get(0).getId()).popPrpPort() == 55555);
        } catch (NoPortsException ex) {
            assert (false);
        }

        Set<Integer> prpPorts2 = new HashSet<Integer>();
        prpPorts2.add(32323);
        prpPorts2.add(21212);
        VodAddress responder = new VodAddress(privAddrs.get(1).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getPdPpPd());
        Interleaved_PRP_ConnectMsg.Request reqMsg
                = new Interleaved_PRP_ConnectMsg.Request(responder, getAddress(),
                        client.getId(), prpPorts2, UUID.nextUUID());
        zServer.handle_Interleaved_PRP_ConnectRequest.handle(reqMsg);
        events = popEvents();
        assertSequence(events, Interleaved_PRP_ConnectMsg.Response.class,
                GoMsg.Request.class, GoMsg.Request.class,
                PRP_PreallocatedPortsMsg.Request.class);
        Interleaved_PRP_ConnectMsg.Response e1 = (Interleaved_PRP_ConnectMsg.Response) events.get(0);
        assert (e1.getResponseType() == Interleaved_PRP_ConnectMsg.ResponseType.OK);
        GoMsg.Request e4 = (GoMsg.Request) events.get(1);
        assert (e4.getRemoteId() == client.getId());
        GoMsg.Request e3 = (GoMsg.Request) events.get(2);
        assert (e3.getRemoteId() == responder.getId());
    }

    @Test
    public void testPRC_initiator() {
        VodAddress client = new VodAddress(privAddrs.get(0).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getPdPcPd());
        addPrivateChild(client, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, null);
        LinkedList<KompicsEvent> events;
        VodAddress responder = new VodAddress(privAddrs.get(1).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getEiPcHd());

        HpConnectMsg.Request c = new HpConnectMsg.Request(responder, getAddress(), client.getId(),
                1, 1000, UUID.nextUUID());
        zServer.handleHpConnect.handle(c);
        events = popEvents();
        assertSequence(events, HpConnectMsg.Response.class,
                PRC_ServerRequestForConsecutiveMsg.Request.class);
        HpConnectMsg.Response e1 = (HpConnectMsg.Response) events.get(0);
        assert (e1.getResponseType() == OpenConnectionResponseType.HP_WILL_START);

        PRC_OpenHoleMsg.Request openHoleReqMsg = new PRC_OpenHoleMsg.Request(responder,
                getAddress(), client.getId(), UUID.nextUUID());
        zServer.handle_PRC_OpenHoleMsg.handle(openHoleReqMsg);
        events = popEvents();
        assertSequence(events, PRC_OpenHoleMsg.Response.class,
                GoMsg.Request.class);
        GoMsg.Request eg = (GoMsg.Request) events.get(1);
        assert (eg.getRemoteId() == responder.getId());
    }

    @Test
    public void testPRC_responder() {
        VodAddress client = new VodAddress(privAddrs.get(0).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getEiPcHd());
        addPrivateChild(client, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, null);
        LinkedList<KompicsEvent> events;

        VodAddress responder = new VodAddress(privAddrs.get(1).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getPdPcPd());

        HpConnectMsg.Request c = new HpConnectMsg.Request(responder, getAddress(), client.getId(),
                1, 1000, UUID.nextUUID());
        zServer.handleHpConnect.handle(c);
        events = popEvents();
        assertSequence(events, HpConnectMsg.Response.class,
                PRC_ServerRequestForConsecutiveMsg.Request.class);
        HpConnectMsg.Response e1 = (HpConnectMsg.Response) events.get(0);
        assert (e1.getResponseType() == OpenConnectionResponseType.HP_WILL_START);

        PRC_OpenHoleMsg.Request openHoleReqMsg = new PRC_OpenHoleMsg.Request(client,
                getAddress(), responder.getId(), UUID.nextUUID());
        zServer.handle_PRC_OpenHoleMsg.handle(openHoleReqMsg);
        events = popEvents();
        assertSequence(events, PRC_OpenHoleMsg.Response.class,
                GoMsg.Request.class);
        GoMsg.Request eg = (GoMsg.Request) events.get(1);
        assert (eg.getRemoteId() == client.getId());
    }

    @Test
    public void testPRC_PRC() {
        VodAddress client = new VodAddress(privAddrs.get(0).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getPdPcHd());
        addPrivateChild(client, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, null);
        LinkedList<KompicsEvent> events;
        VodAddress responder = new VodAddress(privAddrs.get(1).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getPdPcPd());

        HpConnectMsg.Request c = new HpConnectMsg.Request(responder, getAddress(), client.getId(),
                1, 1000, UUID.nextUUID());
        zServer.handleHpConnect.handle(c);
        events = popEvents();
        assertSequence(events, HpConnectMsg.Response.class,
                Interleaved_PRC_ServersRequestForPredictionMsg.Request.class,
                Interleaved_PRC_ServersRequestForPredictionMsg.Request.class);
        HpConnectMsg.Response e1 = (HpConnectMsg.Response) events.get(0);
        assert (e1.getResponseType() == OpenConnectionResponseType.HP_WILL_START);

        Interleaved_PRC_OpenHoleMsg.Request open1 = new Interleaved_PRC_OpenHoleMsg.Request(client,
                getAddress(), responder.getId(), UUID.nextUUID());
        zServer.handle_Interleaved_PRC_OpenHoleMsg.handle(open1);
        events = popEvents();
        assert (events.size() == 1);
        assertSequence(events, Interleaved_PRC_OpenHoleMsg.Response.class);

        Interleaved_PRC_OpenHoleMsg.Request open2 = new Interleaved_PRC_OpenHoleMsg.Request(responder,
                getAddress(), client.getId(), UUID.nextUUID());
        zServer.handle_Interleaved_PRC_OpenHoleMsg.handle(open2);
        events = popEvents();
        assert (events.size() == 3);
        assertSequence(events, Interleaved_PRC_OpenHoleMsg.Response.class,
                GoMsg.Request.class, GoMsg.Request.class);
        GoMsg.Request eg = (GoMsg.Request) events.get(1);
        assert (eg.getRemoteId() == responder.getId());
        GoMsg.Request eg2 = (GoMsg.Request) events.get(2);
        assert (eg2.getRemoteId() == client.getId());

    }

    @Test
    public void testPRP_PRC_PrpInitiator() {
        Set<Integer> ports = new HashSet<Integer>();
        ports.add(1212);
        VodAddress client = new VodAddress(privAddrs.get(0).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getEiPpPd());
        addPrivateChild(client, HpRegisterMsg.RegisterStatus.ACCEPT, 1000, ports);
        LinkedList<KompicsEvent> events;
        VodAddress responder = new VodAddress(privAddrs.get(1).getPeerAddress(),
                VodConfig.SYSTEM_OVERLAY_ID, nf.getEiPcPd());
        HpConnectMsg.Request c = new HpConnectMsg.Request(responder, getAddress(), client.getId(),
                1, 1000, UUID.nextUUID());
        zServer.handleHpConnect.handle(c);
        events = popEvents();
        assertSequence(events, HpConnectMsg.Response.class,
                Interleaved_PRC_ServersRequestForPredictionMsg.Request.class);
        HpConnectMsg.Response e1 = (HpConnectMsg.Response) events.get(0);
        assert (e1.getResponseType() == OpenConnectionResponseType.HP_WILL_START);
        ports.add(10202);
        Interleaved_PRP_ConnectMsg.Request open1 = new Interleaved_PRP_ConnectMsg.Request(client,
                getAddress(), responder.getId(), ports, UUID.nextUUID());
        zServer.handle_Interleaved_PRP_ConnectRequest.handle(open1);
        events = popEvents();
        assert (events.size() == 2);
        assertSequence(events, Interleaved_PRP_ConnectMsg.Response.class,
                Interleaved_PRC_ServersRequestForPredictionMsg.Request.class);

    }
}
