/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import se.sics.gvod.net.Transport;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.timer.TimeoutId;

/**
 * 
 * @author jdowling
 */
public abstract class RewriteableMsgNetty extends RewriteableMsg
        implements Encodable {

    private static final long serialVersionUID = 7778885542850L;

    protected RewriteableMsgNetty(Address source, Address destination) {
        super(source, destination, Transport.UDP);
    }

    protected RewriteableMsgNetty(Address source, Address destination, Transport protocol,
            TimeoutId timeoutId) {
        super(source, destination, protocol, timeoutId);
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

    // do not include 'len' and 'opcode' in the header length. Len is the body length.
    protected int getHeaderSize() {
        return 4 // srcId
                + 4 // destId
                + (hasTimeout() ? 4 : 0); // timeoutId
//                + UserTypesEncoderFactory.UUID_LEN;
    }

    @Override
    public abstract int getSize();

    protected void writeHeader(ChannelBuffer buffer) throws MessageEncodingException {
        byte b = getOpcode();
        buffer.writeByte(b);
        if (hasTimeout()) {
            buffer.writeInt(timeoutId.getId());
        }
        buffer.writeInt(getSource().getId());
        buffer.writeInt(getDestination().getId());
    }
}
