package se.sics.kompics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.address.Address;
import se.sics.gvod.common.Utility;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.NatNetworkControl;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.net.events.PortBindResponse.Status;

/**
 * Unit test for simple App.
 */
public class BindUdtToUdpPortTest extends TestCase {

	private static final Logger logger = LoggerFactory.getLogger(SetsExchangeTest.class);
	private boolean testStatus = true;

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public BindUdtToUdpPortTest(String testName) {
		super(testName);
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(BindUdtToUdpPortTest.class);
	}

	public static void setTestObj(BindUdtToUdpPortTest testObj) {
		TestStClientComponent.testObj = testObj;
	}

	public static class TestStClientComponent extends ComponentDefinition {

		private Component server;
		private static BindUdtToUdpPortTest testObj = null;
		private VodAddress serverAddr, serverAddr0;
		private Utility utility = new UtilityVod(10, 200, 15);
		private VodDescriptor nodeDesc;
		private List<VodDescriptor> nodes;

		public TestStClientComponent() {
			server = create(NettyNetwork.class);

			InetAddress ip = null;
			int serverPort = 54644;

			try {
				ip = InetAddress.getByName("127.0.0.1");

			} catch (UnknownHostException ex) {
				logger.error("UnknownHostException");
				fail();
			}
			Address sAddr0 = new Address(ip, serverPort + 10, 2);
			Address sAddr = new Address(ip, serverPort, 0);

			serverAddr0 = new VodAddress(sAddr0, VodConfig.SYSTEM_OVERLAY_ID);
			serverAddr = new VodAddress(sAddr, VodConfig.SYSTEM_OVERLAY_ID);

			nodeDesc = new VodDescriptor(serverAddr, utility, 0, BaseCommandLineConfig.DEFAULT_MTU);
			nodes = new ArrayList<VodDescriptor>();
			nodes.add(nodeDesc);

			subscribe(handleStart, control);
			subscribe(handlePortBindResponse, server.getPositive(NatNetworkControl.class));

			trigger(new NettyInit(111, true, BaseMsgFrameDecoder.class), 
                                server.getControl());
		}

		public Handler<Start> handleStart = new Handler<Start>() {

			public void handle(Start event) {
				System.out.println("Starting");
				PortBindRequest request = 
                                        new PortBindRequest(serverAddr0.getPeerAddress(), 
                                        Transport.UDP);
				request.setResponse(new PortBindResponse(request) {
				});
				trigger(request, server.getPositive(NatNetworkControl.class));
			}
		};

		private Status expected = Status.SUCCESS;
		public Handler<PortBindResponse> handlePortBindResponse = new Handler<PortBindResponse>() {

			@Override
			public void handle(PortBindResponse event) {
				System.out.println("Port bind response");

				if (event.getStatus() == expected && expected == Status.SUCCESS) {
					expected = Status.FAIL;
					PortBindRequest request = 
                                                new PortBindRequest(serverAddr0.getPeerAddress(),
							Transport.UDT);
					request.setResponse(new PortBindResponse(request) {
					});
					trigger(request, server.getPositive(NatNetworkControl.class));
				} else if (event.getStatus() == expected && expected == Status.FAIL) { 
					testObj.pass();
				} else {
					testObj.failAndRelease();
				}
			}
		};
	}

	private static final int EVENT_COUNT = 1;
	private static Semaphore semaphore = new Semaphore(0);

	private void allTests() {
		runInstance();
		assertTrue(testStatus);
	}

	private void runInstance() {
		System.setProperty("java.net.preferIPv4Stack", "true");
		Kompics.createAndStart(TestStClientComponent.class, 1);

		try {
			BindUdtToUdpPortTest.semaphore.acquire(EVENT_COUNT);
			System.out.println("Finished test.");
		} catch (InterruptedException e) {
			assert (false);
		} finally {
			Kompics.shutdown();
		}
	}

	@org.junit.Ignore
	public void testApp() {
		setTestObj(this);
		allTests();
	}

	public void pass() {
		BindUdtToUdpPortTest.semaphore.release();
	}

	public void failAndRelease() {
		testStatus = false;
		BindUdtToUdpPortTest.semaphore.release();
	}
}
