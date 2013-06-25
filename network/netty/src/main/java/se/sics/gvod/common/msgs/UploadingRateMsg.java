/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.gvod.common.msgs;

import se.sics.gvod.net.VodAddress;
import org.jboss.netty.buffer.ChannelBuffer;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UserTypesEncoderFactory;
import se.sics.gvod.timer.ScheduleTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author gautier
 */
public class UploadingRateMsg {

    public static class Request extends VodMsgNetty {

        private VodAddress target;

        public Request(VodAddress source, VodAddress destination,
                TimeoutId timeoutId, VodAddress target) {
            super(source, destination, timeoutId);
            this.target = target;
        }

        public VodAddress getTarget() {
            return target;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.UPLOADING_RATE_REQUEST;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + UserTypesEncoderFactory.VOD_ADDRESS_LEN_NO_PARENTS
                    ;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buf = createChannelBufferWithHeader();
//            UserTypesEncoderFactory.writeAddress(buf, destination);
            UserTypesEncoderFactory.writeVodAddress(buf, target);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new UploadingRateMsg.Request(vodSrc, target, timeoutId, target);
        }
    }

    public static class Response extends VodMsgNetty {

        private VodAddress target;
        private final int rate;

        public Response(VodAddress source, VodAddress destination, TimeoutId timeoutId
                , VodAddress target, int rate) {
            super(source, destination, timeoutId);
            this.target = target;
            this.rate = rate;
        }

        public int getRate() {
            return rate;
        }

        public VodAddress getTarget() {
            return target;
        }

        @Override
        public OpCode getOpcode() {
            return OpCode.UPLOADING_RATE_RESPONSE;
        }

        @Override
        public int getSize() {
            return getHeaderSize()
                    + UserTypesEncoderFactory.VOD_ADDRESS_LEN_NO_PARENTS
                    + 4 /* rate */
                    ;
        }

        @Override
        public ChannelBuffer toByteArray() throws MessageEncodingException {
            ChannelBuffer buf = createChannelBufferWithHeader();
            UserTypesEncoderFactory.writeVodAddress(buf, target);
            buf.writeInt(rate);
            return buf;
        }

        @Override
        public RewriteableMsg copy() {
            return new UploadingRateMsg.Response(vodSrc, target, timeoutId, target, rate);
        }
    }

    public static class UploadingRateTimeout extends Timeout {

        public UploadingRateTimeout(ScheduleTimeout request) {
            super(request);
        }
    }
}
