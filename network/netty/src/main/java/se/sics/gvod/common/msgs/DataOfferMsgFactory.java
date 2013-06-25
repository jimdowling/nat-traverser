package se.sics.gvod.common.msgs;

import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class DataOfferMsgFactory extends VodMsgNettyFactory {


    public static DataOfferMsg fromBuffer(ChannelBuffer buffer)
                
            throws MessageDecodingException {
        return (DataOfferMsg)
                new DataOfferMsgFactory().decode(buffer, false);
    }

    @Override
    protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
        UtilityVod utility = (UtilityVod) UserTypesDecoderFactory.readUtility(buffer);
        byte[] chunks = UserTypesDecoderFactory.readArrayBytes(buffer);
        byte[][] availablePieces = UserTypesDecoderFactory.readArrayArrayBytes(buffer);
        return new DataOfferMsg(vodSrc, vodDest, utility, chunks,
                availablePieces);
    }
};
