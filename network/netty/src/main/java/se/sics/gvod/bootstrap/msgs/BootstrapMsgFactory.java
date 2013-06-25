package se.sics.gvod.bootstrap.msgs;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.VodMsgNettyFactory;
import se.sics.gvod.net.msgs.VodMsg;
import se.sics.gvod.net.util.UserTypesDecoderFactory;

public class BootstrapMsgFactory {

    public static class GetPeersRequestFactory extends VodMsgNettyFactory {

        private GetPeersRequestFactory() {
        }

        public static BootstrapMsg.GetPeersRequest fromBuffer(ChannelBuffer buffer) 
                
                throws MessageDecodingException {
            return (BootstrapMsg.GetPeersRequest) 
                    new BootstrapMsgFactory.GetPeersRequestFactory().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            int overlay = buffer.readInt();
            int utility = buffer.readInt();
            return new BootstrapMsg.GetPeersRequest(vodSrc, vodDest, 
                    overlay, utility);
        }
    }

    public static class GetPeersResponse extends VodMsgNettyFactory {

        private GetPeersResponse() {
        }

        public static BootstrapMsg.GetPeersResponse fromBuffer(ChannelBuffer buffer) 
                
                throws MessageDecodingException {
            return (BootstrapMsg.GetPeersResponse)
                    new BootstrapMsgFactory.GetPeersResponse().decode(buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            int overlay = buffer.readInt();
            List<VodDescriptor> entries = 
                    UserTypesDecoderFactory.readListGVodNodeDescriptors(buffer);
            return new BootstrapMsg.GetPeersResponse(vodSrc, vodDest, 
                    timeoutId, overlay, entries);
        }
    }

    public static class BootstrapHeartbeatFactory extends VodMsgNettyFactory {

        private BootstrapHeartbeatFactory() {
        }

        public static BootstrapMsg.Heartbeat fromBuffer(ChannelBuffer buffer) 
                
                throws MessageDecodingException {
            return (BootstrapMsg.Heartbeat) new BootstrapMsgFactory.BootstrapHeartbeatFactory().decode(
                    buffer, false);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            short mtu = buffer.readShort();
            Set<Integer> seeders = UserTypesDecoderFactory.readSetInts(buffer);
            Map<Integer,Integer> downloaders = UserTypesDecoderFactory.readMapIntInts(buffer);
            return new BootstrapMsg.Heartbeat(vodSrc, vodDest, mtu, seeders, downloaders);
        }
    }
    
    public static class AddOverlayReq extends VodMsgNettyFactory {

        private AddOverlayReq() {
        }

        public static BootstrapMsg.AddOverlayReq fromBuffer(ChannelBuffer buffer) 
                throws MessageDecodingException {
            return  
                    (BootstrapMsg.AddOverlayReq) new BootstrapMsgFactory.AddOverlayReq().decode(
                    buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            String overlayName = UserTypesDecoderFactory.readStringLength256(buffer);
            int overlayId = buffer.readInt();
            String description = UserTypesDecoderFactory.readStringLength256(buffer);
            byte[] torrentData = UserTypesDecoderFactory.readBytesLength65536(buffer);
            String imageUrl = UserTypesDecoderFactory.readStringLength256(buffer);
            int part = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            int numParts = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
            return new BootstrapMsg.AddOverlayReq(vodSrc, vodDest, overlayName,
                    overlayId, description, torrentData, imageUrl, part, numParts);
        }
    }
    
    
    public static class AddOverlayResp extends VodMsgNettyFactory {

        private AddOverlayResp() {
        }

        public static BootstrapMsg.AddOverlayResp fromBuffer(ChannelBuffer buffer) 
                throws MessageDecodingException {
            return  
                    (BootstrapMsg.AddOverlayResp) new BootstrapMsgFactory.AddOverlayResp().decode(
                    buffer, true);
        }

        @Override
        protected VodMsg process(ChannelBuffer buffer) throws MessageDecodingException {
            int overlayId = buffer.readInt();
            boolean success = UserTypesDecoderFactory.readBoolean(buffer);
            boolean finished = UserTypesDecoderFactory.readBoolean(buffer);
            return new BootstrapMsg.AddOverlayResp(vodSrc, vodDest, overlayId, 
                    success, finished, timeoutId);
        }
    }    
    
};
