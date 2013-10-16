/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.util;

import java.util.ArrayList;
import java.util.List;
import se.sics.gvod.address.Address;
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

    /**
     *
     * @param component
     * @param network
     * @param selfAddress the client's address
     * @param portUsed by the client that is reporting the issue
     * @param target destination address for the issue being reported
     * @param success was the issue a problem or a success
     * @param timeTaken how long did the issue take?
     * @param msg textual description of the report
     */
    public static void report(RetryComponentDelegator component, Positive<VodNetwork> network,
            VodAddress selfAddress, int portUsed, VodAddress target, boolean success, long timeTaken,
            String msg) {
        if (portUsed < 0 || portUsed > 65535) {
            throw new IllegalArgumentException("portUsed must be in the range 0..65535");
        }
        if (timeTaken < 0) {
            throw new IllegalArgumentException("timeTaken must be greater than 0.");
        }
        if (msg.length() > 65535) {
            throw new IllegalArgumentException("msg should be less than 65535 chars.");
        }
        NatReportMsg.NatReport nr = new NatReportMsg.NatReport(portUsed, target, success,
                timeTaken, msg);
        List<NatReportMsg.NatReport> nrs = new ArrayList<NatReportMsg.NatReport>();
        nrs.add(nr);
        Address bAddr = VodConfig.getBootstrapServer();
        if (bAddr != null) {
            VodAddress dest = ToVodAddr.bootstrap(bAddr);
            NatReportMsg evt = new NatReportMsg(selfAddress, dest, nrs);
            component.doTrigger(evt, network);
        }
    }
}
