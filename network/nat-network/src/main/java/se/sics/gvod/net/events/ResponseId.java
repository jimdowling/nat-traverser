/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.events;

import se.sics.kompics.Request;
import se.sics.kompics.Response;

/**
 *
 * @author jdowling
 */
public abstract class ResponseId extends Response implements NodeId
{

	protected ResponseId(Request request) {
            super(request);
	}    
    
}
