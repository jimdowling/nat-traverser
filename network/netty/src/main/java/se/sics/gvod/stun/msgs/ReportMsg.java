/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.msgs;

import io.netty.buffer.ByteBuf;
import se.sics.gvod.common.msgs.DirectMsgNetty;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public class ReportMsg {

    public final static class Request extends DirectMsgNetty.Request {

        private final String report;

        public Request(VodAddress src, VodAddress dest, TimeoutId timeoutId, String report) {
            super(src, dest, timeoutId);
            this.report = report;
        }

        public String getReport() {
            return report;
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + 20 // estimate of size of string
                    ;
        }

        @Override
        public RewriteableMsg copy() {
            Request r = new Request(getVodSource(), getVodDestination(), 
                    timeoutId, report);
            return r;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeStringLength256(buffer, report);
            return buffer;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.REPORT_REQUEST;
        }
    }

    public final static class Response extends DirectMsgNetty.Response {

        public Response(VodAddress src, VodAddress dest, TimeoutId timeoutId) {
            super(src, dest, timeoutId);
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    ;
        }

        @Override
        public RewriteableMsg copy() {
            return new Response(getVodSource(), getVodDestination(), timeoutId);
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            return buffer;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.REPORT_RESPONSE;
        }
    }

    public final static class RequestTimeout extends Timeout {

        private ReportMsg.Request requestMsg;

        public RequestTimeout(ScheduleTimeout st) {
            super(st);
        }

        public void setRequestMsg(Request requestMsg) {
            this.requestMsg = requestMsg;
        }
        
        public ReportMsg.Request getRequestMsg() {
            return requestMsg;
        }
    }
}
