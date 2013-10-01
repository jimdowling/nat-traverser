/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.util;

import java.util.ArrayList;
import java.util.List;
import se.sics.gvod.common.RetryComponentDelegator;
import se.sics.gvod.common.msgs.NatReportMsg;
import se.sics.gvod.common.util.ToVodAddr;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;
import se.sics.kompics.Positive;

/**
 *
 * @author jdowling
 */
public class NatReporter {
    
    public static void report(RetryComponentDelegator component, Positive<VodNetwork> network,
            VodAddress selfAddress, int portUsed, VodAddress target, boolean success, long timeTaken,
            String str) {
        NatReportMsg.NatReport nr = new NatReportMsg.NatReport(portUsed, target, success, 
                timeTaken, str);
        List<NatReportMsg.NatReport> nrs = new ArrayList<NatReportMsg.NatReport>();
        nrs.add(nr);
        VodAddress dest = ToVodAddr.bootstrap(VodConfig.getBootstrapServer());
        NatReportMsg msg = new NatReportMsg(selfAddress, dest, nrs);
        component.doTrigger(msg, network);
    }
    
}
