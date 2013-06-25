package se.sics.gvod.bootstrap.msgs;

import java.util.HashMap;
import java.util.Map;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.VodMsgNettyFactory;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class MonitorMsgFactory extends VodMsgNettyFactory {

    private MonitorMsgFactory() {
    }

    public static MonitorMsg fromBuffer(ChannelBuffer buffer)
            throws MessageDecodingException {
        return (MonitorMsg) new MonitorMsgFactory().decode(buffer, false);
    }

    @Override
    protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
        int size = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
        Map<String, String> attrs = new HashMap<String, String>();
        for (int i = 0; i < size; i++) {
            String key = UserTypesDecoderFactory.readStringLength256(buffer);
            String val = UserTypesDecoderFactory.readStringLength256(buffer);
            attrs.put(key, val);
        }
        return new MonitorMsg(vodSrc, vodDest, attrs);
    }
};
