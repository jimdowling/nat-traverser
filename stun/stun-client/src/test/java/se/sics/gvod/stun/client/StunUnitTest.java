/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.client;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.VodRetryComponentTestCase;
import se.sics.gvod.config.StunClientConfiguration;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import static se.sics.gvod.stun.client.StunClientTest.StunClientComponentTester.ruleLifeTime;
import se.sics.gvod.stun.client.events.GetNatTypeRequest;
import se.sics.gvod.stun.client.events.GetNatTypeResponse;
import se.sics.gvod.stun.client.events.StunClientInit;
import se.sics.gvod.stun.msgs.EchoChangeIpAndPortMsg;
import se.sics.gvod.stun.msgs.EchoChangePortMsg;
import se.sics.gvod.stun.msgs.EchoMsg;
import se.sics.gvod.timer.TimeoutId;
import se.sics.kompics.KompicsEvent;

/**
 *
 * @author Owner
 */
public class StunUnitTest extends VodRetryComponentTestCase {

    StunClient stunClient = null;
    LinkedList<KompicsEvent> events;
    Set<Address> stunServers;

    public StunUnitTest() {
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        stunClient = new StunClient(this,
                new StunClientInit(this,
                        0,
                        StunClientConfiguration.build().
                        setRandTolerance(1).
                        setRuleExpirationMinWait(ruleLifeTime).
                        setRuleExpirationIncrement(ruleLifeTime).
                        setUpnpEnable(false).
                        setUpnpTimeout(500).
                        setMinimumRtt(500).
                        setRto(500).
                        setRtoRetries(0)));
        stunServers = new HashSet<Address>();
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testStunNoServers() {
        stunClient.handleGetNatTypeRequest.handle(new GetNatTypeRequest(stunServers, 5000, true));
        events = pollEvent(1);
        assertSequence(events, GetNatTypeResponse.class);
        GetNatTypeResponse r = (GetNatTypeResponse) events.getFirst();
        assert (r.getStatus() == GetNatTypeResponse.Status.NO_SERVER);
    }

    @Test
    public void testOpenClient() {
        stunServers.add(pubAddrs.get(0).getPeerAddress());
        stunClient.handleGetNatTypeRequest.handle(new GetNatTypeRequest(stunServers, 5000, true));
        events = pollEvent(1);
        assertSequence(events, EchoMsg.Request.class);
        EchoMsg.Request req = (EchoMsg.Request) events.get(0);
        long transId = req.getTransactionId();

        TimeoutId tId = req.getTimeoutId();
        Set<Address> partners = new HashSet<Address>();
        partners.add(pubAddrs.get(1).getPeerAddress());
        stunClient.handleEchoResponse.handle(new EchoMsg.Response(pubAddrs.get(0),
                getAddress(), partners, 100, EchoMsg.Test.UDP_BLOCKED,
                transId, tId, 3479));
        events = pollEvent(1);
        assertSequence(events, EchoChangeIpAndPortMsg.Request.class);
        EchoChangeIpAndPortMsg.Request ecip = (EchoChangeIpAndPortMsg.Request) events.get(0);

        EchoChangeIpAndPortMsg.Response changIpResp = new EchoChangeIpAndPortMsg.Response(
                pubAddrs.get(0), getAddress(), transId, ecip.getTimeoutId());
        stunClient.handleEchoChangeIpAndPortResponse.handle(changIpResp);
        events = pollEvent(1);
        assertSequence(events, GetNatTypeResponse.class);
        GetNatTypeResponse resp = (GetNatTypeResponse) events.get(0);
        assert (resp.getNat().getType() == Nat.Type.OPEN);
    }

    @Test
    public void testNatClient() {
        stunServers.add(pubAddrs.get(0).getPeerAddress());
        stunClient.handleGetNatTypeRequest.handle(new GetNatTypeRequest(stunServers, 5000, true));
        events = pollEvent(1);
        assertSequence(events, EchoMsg.Request.class);
        EchoMsg.Request req = (EchoMsg.Request) events.get(0);
        long transId = req.getTransactionId();

        TimeoutId tId = req.getTimeoutId();
        Set<Address> partners = new HashSet<Address>();
        partners.add(pubAddrs.get(1).getPeerAddress());
        stunClient.handleEchoResponse.handle(new EchoMsg.Response(pubAddrs.get(0),
                privAddrs.get(0), partners, 100, EchoMsg.Test.UDP_BLOCKED,
                transId, tId, 3479));
        events = pollEvent(1);
        assertSequence(events, EchoChangeIpAndPortMsg.Request.class);
        EchoChangeIpAndPortMsg.Request ecip = (EchoChangeIpAndPortMsg.Request) events.get(0);

        ScheduleRetryTimeout st = new ScheduleRetryTimeout(1000, 2);
        EchoChangeIpAndPortMsg.RequestRetryTimeout changIpResp
                = (EchoChangeIpAndPortMsg.RequestRetryTimeout) timeouts.get(ecip.getTimeoutId());
        st.setTimeoutEvent(changIpResp);
        stunClient.handleEchoChangeIpAndPortTimeout.handle(changIpResp);
        events = pollEvent(1);
        assertSequence(events, EchoChangePortMsg.Request.class);
    }
}
