/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.common.msgs;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 *
 * @author jdowling
 */
public interface Encodable {
 

    public ChannelBuffer toByteArray() throws MessageEncodingException;

    public OpCode getOpcode();
}
