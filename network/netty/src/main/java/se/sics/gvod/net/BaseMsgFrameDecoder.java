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
    protected RewriteableMsg decodeMsg(ChannelHandlerContext ctx,
            Channel channel, ChannelBuffer buffer) throws MessageDecodingException {

        // PEERSEARCH MSGS
        switch (opKod) {
            case OpKod.SEARCH_REQUEST:
                return SearchMsgFactory.Request.fromBuffer(buffer);
            case OpKod.SEARCH_RESPONSE:
                return SearchMsgFactory.Response.fromBuffer(buffer);
            // COMMON MSGS
            case OpKod.CONNECT_REQUEST:
                return ConnectMsgFactory.Request.fromBuffer(buffer);
            case OpKod.CONNECT_RESPONSE:
                return ConnectMsgFactory.Response.fromBuffer(buffer);
            case OpKod.DISCONNECT_REQUEST:
                return DisconnectMsgFactory.Request.fromBuffer(buffer);
            case OpKod.DISCONNECT_RESPONSE:
                return DisconnectMsgFactory.Response.fromBuffer(buffer);
            case OpKod.DATAOFFER:
                return DataOfferMsgFactory.fromBuffer(buffer);
            case OpKod.LEAVE:
                return LeaveMsgFactory.fromBuffer(buffer);
            case OpKod.REFERENCES_REQUEST:
                return ReferencesMsgFactory.Request.fromBuffer(buffer);
            case OpKod.REFERENCES_RESPONSE:
                return ReferencesMsgFactory.Response.fromBuffer(buffer);
            case OpKod.UPLOADING_RATE_REQUEST:
                return UploadingRateMsgFactory.Request.fromBuffer(buffer);
            case OpKod.UPLOADING_RATE_RESPONSE:
                return UploadingRateMsgFactory.Response.fromBuffer(buffer);
            case OpKod.D_REQUEST:
                return DataMsgFactory.Request.fromBuffer(buffer);
            case OpKod.D_RESPONSE:
                return DataMsgFactory.Response.fromBuffer(buffer);
            case OpKod.PIECE_NOT_AVAILABLE:
                return DataMsgFactory.PieceNotAvailable.fromBuffer(buffer);
            case OpKod.SATURATED:
                return DataMsgFactory.Saturated.fromBuffer(buffer);
            case OpKod.ACK:
                return DataMsgFactory.Ack.fromBuffer(buffer);
            case OpKod.HASH_REQUEST:
                return DataMsgFactory.HashRequest.fromBuffer(buffer);
            case OpKod.HASH_RESPONSE:
                return DataMsgFactory.HashResponse.fromBuffer(buffer);
            // GRADIENT MSGS
            case OpKod.SETS_EXCHANGE_REQUEST:
                return SetsExchangeMsgFactory.Request.fromBuffer(buffer);
            case OpKod.SETS_EXCHANGE_RESPONSE:
                return SetsExchangeMsgFactory.Response.fromBuffer(buffer);
            case OpKod.TARGET_UTILITY_PROBE_REQUEST:
                return GradientSearchMsgFactory.Request.fromBuffer(buffer);
            case OpKod.TARGET_UTILITY_PROBE_RESPONSE:
                return GradientSearchMsgFactory.Response.fromBuffer(buffer);
            // STUN MSGS
            case OpKod.ECHO_REQUEST:
                return EchoMsgFactory.Request.fromBuffer(buffer);
            case OpKod.ECHO_RESPONSE:
                return EchoMsgFactory.Response.fromBuffer(buffer);
            case OpKod.ECHO_CHANGE_IP_AND_PORT_REQUEST:
                return EchoChangeIpAndPortMsgFactory.Request.fromBuffer(buffer);
            case OpKod.ECHO_CHANGE_IP_AND_PORT_RESPONSE:
                return EchoChangeIpAndPortMsgFactory.Response.fromBuffer(buffer);
            case OpKod.ECHO_CHANGE_PORT_REQUEST:
                return EchoChangePortMsgFactory.Request.fromBuffer(buffer);
            case OpKod.ECHO_CHANGE_PORT_RESPONSE:
                return EchoChangePortMsgFactory.Response.fromBuffer(buffer);
            case OpKod.SERVER_HOST_CHANGE_REQUEST:
                return ServerHostChangeMsgFactory.Request.fromBuffer(buffer);
            case OpKod.SERVER_HOST_CHANGE_RESPONSE:
                return ServerHostChangeMsgFactory.Response.fromBuffer(buffer);
            case OpKod.REPORT_REQUEST:
                return ReportMsgFactory.Request.fromBuffer(buffer);
            case OpKod.REPORT_RESPONSE:
                return ReportMsgFactory.Response.fromBuffer(buffer);
            // HOLE PUNCHING MSGS
            case OpKod.GO_MSG:
                return GoMsgFactory.Request.fromBuffer(buffer);
            case OpKod.DELETE_CONNECTION:
                return DeleteConnectionMsgFactory.fromBuffer(buffer);
            case OpKod.HOLE_PUNCHING_REQUEST:
                return HolePunchingMsgFactory.Request.fromBuffer(buffer);
            case OpKod.HOLE_PUNCHING_RESPONSE:
                return HolePunchingMsgFactory.Response.fromBuffer(buffer);
            case OpKod.HOLE_PUNCHING_RESPONSE_ACK:
                return HolePunchingMsgFactory.ResponseAck.fromBuffer(buffer);
            case OpKod.HP_FINISHED:
                return HpFinishedMsgFactory.Request.fromBuffer(buffer);
            case OpKod.INTERLEAVED_PRC_OPENHOLE_REQUEST:
                return Interleaved_PRC_OpenHoleMsgFactory.Request.fromBuffer(buffer);
            case OpKod.INTERLEAVED_PRC_OPENHOLE_RESPONSE:
                return Interleaved_PRC_OpenHoleMsgFactory.Response.fromBuffer(buffer);
            case OpKod.INTERLEAVED_PRC_SERVER_REQ_PRED_MSG:
                return Interleaved_PRC_ServersRequestForPredictionMsgFactory.Request.fromBuffer(buffer);
            case OpKod.INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_REQUEST:
                return Interleaved_PRP_ConnectMsgFactory.Request.fromBuffer(buffer);
            case OpKod.INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_RESPONSE:
                return Interleaved_PRP_ConnectMsgFactory.Response.fromBuffer(buffer);
            case OpKod.INTERLEAVED_PRP_SERVERS_REQ_AVAILABLE_PORTS_MSG:
                return Interleaved_PRP_ServerRequestForAvailablePortsMsgFactory.Request.fromBuffer(buffer);
            case OpKod.PRC_OPENHOLE_REQUEST:
                return PRC_OpenHoleMsgFactory.Request.fromBuffer(buffer);
            case OpKod.PRC_OPENHOLE_RESPONSE:
                return PRC_OpenHoleMsgFactory.Response.fromBuffer(buffer);
            case OpKod.PRC_SERVER_REQ_CONSEC_MSG:
                return PRC_ServerRequestForConsecutiveMsgFactory.Request.fromBuffer(buffer);
            case OpKod.PRP_SEND_PORTS_ZSERVER_REQUEST:
                return PRP_ConnectMsgFactory.Request.fromBuffer(buffer);
            case OpKod.PRP_SEND_PORTS_ZSERVER_RESPONSE:
                return PRP_ConnectMsgFactory.Response.fromBuffer(buffer);
            case OpKod.PRP_SERVER_REQ_AVAILABLE_PORTS_MSG:
                return PRP_ServerRequestForAvailablePortsMsgFactory.Request.fromBuffer(buffer);
            case OpKod.PRP_PREALLOCATED_PORTS_REQUEST:
                return PRP_PreallocatedPortsMsgFactory.Request.fromBuffer(buffer);
            case OpKod.PRP_PREALLOCATED_PORTS_RESPONSE:
                return PRP_PreallocatedPortsMsgFactory.Response.fromBuffer(buffer);
            case OpKod.HP_REGISTER_REQUEST:
                return HpRegisterMsgFactory.Request.fromBuffer(buffer);
            case OpKod.HP_REGISTER_RESPONSE:
                return HpRegisterMsgFactory.Response.fromBuffer(buffer);
            case OpKod.HP_UNREGISTER_REQUEST:
                return HpUnregisterMsgFactory.Request.fromBuffer(buffer);
            case OpKod.HP_UNREGISTER_RESPONSE:
                return HpUnregisterMsgFactory.Response.fromBuffer(buffer);
            case OpKod.RELAY_CLIENT_TO_SERVER:
                return RelayRequestMsgFactory.Request.fromBuffer(buffer);
            case OpKod.RELAY_SERVER_TO_CLIENT:
                return RelayRequestMsgFactory.Response.fromBuffer(buffer);
            case OpKod.SHP_INITIATE_SHP:
                return SHP_InitiateSimpleHolePunchingMsgFactory.Request.fromBuffer(buffer);
            case OpKod.SHP_OPENHOLE_INITIATOR:
                return SHP_OpenHoleMsgFactory.Initiator.fromBuffer(buffer);
            case OpKod.PARENT_KEEP_ALIVE_REQUEST:
                return ParentKeepAliveMsgFactory.Request.fromBuffer(buffer);
            case OpKod.PARENT_KEEP_ALIVE_RESPONSE:
                return ParentKeepAliveMsgFactory.Response.fromBuffer(buffer);
            case OpKod.HP_KEEP_ALIVE_REQUEST:
                return HpKeepAliveMsgFactory.Request.fromBuffer(buffer);
            case OpKod.HP_KEEP_ALIVE_RESPONSE:
                return HpKeepAliveMsgFactory.Response.fromBuffer(buffer);
            case OpKod.HP_FEASABILITY_REQUEST:
                return HpConnectMsgFactory.Request.fromBuffer(buffer);
            case OpKod.HP_FEASABILITY_RESPONSE:
                return HpConnectMsgFactory.Response.fromBuffer(buffer);
            case OpKod.SHUFFLE_REQUEST:
                return ShuffleMsgFactory.Request.fromBuffer(buffer);
            case OpKod.SHUFFLE_RESPONSE:
                return ShuffleMsgFactory.Response.fromBuffer(buffer);
            default:
                break;
        }

        return null;
    }
}
