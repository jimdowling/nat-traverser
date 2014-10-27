/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.NoTimeoutId;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.UnsetTimeoutId;

/**
 *
 * @author jdowling
 */
public class DirectMsgNetty {

    public static abstract class Base extends DirectMsg implements Encodable {

        private static final long serialVersionUID = 75484442850L;


        /**
         * This constructor should be used by msgs that do set a TimeoutId. If
         * you pass in a null as timeoutId, then Netty will expect that no
         * TimeoutId has been set.
         *
         * @param source
         * @param destination
         * @param timeoutId - should not be null.
         */
        protected Base(VodAddress source, VodAddress destination, TimeoutId timeoutId) {
            this(source, destination, Transport.UDP, timeoutId);
        }

        /**
         *
         * @param source
         * @param destination
         * @param transport
         * @param timeoutId
         */
        protected Base(VodAddress source, VodAddress destination,
                Transport transport, TimeoutId timeoutId) {
            super(source, destination, transport, timeoutId);
        }

        protected ByteBuf createChannelBufferWithHeader()
                throws MessageEncodingException {
            ByteBuf buffer = Unpooled.buffer( getSize() + 1 /*opcode*/);
            writeHeader(buffer);
            return buffer;
        }

        protected int getHeaderSize() {
            return 4 // srcId
                    + 4 // destId
                    + (hasTimeout() ? 4 : 0) // timeoutId
                    + 1 /*natPolicy src*/
                    + (vodSrc.isOpen() ? 0 : (UserTypesEncoderFactory.ADDRESS_LEN * VodConfig.PM_NUM_PARENTS)) 
                    + 1 /*natPolicy dest*/
                    + (4 * 2) /* overlayId of client and server */;
        }

        protected void writeHeader(ByteBuf buffer) throws MessageEncodingException {
            byte b = getOpcode();
            buffer.writeByte(b);
            if (hasTimeout()) {
                UserTypesEncoderFactory.writeTimeoutId(buffer, timeoutId);
            }
            buffer.writeInt(getSource().getId());
            buffer.writeInt(getDestination().getId());
            buffer.writeInt(vodSrc.getOverlayId());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, vodSrc.getNatPolicy());
            UserTypesEncoderFactory.writeListAddresses(buffer, vodSrc.getParents());
            buffer.writeInt(vodDest.getOverlayId());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, vodDest.getNatPolicy());
            // do not serialize parents of destination
        }

        @Override
        public abstract int getSize();
    }

    /**
     * timeoutId *must* be set for request objects. The normal way to do this is to
     * use MsgRetryComponent's doRetry() method to send the Request msg.
     * 
     * If you relay one of these msgs, you have to set the TimeoutId explicitly,
     * e.g., by taking the timeoutId returned from doRetry() and setting it on
     * the msg:
     * 
     * TimeoutId id = delgator.doRetry(relayMsgTimeout, 1, 1000d);
     * msg.setTimeoutId(id);
     * 
     */
    public static abstract class Request extends Base {

        protected Request(VodAddress source, VodAddress destination) {
            this(source, destination, new UnsetTimeoutId());
        }

        protected Request(VodAddress source, VodAddress destination, TimeoutId timeoutId) {
            this(source, destination, Transport.UDP, timeoutId);
        }

        protected Request(VodAddress source, VodAddress destination,
                Transport transport, TimeoutId timeoutId) {
            super(source, destination, transport, timeoutId);
        }
    }
    
    /**
     * Response objects must set TimeoutId in their constructors.
     */
    public static abstract class Response extends Base {

        protected Response(VodAddress source, VodAddress destination) {
            this(source, destination, new UnsetTimeoutId());
        }

        protected Response(VodAddress source, VodAddress destination, TimeoutId timeoutId) {
            this(source, destination, Transport.UDP, timeoutId);
        }

        protected Response(VodAddress source, VodAddress destination,
                Transport transport, TimeoutId timeoutId) {
            super(source, destination, transport, timeoutId);
        }
    }
    
    /**
     * TimeoutId must not be set for Oneway objects.
     */
    public static abstract class Oneway extends Base {

        protected Oneway(VodAddress source, VodAddress destination) {
            this(source, destination, Transport.UDP);
        }

        protected Oneway(VodAddress source, VodAddress destination,
                Transport transport) {
            super(source, destination, transport, new NoTimeoutId());
        }

        @Override
        public void setTimeoutId(TimeoutId timeoutId) {
            throw new IllegalStateException("You cannot set a timeoutId for a Oneway Message.");
        }
    
    }

    /**
     * timeoutId *must* be set for request objects. The normal way to do this is to
     * use MsgRetryComponent's doRetry() method to send the Request msg.
     * 
     * If you relay one of these msgs, you have to set the TimeoutId explicitly,
     * e.g., by taking the timeoutId returned from doRetry() and setting it on
     * the msg:
     * 
     * TimeoutId id = delgator.doRetry(relayMsgTimeout, 1, 1000d);
     * msg.setTimeoutId(id);
     * 
     */
    public static abstract class SystemRequest extends Base {

        protected SystemRequest(VodAddress source, VodAddress destination) {
            this(source, destination, new UnsetTimeoutId());
        }

        protected SystemRequest(VodAddress source, VodAddress destination, TimeoutId timeoutId) {
            this(source, destination, Transport.UDP, timeoutId);
        }

        protected SystemRequest(VodAddress source, VodAddress destination,
                Transport transport, TimeoutId timeoutId) {
            super(source, destination, transport, timeoutId);
        }
    }
    
    /**
     * Response objects must set TimeoutId in their constructors.
     */
    public static abstract class SystemResponse extends Base {

        protected SystemResponse(VodAddress source, VodAddress destination) {
            this(source, destination, new UnsetTimeoutId());
        }

        protected SystemResponse(VodAddress source, VodAddress destination, TimeoutId timeoutId) {
            this(source, destination, Transport.UDP, timeoutId);
        }

        protected SystemResponse(VodAddress source, VodAddress destination,
                Transport transport, TimeoutId timeoutId) {
            super(source, destination, transport, timeoutId);
        }
    }

    /**
     * TimeoutId must not be set for Oneway objects.
     */
    public static abstract class SystemOneway extends Base {

        protected SystemOneway(VodAddress source, VodAddress destination) {
            this(source, destination, Transport.UDP);
        }

        protected SystemOneway(VodAddress source, VodAddress destination,
                Transport transport) {
            super(source, destination, transport, new NoTimeoutId());
        }

        @Override
        public void setTimeoutId(TimeoutId timeoutId) {
            throw new IllegalStateException("You cannot set a timeoutId for a Oneway Message.");
        }
        
        
    }    
}