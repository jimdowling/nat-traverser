/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.common.msgs;

/**
 *
 * @author jdowling
 */
public class MessageDecodingException extends Exception {

	private static final long serialVersionUID = -3091417424840033881L;

	public MessageDecodingException(String mesg) {
		super(mesg);
	}

	public MessageDecodingException() {
	}

	public MessageDecodingException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public MessageDecodingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
}