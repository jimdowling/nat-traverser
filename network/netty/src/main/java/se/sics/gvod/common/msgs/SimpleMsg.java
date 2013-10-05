/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.Transport;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.timer.NoTimeoutId;

public class SimpleMsg extends RewriteableMsg implements Encodable {

    private static final long serialVersionUID = 43434242850L;
    String msg;

    public SimpleMsg(Address source, Address destination, Transport protocol, String msg) {
        super(source, destination, protocol, new NoTimeoutId());
        this.source = source;
        this.destination = destination;
        this.msg = new String(msg + "\n");
    }

    @Override
    public int getSize() {
    	return this.msg.length();
    }
    
	@Override
	public RewriteableMsg copy() {
		return null;
	}

	@Override
	public ByteBuf toByteArray() throws MessageEncodingException {
    	ByteBuf buffer = Unpooled.buffer(this.getSize() + 1);
        byte b = getOpcode();
        buffer.writeByte(b);
        buffer.writeBytes(this.msg.getBytes());
        return buffer;
	}

	@Override
	public byte getOpcode() {
		// TODO Auto-generated method stub
		return 0;
	}
}
