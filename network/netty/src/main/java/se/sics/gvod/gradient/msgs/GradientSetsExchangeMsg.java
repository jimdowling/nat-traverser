/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.gradient.msgs;

import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.OpCode;
import se.sics.gvod.common.msgs.RelayMsgNetty;
import se.sics.gvod.common.msgs.RelayMsgNetty.Status;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jim
 */
public class GradientSetsExchangeMsg {

    public static class Request extends RelayMsgNetty.Request {

        public Request(VodAddress source, VodAddress destination,
                int clientId, int remoteId) {
            super(source, destination, clientId, remoteId);
        }

        public Request(VodAddress source, VodAddress destination, int clientId, int remoteId,
                TimeoutId timeoutId) {
            super(source, destination, clientId, remoteId, timeoutId);
        }
        private Request(VodAddress source, VodAddress destination,
                int clientId, int remoteId, VodAddress nextDest, TimeoutId timeoutId) {
            super(source, destination, clientId, remoteId, nextDest, timeoutId);
        }


        @Override
        public OpCode getOpcode() {
            return OpCode.SETS_EXCHANGE_REQUEST;
        }

        @Override
        public RewriteableMsg copy() {
            return new GradientSetsExchangeMsg.Request(vodSrc, 
                    vodDest, clientId, remoteId, nextDest, timeoutId);
        }
    }

    public static class Response extends RelayMsgNetty.Response {

        private final List<VodDescriptor> similarPeers;

        public Response(VodAddress source, Request request,
                List<VodDescriptor> similarPeers) {
            super(source, request, Status.OK);
            this.similarPeers = similarPeers;
        }
        
        public Response(VodAddress source, VodAddress destination, 
                VodAddress nextDest, TimeoutId timeoutId,
                List<VodDescriptor> similarPeers) {
            this(source, destination, nextDest.getId(), source.getId(),
                    nextDest, timeoutId, similarPeers);
        }
        private Response(VodAddress source, VodAddress destination, 
                int clientId, int remoteId, VodAddress nextDest, 
                TimeoutId timeoutId,
                List<VodDescriptor> similarPeers) {
            super(source, destination,  clientId, remoteId, nextDest, timeoutId, Status.OK);
            this.similarPeers = similarPeers;
        }

        @Override
        public int getSize() {
            return super.getSize()
                    + UserTypesEncoderFactory.getListGVodNodeDescriptorSize(similarPeers);
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeListVodNodeDescriptors(buffer, similarPeers);
            return buffer;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.SETS_EXCHANGE_RESPONSE;
        }

        /**
         * @return the similarPeers
         */
        public List<VodDescriptor> getSimilarPeers() {
            return similarPeers;
        }

        @Override
        public RewriteableMsg copy() {
            return new GradientSetsExchangeMsg.Response(vodSrc, vodDest, 
                    clientId, remoteId, nextDest, timeoutId, similarPeers);
        }
    }

    public static class RequestRetryTimeout extends RewriteableRetryTimeout {

        private final Request requestMsg;

        public RequestRetryTimeout(ScheduleRetryTimeout st, Request requestMsg) {
            super(st, requestMsg);
            this.requestMsg = requestMsg;
        }

        public Request getRequestMsg() {
            return requestMsg;
        }
    }
}
