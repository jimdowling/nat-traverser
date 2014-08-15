package se.sics.kompics.nat.upnp.comp;

import se.sics.gvod.stun.upnp.UpnpPort;
import se.sics.gvod.stun.upnp.UpnpComponent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import se.sics.gvod.address.Address;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics; 
import se.sics.kompics.Start;
import se.sics.gvod.stun.upnp.events.MapPortRequest;
import se.sics.gvod.stun.upnp.events.MapPortsRequest;
import se.sics.gvod.stun.upnp.events.MapPortsResponse;
import se.sics.gvod.stun.upnp.events.UnmapPortsRequest;
import se.sics.gvod.stun.upnp.events.UnmapPortsResponse;
import se.sics.gvod.stun.upnp.events.UpnpGetPublicIpRequest;
import se.sics.gvod.stun.upnp.events.UpnpGetPublicIpResponse;
import se.sics.gvod.stun.upnp.events.UpnpInit;
import se.sics.gvod.timer.UUID;

public class UpnpTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public UpnpTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(UpnpTest.class);
    }

    public static void setTestObj(UpnpTest testObj) {
        TestUpnpComponent.testObj = testObj;
    }
    private boolean status = true;

    public static class TestUpnpComponent extends ComponentDefinition {

        private Component upnp;
        private static UpnpTest testObj = null;
        private int requestedPort = 58026;
        private Address src;
        private long startTime;

        public TestUpnpComponent() {
            upnp = create(UpnpComponent.class, new UpnpInit(5000, 1500));

            subscribe(handleStart, control);
            subscribe(handleUpnpDeletePortResponse, upnp.getPositive(UpnpPort.class));
            subscribe(handleUpnpAddPortResponse, upnp.getPositive(UpnpPort.class));
            subscribe(handleUpnpGetPublicIpResponse, upnp.getPositive(UpnpPort.class));
        }
        public Handler<Start> handleStart = new Handler<Start>() {

            public void handle(Start event) {

                try {
                    src = new Address(InetAddress.getLocalHost(), requestedPort, 0);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(UpnpTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                trigger(new UpnpGetPublicIpRequest(UUID.nextUUID()),
                        upnp.getPositive(UpnpPort.class));

            }
        };

        public Handler<UpnpGetPublicIpResponse> handleUpnpGetPublicIpResponse = new Handler<UpnpGetPublicIpResponse>() {

            public void handle(UpnpGetPublicIpResponse event) {
                InetAddress upnpIpAddr = event.getExternalIp();
                System.out.println("Using private address: " + src);
                System.out.println("External UPnP Address: " + upnpIpAddr);
                if (upnpIpAddr == null) {
                    testObj.pass();
                } else {
                    System.out.println("Requesting UPnP Port mapping.");
                    Map<Integer, Integer> portsRequested = new HashMap<Integer, Integer>();
                    portsRequested.put(src.getPort(), requestedPort);
                    MapPortsRequest req = new MapPortsRequest(UUID.nextUUID(),
                            portsRequested, MapPortRequest.Protocol.TCP);
                    trigger(req, upnp.getPositive(UpnpPort.class));
                    startTime = System.currentTimeMillis();
                }
            }
        };
        public Handler<MapPortsResponse> handleUpnpAddPortResponse = new Handler<MapPortsResponse>() {

            public void handle(MapPortsResponse event) {

                Map<Integer, Integer> privatePublicIps = event.getPrivatePublicPorts();
                System.out.print("received " + privatePublicIps.size() + " port response. Ports : {");
                for (Integer pub : privatePublicIps.values()) {
                    System.out.print(pub + ",");
                }
                System.out.println("}");

                System.out.println("Mapping took msec: " + (System.currentTimeMillis() - startTime));

                trigger(new UnmapPortsRequest(UUID.nextUUID()),
                        upnp.getPositive(UpnpPort.class));

                for (Integer pub : privatePublicIps.values()) {
                    if (pub != requestedPort) {
                        System.out.println("Allocated port didn't match requested port!!");
                        testObj.fail(true);
                    } else {
                        testObj.pass();
                    }
                }

            }
        };

        private Handler<UnmapPortsResponse> handleUpnpDeletePortResponse = new Handler<UnmapPortsResponse>() {

            public void handle(UnmapPortsResponse event) {

                System.out.println("delete port received response...." + event.isSuccess());
//                testObj.pass();
            }
        };
    }
    private static final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    public void testApp() {

        setTestObj(this);

        Kompics.createAndStart(TestUpnpComponent.class, 1);
        try {
            UpnpTest.semaphore.acquire(EVENT_COUNT);
            System.out.println("Exiting unit test....");
        } catch (InterruptedException e) {
            assert (false);
        } finally {
            Kompics.shutdown();
        }

        if (status == false) {
            assertTrue(false);
        } else {
            assertTrue(true);
        }
    }

    public void pass() {
        UpnpTest.semaphore.release();
    }

    public void fail(boolean release) {
        UpnpTest.semaphore.release();
        status = false;
    }
}
