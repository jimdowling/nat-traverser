/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.bootstrap.msgs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.OpCode;
import se.sics.gvod.common.msgs.VodMsgNetty;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.NoTimeoutId;

/**
 *
 * @author Jim Dowling<jdowling@sics.se>
 */
public final class MonitorMsg extends VodMsgNetty {

    private static final long serialVersionUID = 787143338863400423L;
    private final Map<String, String> attrValues;

    public MonitorMsg(VodAddress source, VodAddress destination,
            Map<String, String> attrValues) {
        super(source, destination, new NoTimeoutId());
        assert (attrValues != null);
        this.attrValues = attrValues;
    }

    public Map<String, String> getAttrValues() {
        return attrValues;
    }

    @Override
    public int getSize() {
        return getHeaderSize()
                + attrValues.size() * 4 /*
                 * guess at size
                 */;
    }

    @Override
    public ChannelBuffer toByteArray() throws MessageEncodingException {
        ChannelBuffer buf = createChannelBufferWithHeader();
        UserTypesEncoderFactory.writeUnsignedintAsOneByte(buf, attrValues.size());
        for (Entry<String, String> pair : attrValues.entrySet()) {
            UserTypesEncoderFactory.writeStringLength256(buf, pair.getKey());
            UserTypesEncoderFactory.writeStringLength256(buf, pair.getValue());
        }
        return buf;
    }

    @Override
    public OpCode getOpcode() {
        return OpCode.MONITOR_MSG;
    }

    @Override
    public RewriteableMsg copy() {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.putAll(attrValues);
        return new MonitorMsg(vodSrc, vodDest, attrs);
    }
}