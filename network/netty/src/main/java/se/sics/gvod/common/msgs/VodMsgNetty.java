/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public abstract class VodMsgNetty extends VodMsg implements Encodable  
{

    private static final long serialVersionUID = 75484442850L;

    protected VodMsgNetty(VodAddress source, VodAddress destination) {
        this(source, destination, null);
    }

    protected VodMsgNetty(VodAddress source, VodAddress destination,
            TimeoutId timeoutId) {
        this(source, destination, Transport.UDP, timeoutId);
    }    
    
    protected VodMsgNetty(VodAddress source, VodAddress destination,
            Transport protocol, TimeoutId timeoutId) {
        super(source, destination,  protocol, timeoutId);
    }
    
    protected ChannelBuffer createChannelBufferWithHeader()
            throws MessageEncodingException {
        ChannelBuffer buffer =
                ChannelBuffers.dynamicBuffer(
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

    protected void writeHeader(ChannelBuffer buffer) throws MessageEncodingException {
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
