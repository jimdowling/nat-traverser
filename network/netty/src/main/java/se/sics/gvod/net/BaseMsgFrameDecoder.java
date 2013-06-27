/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.msgs.ConnectMsgFactory;
import se.sics.gvod.common.msgs.DataMsgFactory;
import se.sics.gvod.common.msgs.DataOfferMsgFactory;
import se.sics.gvod.common.msgs.DisconnectMsgFactory;
import se.sics.gvod.common.msgs.LeaveMsgFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.OpKod;
import se.sics.gvod.common.msgs.ReferencesMsgFactory;
import se.sics.gvod.common.msgs.SearchMsgFactory;
import se.sics.gvod.common.msgs.UploadingRateMsgFactory;
import se.sics.gvod.croupier.msgs.ShuffleMsgFactory;
import se.sics.gvod.gradient.msgs.GradientSearchMsgFactory;
import se.sics.gvod.gradient.msgs.SetsExchangeMsgFactory;
import se.sics.gvod.hp.msgs.DeleteConnectionMsgFactory;
import se.sics.gvod.hp.msgs.GoMsgFactory;
import se.sics.gvod.hp.msgs.HolePunchingMsgFactory;
import se.sics.gvod.hp.msgs.HpConnectMsgFactory;
import se.sics.gvod.hp.msgs.HpFinishedMsgFactory;
import se.sics.gvod.hp.msgs.HpKeepAliveMsgFactory;
import se.sics.gvod.hp.msgs.HpRegisterMsgFactory;
import se.sics.gvod.hp.msgs.HpUnregisterMsgFactory;
import se.sics.gvod.hp.msgs.Interleaved_PRC_OpenHoleMsgFactory;
import se.sics.gvod.hp.msgs.Interleaved_PRC_ServersRequestForPredictionMsgFactory;
import se.sics.gvod.hp.msgs.Interleaved_PRP_ConnectMsgFactory;
import se.sics.gvod.hp.msgs.Interleaved_PRP_ServerRequestForAvailablePortsMsgFactory;
import se.sics.gvod.hp.msgs.PRC_OpenHoleMsgFactory;
import se.sics.gvod.hp.msgs.PRC_ServerRequestForConsecutiveMsgFactory;
import se.sics.gvod.hp.msgs.PRP_ConnectMsgFactory;
import se.sics.gvod.hp.msgs.PRP_PreallocatedPortsMsgFactory;
import se.sics.gvod.hp.msgs.PRP_ServerRequestForAvailablePortsMsgFactory;
import se.sics.gvod.hp.msgs.ParentKeepAliveMsgFactory;
import se.sics.gvod.hp.msgs.RelayRequestMsgFactory;
import se.sics.gvod.hp.msgs.SHP_InitiateSimpleHolePunchingMsgFactory;
import se.sics.gvod.hp.msgs.SHP_OpenHoleMsgFactory;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.stun.msgs.EchoChangeIpAndPortMsgFactory;
import se.sics.gvod.stun.msgs.EchoChangePortMsgFactory;
import se.sics.gvod.stun.msgs.EchoMsgFactory;
import se.sics.gvod.stun.msgs.ReportMsgFactory;
import se.sics.gvod.stun.msgs.ServerHostChangeMsgFactory;

/**
 *
 * @author jdowling
 */
public class BaseMsgFrameDecoder extends MsgFrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(BaseMsgFrameDecoder.class);

    public BaseMsgFrameDecoder() {
        super();
    }

    /**
     * Subclasses should call super() on their first line, and if a msg is
     * returned, then return, else test msgs in this class.
     *
     * @param ctx
     * @param channel
     * @param buffer
     * @return
     * @throws MessageDecodingException
     */
    @Override
    protected RewriteableMsg decodeMsg(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws MessageDecodingException {

        // PEERSEARCH MSGS
        if (opKod.getByte() == OpKod.SEARCH_REQUEST.getByte()) {
            return SearchMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.SEARCH_RESPONSE.getByte()) {
            return SearchMsgFactory.Response.fromBuffer(buffer);
        } // COMMON MSGS
        else if (opKod.getByte() == OpKod.CONNECT_REQUEST.getByte()) {
            return ConnectMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.CONNECT_RESPONSE.getByte()) {
            return ConnectMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.DISCONNECT_REQUEST.getByte()) {
            return DisconnectMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.DISCONNECT_RESPONSE.getByte()) {
            return DisconnectMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.DATAOFFER.getByte()) {
            return DataOfferMsgFactory.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.LEAVE.getByte()) {
            return LeaveMsgFactory.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.REFERENCES_REQUEST.getByte()) {
            return ReferencesMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.REFERENCES_RESPONSE.getByte()) {
            return ReferencesMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.UPLOADING_RATE_REQUEST.getByte()) {
            return UploadingRateMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.UPLOADING_RATE_RESPONSE.getByte()) {
            return UploadingRateMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.D_REQUEST.getByte()) {
            return DataMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.D_RESPONSE.getByte()) {
            return DataMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PIECE_NOT_AVAILABLE.getByte()) {
            return DataMsgFactory.PieceNotAvailable.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.SATURATED.getByte()) {
            return DataMsgFactory.Saturated.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.ACK.getByte()) {
            return DataMsgFactory.Ack.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HASH_REQUEST.getByte()) {
            return DataMsgFactory.HashRequest.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HASH_RESPONSE.getByte()) {
            return DataMsgFactory.HashResponse.fromBuffer(buffer);
        } // GRADIENT MSGS
        else if (opKod.getByte() == OpKod.SETS_EXCHANGE_REQUEST.getByte()) {
            return SetsExchangeMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.SETS_EXCHANGE_RESPONSE.getByte()) {
            return SetsExchangeMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.TARGET_UTILITY_PROBE_REQUEST.getByte()) {
            return GradientSearchMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.TARGET_UTILITY_PROBE_RESPONSE.getByte()) {
            return GradientSearchMsgFactory.Response.fromBuffer(buffer);
        } // STUN MSGS
        else if (opKod.getByte() == OpKod.ECHO_REQUEST.getByte()) {
            return EchoMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.ECHO_RESPONSE.getByte()) {
            return EchoMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.ECHO_CHANGE_IP_AND_PORT_REQUEST.getByte()) {
            return EchoChangeIpAndPortMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.ECHO_CHANGE_IP_AND_PORT_RESPONSE.getByte()) {
            return EchoChangeIpAndPortMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.ECHO_CHANGE_PORT_REQUEST.getByte()) {
            return EchoChangePortMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.ECHO_CHANGE_PORT_RESPONSE.getByte()) {
            return EchoChangePortMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.SERVER_HOST_CHANGE_REQUEST.getByte()) {
            return ServerHostChangeMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.SERVER_HOST_CHANGE_RESPONSE.getByte()) {
            return ServerHostChangeMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.REPORT_REQUEST.getByte()) {
            return ReportMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.REPORT_RESPONSE.getByte()) {
            return ReportMsgFactory.Response.fromBuffer(buffer);
        } // HOLE PUNCHING MSGS
        else if (opKod.getByte() == OpKod.GO_MSG.getByte()) {
            return GoMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.DELETE_CONNECTION.getByte()) {
            return DeleteConnectionMsgFactory.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HOLE_PUNCHING_REQUEST.getByte()) {
            return HolePunchingMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HOLE_PUNCHING_RESPONSE.getByte()) {
            return HolePunchingMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HOLE_PUNCHING_RESPONSE_ACK.getByte()) {
            return HolePunchingMsgFactory.ResponseAck.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HP_FINISHED.getByte()) {
            return HpFinishedMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.INTERLEAVED_PRC_OPENHOLE_REQUEST.getByte()) {
            return Interleaved_PRC_OpenHoleMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.INTERLEAVED_PRC_OPENHOLE_RESPONSE.getByte()) {
            return Interleaved_PRC_OpenHoleMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.INTERLEAVED_PRC_SERVER_REQ_PRED_MSG.getByte()) {
            return Interleaved_PRC_ServersRequestForPredictionMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_REQUEST.getByte()) {
            return Interleaved_PRP_ConnectMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_RESPONSE.getByte()) {
            return Interleaved_PRP_ConnectMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.INTERLEAVED_PRP_SERVERS_REQ_AVAILABLE_PORTS_MSG.getByte()) {
            return Interleaved_PRP_ServerRequestForAvailablePortsMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PRC_OPENHOLE_REQUEST.getByte()) {
            return PRC_OpenHoleMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PRC_OPENHOLE_RESPONSE.getByte()) {
            return PRC_OpenHoleMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PRC_SERVER_REQ_CONSEC_MSG.getByte()) {
            return PRC_ServerRequestForConsecutiveMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PRP_SEND_PORTS_ZSERVER_REQUEST.getByte()) {
            return PRP_ConnectMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PRP_SEND_PORTS_ZSERVER_RESPONSE.getByte()) {
            return PRP_ConnectMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PRP_SERVER_REQ_AVAILABLE_PORTS_MSG.getByte()) {
            return PRP_ServerRequestForAvailablePortsMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PRP_PREALLOCATED_PORTS_REQUEST.getByte()) {
            return PRP_PreallocatedPortsMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PRP_PREALLOCATED_PORTS_RESPONSE.getByte()) {
            return PRP_PreallocatedPortsMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HP_REGISTER_REQUEST.getByte()) {
            return HpRegisterMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HP_REGISTER_RESPONSE.getByte()) {
            return HpRegisterMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HP_UNREGISTER_REQUEST.getByte()) {
            return HpUnregisterMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HP_UNREGISTER_RESPONSE.getByte()) {
            return HpUnregisterMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.RELAY_CLIENT_TO_SERVER.getByte()) {
            return RelayRequestMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.RELAY_SERVER_TO_CLIENT.getByte()) {
            return RelayRequestMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.SHP_INITIATE_SHP.getByte()) {
            return SHP_InitiateSimpleHolePunchingMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.SHP_OPENHOLE_INITIATOR.getByte()) {
            return SHP_OpenHoleMsgFactory.Initiator.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PARENT_KEEP_ALIVE_REQUEST.getByte()) {
            return ParentKeepAliveMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.PARENT_KEEP_ALIVE_RESPONSE.getByte()) {
            return ParentKeepAliveMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HP_KEEP_ALIVE_REQUEST.getByte()) {
            return HpKeepAliveMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HP_KEEP_ALIVE_RESPONSE.getByte()) {
            return HpKeepAliveMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HP_FEASABILITY_REQUEST.getByte()) {
            return HpConnectMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.HP_FEASABILITY_RESPONSE.getByte()) {
            return HpConnectMsgFactory.Response.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.SHUFFLE_REQUEST.getByte()) {
            return ShuffleMsgFactory.Request.fromBuffer(buffer);
        } else if (opKod.getByte() == OpKod.SHUFFLE_RESPONSE.getByte()) {
            return ShuffleMsgFactory.Response.fromBuffer(buffer);
        }

        return null;
    }
}
