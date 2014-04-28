/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.common;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.DirectMsg;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class TestMsgId extends DirectMsg {

    static final long serialVersionUID = 1L;

    public TestMsgId(VodAddress src, VodAddress dest, TimeoutId timeoutId) {
        super(src, dest);
        setTimeoutId(timeoutId);
    }

    public TestMsgId(VodAddress src, VodAddress dest) {
        super(src, dest);
    }

    @Override
    public RewriteableMsg copy() {
        return new TestMsgId(getVodSource(), getVodDestination(), getTimeoutId());
    }
}