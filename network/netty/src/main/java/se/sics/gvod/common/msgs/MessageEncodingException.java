/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.common.msgs;

/**
 *
 * @author jdowling
 */
public class MessageEncodingException extends Exception {

	private static final long serialVersionUID = -3078825718180083408L;

	public MessageEncodingException(String mesg) {
		super(mesg);
	}

}
