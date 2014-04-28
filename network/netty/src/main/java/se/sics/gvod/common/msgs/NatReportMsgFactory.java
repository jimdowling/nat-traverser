package se.sics.gvod.common.msgs;

import io.netty.buffer.ByteBuf;
import java.util.List;
import se.sics.gvod.common.msgs.NatReportMsg.NatReport;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class NatReportMsgFactory extends DirectMsgNettyFactory.Oneway {

    private NatReportMsgFactory() {
    }

    public static NatReportMsg fromBuffer(ByteBuf buffer)
            throws MessageDecodingException {
        return (NatReportMsg) new NatReportMsgFactory().decode(buffer);
    }

    @Override
    protected NatReportMsg process(ByteBuf buffer) throws MessageDecodingException {
         List<NatReport> natReports = UserTypesDecoderFactory.readListNatReports(buffer);
        return new NatReportMsg(vodSrc, vodDest, natReports);
    }
}
