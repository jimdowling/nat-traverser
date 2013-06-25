/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.nat.hp.client.events;

import se.sics.gvod.hp.msgs.HpRegisterMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;

/**
 *
 * @author jdowling
 */
public class RegisterWithRendezvousServerTimeout extends RewriteableRetryTimeout
{

//    public static final class RequestRetryTimeout extends RewriteableRetryTimeout {

        private final HpRegisterMsg.Request requestMsg;
        private final RegisterWithRendezvousServerRequest  registerWithRendezvousServerRequest;

        public RegisterWithRendezvousServerTimeout(ScheduleRetryTimeout st,
                HpRegisterMsg.Request requestMsg,
                RegisterWithRendezvousServerRequest registerWithRendezvousServerRequest) {
            super(st, requestMsg);
            this.requestMsg = requestMsg;
            this.registerWithRendezvousServerRequest = registerWithRendezvousServerRequest;
        }

        public RegisterWithRendezvousServerRequest getRegisterWithRendezvousServerRequest() {
            return registerWithRendezvousServerRequest;
        }

        public HpRegisterMsg.Request getRequestMsg() {
            return requestMsg;
        }
    }