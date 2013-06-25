/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.peersearch.msgs;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.common.msgs.OpCode;
import se.sics.gvod.common.msgs.RelayMsgNetty;
import se.sics.gvod.common.msgs.VodMsgNetty;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public class SearchMsg {

    public static class IllegalSearchString extends Exception {

        public IllegalSearchString(String message) {
            super(message);
        }
        
    }
    
    public static class Request extends VodMsgNetty {
        
        private final String query;

        public Request(VodAddress source, VodAddress destination, 
                TimeoutId timeoutId, String query) throws IllegalSearchString
        {
            super(source, destination, timeoutId);
            if (query.length() > 255) {
                throw new IllegalSearchString("Search string is too long. Max length is 255 chars.");
            }
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + 30 // guess at length of query
                    ;
        }

        @Override
        public RewriteableMsg copy() {
             SearchMsg.Request r = null;
            try {
                r = new SearchMsg.Request(vodSrc, vodDest, timeoutId, query);
            } catch (IllegalSearchString ex) {
                // we can swallow the exception because the original object should 
                // have been correctly constructed.
                Logger.getLogger(SearchMsg.class.getName()).log(Level.SEVERE, null, ex);
            }
             return r;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeStringLength256(buffer, query);
            return buffer;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.SEARCH_REQUEST;
        }
    }

    public static class Response extends VodMsgNetty {
        
        public static final int MAX_RESULTS_STR_LEN = 1400;
        
        private final String results;
        private final int numResponses;
        private final int responseNumber;
        
        public Response(VodAddress source,
                VodAddress destination, TimeoutId timeoutId, 
                int numResponses, int responseNumber,
                String results) throws IllegalSearchString
        {
            super(source, destination, timeoutId);
            if (results.length() > MAX_RESULTS_STR_LEN ) {
                throw new IllegalSearchString(("Size of results string is too large. It was " 
                        + results.length() + " Max size allowed is: " + MAX_RESULTS_STR_LEN));
            }
            this.numResponses = numResponses;
            this.responseNumber = responseNumber;
            this.results = results;
        }

        public String getResults() {
            return results;
        }

        public int getResponseNumber() {
            return responseNumber;
        }

        public int getNumResponses() {
            return numResponses;
        }
        
        @Override
        public int getSize() {
            return getHeaderSize()
                    + 4 // numResponses
                    + 4 // responseNum
                    + MAX_RESULTS_STR_LEN; 
        }

        @Override
        public RewriteableMsg copy() {
            try {
                return new SearchMsg.Response(vodSrc, vodDest, timeoutId,            
                        numResponses, responseNumber, results);
            } catch (IllegalSearchString ex) {
                // we can swallow the exception because the original object should 
                // have been correctly constructed.
                Logger.getLogger(SearchMsg.class.getName()).log(Level.SEVERE, null, ex);
            }
            // shouldn't get here.
            return null;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, numResponses);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, responseNumber);
            UserTypesEncoderFactory.writeStringLength65536(buffer, results);
            return buffer;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.SEARCH_RESPONSE;

        }
    }

    public static class RequestTimeout extends RewriteableRetryTimeout {

        public RequestTimeout(ScheduleRetryTimeout st, RewriteableMsg retryMessage) {
            super(st, retryMessage);
        }
    }
}
