/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.common.msgs.ConnectMsgFactory;
import se.sics.gvod.common.msgs.DataMsgFactory;
import se.sics.gvod.common.msgs.DataOfferMsgFactory;
import se.sics.gvod.common.msgs.DisconnectMsgFactory;
import se.sics.gvod.stun.msgs.EchoChangeIpAndPortMsgFactory;
import se.sics.gvod.stun.msgs.EchoChangePortMsgFactory;
import se.sics.gvod.stun.msgs.EchoMsgFactory;
import se.sics.gvod.common.msgs.LeaveMsgFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.common.msgs.OpCode;
import static se.sics.gvod.common.msgs.OpCode.SEARCH_REQUEST;
import static se.sics.gvod.common.msgs.OpCode.SEARCH_RESPONSE;
import static se.sics.gvod.common.msgs.OpCode.SHUFFLE_REQUEST;
import static se.sics.gvod.common.msgs.OpCode.SHUFFLE_RESPONSE;
import se.sics.gvod.common.msgs.ReferencesMsgFactory;
import se.sics.gvod.common.msgs.SearchMsgFactory;
import se.sics.gvod.stun.msgs.ServerHostChangeMsgFactory;
import se.sics.gvod.gradient.msgs.SetsExchangeMsgFactory;
import se.sics.gvod.common.msgs.UploadingRateMsgFactory;
import se.sics.gvod.croupier.msgs.ShuffleMsgFactory;
import se.sics.gvod.gradient.msgs.GradientSearchMsgFactory;
import se.sics.gvod.hp.msgs.*;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.stun.msgs.ReportMsgFactory;

/**
 *
 * http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/replay/ReplayingDecoder.html
 * 
 * @author jdowling
 */
public class MsgFrameDecoder
        extends ReplayingDecoder<DecoderState> {

    private static final Logger logger = LoggerFactory.getLogger(MsgFrameDecoder.class);
    private OpCode opCode;

    public MsgFrameDecoder() {
        // Set the initial state.
        super(DecoderState.READ_TYPE);
    }

    public Object parse(ChannelBuffer buffer) throws Exception {
        return decode(null, null, buffer, DecoderState.READ_TYPE);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx,
            Channel channel,
            ChannelBuffer buffer,
            DecoderState state) throws Exception {


        switch (state) {
            case READ_TYPE:
                Byte type = buffer.readByte();
                opCode = OpCode.fromByte(type);
                checkpoint(DecoderState.READ_CONTENT);
            case READ_CONTENT:
                // Read in the RewriteableMessage header first
                RewriteableMsg msg = null;

                switch (opCode) {
                  // PEERSEARCH MSGS
                    case SEARCH_REQUEST:
                        msg = SearchMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case SEARCH_RESPONSE:
                        msg = SearchMsgFactory.Response.fromBuffer(buffer);
                        break;
                                        
                    // COMMON MSGS
                    case CONNECT_REQUEST:
                        msg = ConnectMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case CONNECT_RESPONSE:
                        msg = ConnectMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case DISCONNECT_REQUEST:
                        msg = DisconnectMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case DISCONNECT_RESPONSE:
                        msg = DisconnectMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case DATAOFFER:
                        msg = DataOfferMsgFactory.fromBuffer(buffer);
                        break;
                    case LEAVE:
                        msg = LeaveMsgFactory.fromBuffer(buffer);
                        break;
                    case REFERENCES_REQUEST:
                        msg = ReferencesMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case REFERENCES_RESPONSE:
                        msg = ReferencesMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case UPLOADING_RATE_REQUEST:
                        msg = UploadingRateMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case UPLOADING_RATE_RESPONSE:
                        msg = UploadingRateMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case D_REQUEST:
                        msg = DataMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case D_RESPONSE:
                        msg = DataMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case PIECE_NOT_AVAILABLE:
                        msg = DataMsgFactory.PieceNotAvailable.fromBuffer(buffer);
                        break;
                    case SATURATED:
                        msg = DataMsgFactory.Saturated.fromBuffer(buffer);
                        break;
                    case ACK:
                        msg = DataMsgFactory.Ack.fromBuffer(buffer);
                        break;
                    case HASH_REQUEST:
                        msg = DataMsgFactory.HashRequest.fromBuffer(buffer);
                        break;
                    case HASH_RESPONSE:
                        msg = DataMsgFactory.HashResponse.fromBuffer(buffer);
                        break;
                    // GRADIENT MSGS
                    case SETS_EXCHANGE_REQUEST:
                        msg = SetsExchangeMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case SETS_EXCHANGE_RESPONSE:
                        msg = SetsExchangeMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case TARGET_UTILITY_PROBE_REQUEST:
                        msg = GradientSearchMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case TARGET_UTILITY_PROBE_RESPONSE:
                        msg = GradientSearchMsgFactory.Response.fromBuffer(buffer);
                        break;
                        
                    // STUN MSGS
                    case ECHO_REQUEST:
                        msg = EchoMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case ECHO_RESPONSE:
                        msg = EchoMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case ECHO_CHANGE_IP_AND_PORT_REQUEST:
                        msg = EchoChangeIpAndPortMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case ECHO_CHANGE_IP_AND_PORT_RESPONSE:
                        msg = EchoChangeIpAndPortMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case ECHO_CHANGE_PORT_REQUEST:
                        msg = EchoChangePortMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case ECHO_CHANGE_PORT_RESPONSE:
                        msg = EchoChangePortMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case SERVER_HOST_CHANGE_REQUEST:
                        msg = ServerHostChangeMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case SERVER_HOST_CHANGE_RESPONSE:
                        msg = ServerHostChangeMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case REPORT_REQUEST:
                        msg = ReportMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case REPORT_RESPONSE:
                        msg = ReportMsgFactory.Response.fromBuffer(buffer);
                        break;
                        // HOLE PUNCHING MSGS
                    case GO_MSG:
                        msg = GoMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case DELETE_CONNECTION:
                        msg = DeleteConnectionMsgFactory.fromBuffer(buffer);
                        break;
                        // TODO - the hp msgs are all the same. Could use one templated
                        // factory instead!
                    case HOLE_PUNCHING_REQUEST:
                        msg = HolePunchingMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case HOLE_PUNCHING_RESPONSE:
                        msg = HolePunchingMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case HOLE_PUNCHING_RESPONSE_ACK:
                        msg = HolePunchingMsgFactory.ResponseAck.fromBuffer(buffer);
                        break;
                    case HP_FINISHED:
                        msg = HpFinishedMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case INTERLEAVED_PRC_OPENHOLE_REQUEST:
                        msg = Interleaved_PRC_OpenHoleMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case INTERLEAVED_PRC_OPENHOLE_RESPONSE:
                        msg = Interleaved_PRC_OpenHoleMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case INTERLEAVED_PRC_SERVER_REQ_PRED_MSG:
                        msg = Interleaved_PRC_ServersRequestForPredictionMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_REQUEST:
                        msg = Interleaved_PRP_ConnectMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_RESPONSE:
                        msg = Interleaved_PRP_ConnectMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case INTERLEAVED_PRP_SERVERS_REQ_AVAILABLE_PORTS_MSG:
                        msg = Interleaved_PRP_ServerRequestForAvailablePortsMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case PRC_OPENHOLE_REQUEST:
                        msg = PRC_OpenHoleMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case PRC_OPENHOLE_RESPONSE:
                        msg = PRC_OpenHoleMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case PRC_SERVER_REQ_CONSEC_MSG:
                        msg = PRC_ServerRequestForConsecutiveMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case PRP_SEND_PORTS_ZSERVER_REQUEST:
                        msg = PRP_ConnectMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case PRP_SEND_PORTS_ZSERVER_RESPONSE:
                        msg = PRP_ConnectMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case PRP_SERVER_REQ_AVAILABLE_PORTS_MSG:
                        msg = PRP_ServerRequestForAvailablePortsMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case PRP_PREALLOCATED_PORTS_REQUEST:
                        msg = PRP_PreallocatedPortsMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case PRP_PREALLOCATED_PORTS_RESPONSE:
                        msg = PRP_PreallocatedPortsMsgFactory.Response.fromBuffer(buffer);
                        break;                        
                    case HP_REGISTER_REQUEST:
                        msg = HpRegisterMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case HP_REGISTER_RESPONSE:
                        msg = HpRegisterMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case HP_UNREGISTER_REQUEST:
                        msg = HpUnregisterMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case HP_UNREGISTER_RESPONSE:
                        msg = HpUnregisterMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case RELAY_CLIENT_TO_SERVER:
                        msg = RelayRequestMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case RELAY_SERVER_TO_CLIENT:
                        msg = RelayRequestMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case SHP_INITIATE_SHP:
                        msg = SHP_InitiateSimpleHolePunchingMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case SHP_OPENHOLE_INITIATOR:
                        msg = SHP_OpenHoleMsgFactory.Initiator.fromBuffer(buffer);
                        break;
                    case PARENT_KEEP_ALIVE_REQUEST:
                        msg = ParentKeepAliveMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case PARENT_KEEP_ALIVE_RESPONSE:
                        msg = ParentKeepAliveMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case HP_KEEP_ALIVE_REQUEST:
                        msg = HpKeepAliveMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case HP_KEEP_ALIVE_RESPONSE:
                        msg = HpKeepAliveMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case HP_FEASABILITY_REQUEST:
                        msg = HpConnectMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case HP_FEASABILITY_RESPONSE:
                        msg = HpConnectMsgFactory.Response.fromBuffer(buffer);
                        break;
                    case SHUFFLE_REQUEST:
                        msg = ShuffleMsgFactory.Request.fromBuffer(buffer);
                        break;
                    case SHUFFLE_RESPONSE:
                        msg = ShuffleMsgFactory.Response.fromBuffer(buffer);
                        break;
                        
                        
                    // Unrecognised type
                    default:
                        logger.warn("No msg decoder found for msg from : " + channel.getRemoteAddress()
                                + ". OpCode = " + opCode);
                        throw new MessageDecodingException("No message decoder found");
                }
//                channel.getPipeline().replace("RS2Decoder", "RS2Decoder",
//                        new GameDecoder());
//                checkpoint(DecoderState.READ_LENGTH);
                checkpoint(DecoderState.READ_TYPE);
                return msg;
            default:
                throw new Error("Shouldn't reach here.");
        }
    }
}
