/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.nat.common;

import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.DirectMsg;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public class TestMsg extends DirectMsg
{

    public TestMsg(VodAddress source, VodAddress destination) {
        super(source, destination);
    }

    @Override
    public RewriteableMsg copy() {
        return new TestMsg(getVodSource(), getVodDestination());
    }
}
