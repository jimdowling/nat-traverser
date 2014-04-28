/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.croupier.msgs;

import io.netty.buffer.ByteBuf;
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

        public static ShuffleMsg.Request fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (ShuffleMsg.Request) new ShuffleMsgFactory.Request().decode(buffer);
        }
        
        @Override
        protected RelayMsgNetty.Request process(ByteBuf buffer) throws MessageDecodingException {
            DescriptorBuffer descBuf = UserTypesDecoderFactory.readDescriptorBuffer(buffer);
            VodDescriptor nodeDescriptor = UserTypesDecoderFactory.readVodNodeDescriptor(buffer);
            
            
            return new ShuffleMsg.Request(gvodSrc, gvodDest, descBuf, nodeDescriptor);
        }

    }

    public static class Response extends RelayMsgNettyFactory.Response {

        public static ShuffleMsg.Response fromBuffer(ByteBuf buffer)
                throws MessageDecodingException {
            return (ShuffleMsg.Response) new ShuffleMsgFactory.Response().decode(buffer);
        }
        
        @Override
        protected RelayMsgNetty.Response process(ByteBuf buffer) throws MessageDecodingException {
            DescriptorBuffer descBuf = UserTypesDecoderFactory.readDescriptorBuffer(buffer);
            VodDescriptor nodeDescriptor = UserTypesDecoderFactory.readVodNodeDescriptor(buffer);
            return new ShuffleMsg.Response(gvodSrc, gvodDest, clientId, remoteId, nextDest, 
                    timeoutId, status, descBuf, nodeDescriptor);
        }
    }
}
