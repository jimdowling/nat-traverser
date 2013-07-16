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
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public abstract class DirectMsgNetty extends DirectMsg implements Encodable  
{

    private static final long serialVersionUID = 75484442850L;

    /**
     * This constructor should only be used by msgs that do not set a TimeoutId.
     * @param source
     * @param destination 
     */
    protected DirectMsgNetty(VodAddress source, VodAddress destination) {
        this(source, destination, null);
    }

    /**
     * This constructor should be used by msgs that do set a TimeoutId.
     * If you pass in a null as timeoutId, then Netty will expect that no
     * TimeoutId has been set.
     * @param source
     * @param destination 
     * @param timeoutId - should not be null.
     */    protected DirectMsgNetty(VodAddress source, VodAddress destination, TimeoutId timeoutId) {
        this(source, destination, Transport.UDP, timeoutId);
    }
    
     /**
      * 
      * @param source
      * @param destination
      * @param protocol
      * @param timeoutId 
      */
    protected DirectMsgNetty(VodAddress source, VodAddress destination,
            Transport protocol, TimeoutId timeoutId) {
        super(source, destination,  protocol, timeoutId);
    }
    
    protected ByteBuf createChannelBufferWithHeader()
            throws MessageEncodingException {
        ByteBuf buffer =
        		Unpooled.buffer(
                getSize()
                + 1 /*opcode*/);
        writeHeader(buffer);
        return buffer;
    }    

    protected int getHeaderSize() {
        return  4 // srcId
                + 4 // destId
                + (hasTimeout() ? 4 : 0) // timeoutId
                + 1 /*natPolicy src*/
                + (UserTypesEncoderFactory.ADDRESS_LEN * VodConfig.PM_PARENT_SIZE)
                + 1 /*natPolicy dest*/
                + (4*2) /* overlayId of client and server */
                ;
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

    @Override public abstract int getSize();
}
