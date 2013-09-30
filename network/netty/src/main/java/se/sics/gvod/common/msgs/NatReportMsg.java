/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;
import java.util.List;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;

public class NatReportMsg extends DirectMsgNetty.Oneway {

    private static final long serialVersionUID = 43434242850L;

    public static class NatReport {

        private final int portUsed;
        private final VodAddress target;
        private final boolean success;
        private final long timeTaken;
        private final String msg;

        public NatReport(int portUsed, VodAddress target, boolean success, long timeTaken, String msg) {
            this.portUsed = portUsed;
            this.target = target;
            this.success = success;
            this.timeTaken = timeTaken;
            this.msg = msg;
        }

        public int getPortUsed() {
            return portUsed;
        }
        
        public String getMsg() {
            return msg;
        }

        public VodAddress getTarget() {
            return target;
        }

        public long getTimeTaken() {
            return timeTaken;
        }
        
        public boolean isSuccess() {
            return success;
        }
    }
    
    private List<NatReport> natReports;

    public NatReportMsg(VodAddress source, VodAddress destination,
            List<NatReport> natReports) {
        super(source, destination);
        assert(natReports != null);
        this.natReports = natReports;
    }

    public List<NatReport> getNatReports() {
        return natReports;
    }

    @Override
    public int getSize() {
        return this.natReports.size() *  58;
    }

    @Override
    public RewriteableMsg copy() {
        // immutable object, so ok to copy references
        return new NatReportMsg(vodSrc, vodDest, natReports);
    }

    @Override
    public ByteBuf toByteArray() throws MessageEncodingException {
        ByteBuf buffer = createChannelBufferWithHeader();
        UserTypesEncoderFactory.writeNatReports(buffer, natReports);
        return buffer;
    }

    @Override
    public byte getOpcode() {
        return BaseMsgFrameDecoder.NAT_MONITOR_REPORT;
    }
}
