/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.croupier.msgs;

import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.DescriptorBuffer;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.RelayMsgNetty;
import se.sics.gvod.common.msgs.RelayMsgNettyFactory;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

/**
 *
 */
public class ShuffleMsgFactory {

    
    public static class Request extends RelayMsgNettyFactory.Request {

        public static ShuffleMsg.Request fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (ShuffleMsg.Request) new ShuffleMsgFactory.Request().decode(buffer, true);
        }
        
        @Override
        protected RelayMsgNetty.Request process(ChannelBuffer buffer) throws MessageDecodingException {
            DescriptorBuffer descBuf = UserTypesDecoderFactory.readDescriptorBuffer(buffer);
            VodDescriptor nodeDescriptor = UserTypesDecoderFactory.readGVodNodeDescriptor(buffer);
            
            
            return new ShuffleMsg.Request(gvodSrc, gvodDest, descBuf, nodeDescriptor);
        }

    }

    public static class Response extends RelayMsgNettyFactory.Response {

        public static ShuffleMsg.Response fromBuffer(ChannelBuffer buffer)
                throws MessageDecodingException {
            return (ShuffleMsg.Response) new ShuffleMsgFactory.Response().decode(buffer, true);
        }
        
        @Override
        protected RelayMsgNetty.Response process(ChannelBuffer buffer) throws MessageDecodingException {
            DescriptorBuffer descBuf = UserTypesDecoderFactory.readDescriptorBuffer(buffer);
            VodDescriptor nodeDescriptor = UserTypesDecoderFactory.readGVodNodeDescriptor(buffer);
            return new ShuffleMsg.Response(gvodSrc, gvodDest, clientId, remoteId, nextDest, 
                    timeoutId, status, descBuf, nodeDescriptor);
        }
    }
}
