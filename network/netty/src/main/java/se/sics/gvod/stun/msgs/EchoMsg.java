/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.stun.msgs;

import io.netty.buffer.ByteBuf;

import java.util.Set;

import se.sics.gvod.address.Address;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.net.BaseMsgFrameDecoder;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.msgs.RewriteableRetryTimeout;
import se.sics.gvod.net.msgs.ScheduleRetryTimeout;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.TimeoutId;

/**
 * TEST_1 discovers port mapping and allocation policies.
 * TEST_1 is the server replies with the the public IP
 * it received the msg on. TEST_1 performed on 2 stun servers
 * to test (1) for presence of NAT, and (2) for presence of a firewall.
 *
 * TEST_2 and TEST_3 discover port filtering policy.
 * TEST_2 involves sending req to StunServer1 who replies over a
 * different socket bound on a different port.
 *
 * TEST_3 involves sending req to StunServer1 who delegates to
 * StunServer2 who sends response.
 * 
 * @author jdowling
 */
public class EchoMsg {

    public static enum Test {

        UDP_BLOCKED(0), PING(1), HEARTBEAT(2), FAILED_NO_PARTNER(3);

        private final int id;
        private Test(int id) {
            this.id = id;
        }
        public static Test create(int id) {
            switch(id) {
                case 0: return UDP_BLOCKED;
                case 1: return PING;
                case 2: return HEARTBEAT;
                case 3: return FAILED_NO_PARTNER;
            }
            return null;
        }

        public int getId() {
            return id;
        }
    };

    public static final class Request extends StunRequestMsg {

        static final long serialVersionUID = 1L;
        // Used to check for firewall and for determining the allocation and mapping policies
        private final Test testType;
        // try 1 to 8. for determining mapping and allocation
        private int tryId = 0;

        // Used in the Try tests to reply to different source ports
        private final Address replyTo;        
        
        public Request(VodAddress src, VodAddress dest, Test testType,
                long transactionId) {
            this(src,dest,testType, transactionId, src.getPeerAddress());
        }

        public Request(VodAddress src, VodAddress dest, Test testType,
                long transactionId, Address replyTo) {
            this(src,dest,testType,transactionId,replyTo, 0);
        }
        
        
        public Request(VodAddress src, VodAddress dest, Test testType,
                long transactionId, Address replyTo, int tryId) {
            super(src, dest, transactionId);
            this.testType = testType;
            this.tryId = tryId;
            if (replyTo == null) {
                throw new NullPointerException("Reply-to field cannot be null");
            }
            this.replyTo = replyTo;
            
        }

        public Address getReplyTo() {
            return replyTo;
        }
        
        public void setTryId(int tryId) {
            this.tryId = tryId;
        }

        public int getTryId() {
            return tryId;
        }

        public Test getTestType() {
            return testType;
        }

        @Override
        public String toString() {
            return "Echo.RequestMessage. Type " + testType + " tryId " + tryId;
        }

        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + 1 /*test type*/
                    + 1 /*try id*/
                    + UserTypesEncoderFactory.ADDRESS_LEN // replyTo
                    ;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, testType.getId());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, tryId);
            UserTypesEncoderFactory.writeAddress(buffer, replyTo);
            return buffer;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.ECHO_REQUEST;
        }

        @Override
        public RewriteableMsg copy() {
            EchoMsg.Request copy = new EchoMsg.Request(vodSrc, vodDest, testType, transactionId, replyTo, tryId);
            copy.setTimeoutId(timeoutId);
            return copy;
        }
    }

    public final static class Response extends StunResponseMsg {

        static final long serialVersionUID = 1L;
        private final int partnerPort;
        private final Test testType;
        private int tryId;
        private final Set<Address> partners;
        private final int bestPartnerRto;

        public Response(VodAddress src, VodAddress dest,
                Set<Address> partners, int bestPartnerRto, 
                Test testType, long transactionId, TimeoutId timeoutId,
                int partnerPort) {
            this(src, dest, dest.getPeerAddress(), partners, 
                    bestPartnerRto, testType,transactionId, timeoutId, partnerPort);
        }

        public Response(VodAddress src, VodAddress dest, Address replyPublicIp,
                Set<Address> partnerRtos, int bestPartnerRto, Test testType,
                long transactionId, TimeoutId timeoutId,
                int partnerPort) {
            this(src, dest, replyPublicIp, partnerRtos, bestPartnerRto, testType, transactionId,
                    timeoutId, partnerPort, 0);
        }

        public Response(VodAddress src, VodAddress dest, Address replyPublicIp,
                Set<Address> partners, int bestPartnerRto, Test testType,
                long transactionId, TimeoutId timeoutId,
                int partnerPort, int tryId) {
            super(src, dest, replyPublicIp, transactionId, timeoutId);
            this.partners = partners;
            this.bestPartnerRto = bestPartnerRto;
            this.partnerPort = partnerPort;
            this.testType = testType;
            this.tryId = tryId;
        }

        public void setTryId(int tryId){
            this.tryId = tryId;
        }

        public int getTryId() {
            return tryId;
        }

        public Test getTestType() {
            return testType;
        }

        public int getPortChange() {
            return partnerPort;
        }

        public Set<Address> getPartners() {
            return partners;
        }

        public int getBestPartnerRto() {
            return bestPartnerRto;
        }

        
        @Override
        public int getSize() {
            return super.getHeaderSize()
                    + 2 /*port*/
                    + 1 /* test type*/
                    + 1 /* try id */
                    + UserTypesEncoderFactory.getListAddressSize(partners)
                    + 4 /* best partner rto */
                    ;
        }

        @Override
        public ByteBuf toByteArray() throws MessageEncodingException {
        	ByteBuf buffer = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, partnerPort);
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, testType.getId());
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, tryId);
            UserTypesEncoderFactory.writeListAddresses(buffer, partners);
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, bestPartnerRto);
            return buffer;
        }

        @Override
        public byte getOpcode() {
            return BaseMsgFrameDecoder.ECHO_RESPONSE;
        }

        @Override
        public RewriteableMsg copy() {
            return new EchoMsg.Response(vodSrc, vodDest, replyAddr, partners,
                    bestPartnerRto, testType, transactionId, timeoutId, partnerPort, tryId);
        }
    }

    public static final class RequestRetryTimeout extends RewriteableRetryTimeout {

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
