package se.sics.kompics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Semaphore;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.config.BaseCommandLineConfig;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.NettyInit;
import se.sics.gvod.net.NettyNetwork;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.UUID;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.gvod.video.msgs.EncodedSubPiece;
import se.sics.gvod.video.msgs.Piece;
import se.sics.gvod.video.msgs.SubPiece;
import se.sics.gvod.video.msgs.VideoPieceMsg;

/**
 * Unit test for simple App.
 */
public class VideoPieceMsgSendingTest
        extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(VideoPieceMsgSendingTest.class);
    private boolean testStatus = true;

    /**
     * Create the test case
     *
     */
    public VideoPieceMsgSendingTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(VideoPieceMsgSendingTest.class);
    }

    public static void setTestObj(VideoPieceMsgSendingTest testObj) {
        VideoPieceMsgSendingTest.TestStClientComponent.testObj = testObj;
    }

    public static class TestStClientComponent extends ComponentDefinition {

        private Component client;
        private Component server;
        private Component timer;
        private static VideoPieceMsgSendingTest testObj = null;
        private VodAddress clientAddr;
        private VodAddress serverAddr;
        private UtilityVod utility = new UtilityVod(10, 200, 15);
        // VideoPieceMsg
        private Piece piece;
        private Map<Integer, EncodedSubPiece> subPieces;

        public TestStClientComponent() {
            timer = create(JavaTimer.class);
            client = create(NettyNetwork.class);
            server = create(NettyNetwork.class);

            InetAddress ip = null;
            int clientPort = 59877;
            int serverPort = 33221;


            try {
                ip = InetAddress.getLocalHost();

            } catch (UnknownHostException ex) {
                logger.error("UnknownHostException");
                testObj.fail();
            }
            Address cAddr = new Address(ip, clientPort, 0);
            Address sAddr = new Address(ip, serverPort, 1);

            clientAddr = new VodAddress(cAddr, VodConfig.SYSTEM_OVERLAY_ID);
            serverAddr = new VodAddress(sAddr, VodConfig.SYSTEM_OVERLAY_ID);

            connect(client.getNegative(Timer.class), timer.getPositive(Timer.class));
            connect(server.getNegative(Timer.class), timer.getPositive(Timer.class));

            subscribe(handleStart, control);
            subscribe(handleMsgTimeout, timer.getPositive(Timer.class));
            subscribe(handlePieceRequest, server.getPositive(VodNetwork.class));
            subscribe(handlePieceResponse, client.getPositive(VodNetwork.class));

            trigger(new NettyInit(clientAddr.getPeerAddress(), false, (int) 132),
                    client.getControl());
            trigger(new NettyInit(serverAddr.getPeerAddress(), false, (int) 132),
                    server.getControl());

            // VideoPieceMsg
            subPieces = new HashMap<Integer,EncodedSubPiece>();
            Random random = new Random();
            byte[] pieceData = new byte[Piece.PIECE_DATA_SIZE];
            random.nextBytes(pieceData);
            piece = new Piece(0, pieceData);
            for (SubPiece sp : piece.getSubPieces()) {
                EncodedSubPiece esp = new EncodedSubPiece(sp.getId(), sp.getId(), sp.getData(), piece.getId());
                subPieces.put(esp.getGlobalId(), esp);
            }
        }
        public Handler<Start> handleStart = new Handler<Start>() {

            @Override
            public void handle(Start event) {
                System.out.println("Starting");
                Set<Integer> requestIds = new HashSet<Integer>();
                for (int i = 0; i < 1; i++) {
                    requestIds.add(i);
                }
                VideoPieceMsg.Request request = new VideoPieceMsg.Request(clientAddr, serverAddr, UUID.nextUUID(), requestIds);
                trigger(request, client.getPositive(VodNetwork.class));
            }
        };
        public Handler<VideoPieceMsg.Request> handlePieceRequest = new Handler<VideoPieceMsg.Request>() {

            @Override
            public void handle(VideoPieceMsg.Request r) {
                System.out.println("Piece Request (from " + r.getVodSource().getId() + " to " + r.getVodDestination().getId() + ")");
                EncodedSubPiece esp = subPieces.get(r.getPiecesIds().iterator().next());
                        VideoPieceMsg.Response response = new VideoPieceMsg.Response(
                        r.getVodDestination(),
                        r.getVodSource(),
                        r.getTimeoutId(),
                        esp);
                trigger(response, server.getPositive(VodNetwork.class));
            }
        };
        public Handler<VideoPieceMsg.Response> handlePieceResponse = new Handler<VideoPieceMsg.Response>() {

            @Override
            public void handle(VideoPieceMsg.Response event) {
                System.out.println("Piece Response");

                EncodedSubPiece esp = event.getEncodedSubPiece();
                if (!esp.equals(subPieces.get(esp.getGlobalId()))) {
                    testObj.fail(true);
                }


                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());

                testObj.pass();
            }
        };
        public Handler<VideoPieceMsg.RequestTimeout> handleMsgTimeout = new Handler<VideoPieceMsg.RequestTimeout>() {

            @Override
            public void handle(VideoPieceMsg.RequestTimeout event) {
                trigger(new Stop(), client.getControl());
                trigger(new Stop(), server.getControl());
                System.out.println("Msg timeout");
                testObj.testStatus = false;
                testObj.fail();
            }
        };
    }
    private static final int EVENT_COUNT = 1;
    private static Semaphore semaphore = new Semaphore(0);

    private void allTests() {
        int i = 0;

        runInstance();
        if (testStatus == true) {
            assertTrue(true);
        }
    }

    private void runInstance() {
        Kompics.createAndStart(VideoPieceMsgSendingTest.TestStClientComponent.class, 1);
        try {
            VideoPieceMsgSendingTest.semaphore.acquire(EVENT_COUNT);
            System.out.println("Finished test.");
        } catch (InterruptedException e) {
            assert (false);
        } finally {
            Kompics.shutdown();
        }
        if (testStatus == false) {
            assertTrue(false);
        }

    }

    @org.junit.Ignore
    public void testApp() {
        setTestObj(this);

        allTests();
    }

    public void pass() {
        VideoPieceMsgSendingTest.semaphore.release();
    }

    public void fail(boolean release) {
        testStatus = false;
        VideoPieceMsgSendingTest.semaphore.release();
    }
}
