package se.sics.kompics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.*;
import se.sics.gvod.address.Address;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.common.msgs.Encodable;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.OpCode;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.UUID;
import se.sics.gvod.video.msgs.VideoConnectionMsg;
import se.sics.gvod.video.msgs.VideoConnectionMsgFactory;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoConnectionMsgTest extends TestCase {

    private static Address src, dest;
    private static VodAddress gSrc, gDest;

    public VideoConnectionMsgTest() {
    }

    private void opCodeCorrect(ChannelBuffer buffer, Encodable msg) {
        byte type = buffer.readByte();
        OpCode opCode = OpCode.fromByte(type);
        assert (opCode.equals(msg.getOpcode()));
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
            InetAddress self = InetAddress.getByName("127.0.0.1");
            src = new Address(self, 58027, 123);
            dest = new Address(self, 65535, 123);
            gSrc = new VodAddress(src, VodConfig.SYSTEM_OVERLAY_ID);
            gDest = new VodAddress(dest, VodConfig.SYSTEM_OVERLAY_ID);
        } catch (UnknownHostException ex) {
            Logger.getLogger(VideoConnectionMsgTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testRequest() {
        boolean randomRequest = false;
        VideoConnectionMsg.Request request = new VideoConnectionMsg.Request(gSrc, gDest, UUID.nextUUID(), randomRequest);
        assertEquals(OpCode.VIDEO_CONNECTION_REQUEST, request.getOpcode());
        assertEquals(gSrc, request.getVodSource());
        assertEquals(gDest, request.getVodDestination());
        assertEquals(randomRequest, request.isRandomRequest());
        try {
            ChannelBuffer buffer = request.toByteArray();
            opCodeCorrect(buffer, request);
            VideoConnectionMsg.Request processedRequest =
                    VideoConnectionMsgFactory.Request.fromBuffer(buffer);
            assertEquals(request.getSize(), processedRequest.getSize());
            assertEquals(request.isRandomRequest(), processedRequest.isRandomRequest());
            assertEquals(request.getVodSource(), processedRequest.getVodSource());
            assertEquals(request.getVodDestination(), processedRequest.getVodDestination());
        } catch (MessageEncodingException ex) {
            Logger.getLogger(VideoConnectionMsgTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessageDecodingException ex) {
            Logger.getLogger(VideoConnectionMsgTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testResponse() {
        boolean randomRequest = false;
        boolean accepted = true;
        VideoConnectionMsg.Response response = new VideoConnectionMsg.Response(gSrc, gDest, randomRequest, accepted);
        assertEquals(OpCode.VIDEO_CONNECTION_RESPONSE, response.getOpcode());
        assertEquals(randomRequest, response.wasRandomRequest());
        assertEquals(accepted, response.connectionAccepted());
        assertEquals(gSrc, response.getVodSource());
        assertEquals(gDest, response.getVodDestination());

        try {
            ChannelBuffer buffer = response.toByteArray();
            opCodeCorrect(buffer, response);
            VideoConnectionMsg.Response processedResponse =
                    VideoConnectionMsgFactory.Response.fromBuffer(buffer);
            assertEquals(response.getSize(), processedResponse.getSize());
            assertEquals(response.getVodSource(), processedResponse.getVodSource());
            assertEquals(response.getVodDestination(), processedResponse.getVodDestination());
            assertEquals(randomRequest, processedResponse.wasRandomRequest());
            assertEquals(accepted, processedResponse.connectionAccepted());
        } catch (MessageEncodingException ex) {
            Logger.getLogger(VideoConnectionMsgTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessageDecodingException ex) {
            Logger.getLogger(VideoConnectionMsgTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
