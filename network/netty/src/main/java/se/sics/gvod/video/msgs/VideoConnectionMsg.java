package se.sics.gvod.video.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.OpCode;
import se.sics.gvod.common.msgs.VodMsgNetty;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.NoTimeoutId;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author Niklas Wahl&#233;n <nwahlen@kth.se>
 */
public class VideoConnectionMsg {
    
    public enum Type {
        STANDARD, RANDOM, PARENT, CHILD;
    }

    public static final class Request extends VodMsgNetty {

        private boolean randomRequest;

        public Request(VodAddress source, VodAddress destination, boolean randomRequest) {
            this(source, destination, new NoTimeoutId(), randomRequest);
        }

        public Request(VodAddress source, VodAddress destination,
                TimeoutId timeoutId, boolean randomRequest) {
            super(source, destination, Transport.UDP, timeoutId);
            this.randomRequest = randomRequest;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 1 /*
                     * randomRequest
                     */;
        }

        @Override
        public RewriteableMsg copy() {
            return new VideoConnectionMsg.Request(vodSrc, vodDest, timeoutId, randomRequest);
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeBoolean(buffer, randomRequest);
            return buffer;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.VIDEO_CONNECTION_REQUEST;
        }

        public boolean isRandomRequest() {
            return randomRequest;
        }
    }

    public static final class Response extends VodMsgNetty {

        private boolean randomRequest;
        private boolean acceptConnection;

        public Response(VodAddress source, VodAddress destination, boolean randomRequest, boolean acceptConnection) {
            this(source, destination, null, randomRequest, acceptConnection);
        }

        public Response(VideoConnectionMsg.Request request, boolean acceptConnection) {
            this(request.getVodDestination(), request.getVodSource(), request.getTimeoutId(), request.isRandomRequest(), acceptConnection);
        }

        public Response(VodAddress source, VodAddress destination,
                TimeoutId timeoutId, boolean randomRequest, boolean acceptConnection) {
            super(source, destination, Transport.UDP, timeoutId);
            this.randomRequest = randomRequest;
            this.acceptConnection = acceptConnection;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 1 /*
                     * randomRequest
                     */
                    + 1 /*
                     * acceptConnection
                     */;
        }

        @Override
        public RewriteableMsg copy() {
            return new Response(vodSrc, vodDest, timeoutId, randomRequest, acceptConnection);
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeBoolean(buffer, randomRequest);
            UserTypesEncoderFactory.writeBoolean(buffer, acceptConnection);
            return buffer;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.VIDEO_CONNECTION_RESPONSE;
        }

        public boolean wasRandomRequest() {
            return randomRequest;
        }

        public boolean connectionAccepted() {
            return acceptConnection;
        }
    }

    public static final class RequestTimeout extends Timeout {

        private final Request requestMsg;

        public RequestTimeout(ScheduleTimeout st, Request requestMsg) {
            super(st);
            this.requestMsg = requestMsg;
        }

        public Request getRequestMsg() {
            return requestMsg;
        }
    }
    
    public static final class Disconnect extends VodMsgNetty {
        
        private boolean randomConnection;
        
        public Disconnect(VodAddress source, VodAddress destination, TimeoutId timeoutId, boolean randomConnection) {
            super(source, destination, Transport.UDP, timeoutId);
            this.randomConnection = randomConnection;
        }

        @Override
        public int getSize() {
            return getHeaderSize() + 1;
        }

        @Override
        public RewriteableMsg copy() {
            return new Disconnect(vodSrc, vodDest, timeoutId, randomConnection);
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeBoolean(buffer, randomConnection);
            return buffer;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.VIDEO_CONNECTION_DISCONNECT;
        }

        public boolean wasRandomConnection() {
            return randomConnection;
        }
        
    }
}
