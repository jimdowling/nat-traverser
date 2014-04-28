/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;

/**
 *
 * @author jdowling
 */
public interface Encodable {
 

    public ByteBuf toByteArray() throws MessageEncodingException;

    public byte getOpcode();
}
