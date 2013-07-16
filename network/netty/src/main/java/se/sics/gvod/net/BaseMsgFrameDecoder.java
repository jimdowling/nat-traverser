/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.common.msgs.ConnectMsgFactory;
import se.sics.gvod.common.msgs.DisconnectMsgFactory;
import se.sics.gvod.common.msgs.MessageDecodingException;
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
import se.sics.gvod.hp.msgs.TConnectionMsgFactory;
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

    public static final byte CONNECT_REQUEST       = 0x01;
    public static final byte CONNECT_RESPONSE      = 0x02;
    public static final byte DISCONNECT_REQUEST    = 0x03;
    public static final byte DISCONNECT_RESPONSE   = 0x04;
    public static final byte SHUFFLE_REQUEST       = 0x07;
    public static final byte SHUFFLE_RESPONSE      = 0x08;
    public static final byte VOD_MESSAGE           = 0x16;
    public static final byte REWRITEABLE_MESSAGE   = 0x17;
    // OTHER MSGS
    public static final byte RELAY_REQUEST         = 0x18;
    public static final byte RELAY_RESPONSE        = 0x19;
    public static final byte RELAY_ONEWAY          = 0x1a;
    //GRADIENT MSGS
    public static final byte TARGET_UTILITY_PROBE_REQUEST  = 0x20;
    public static final byte TARGET_UTILITY_PROBE_RESPONSE = 0x21;
    public static final byte SETS_EXCHANGE_REQUEST         = 0x22;
    public static final byte SETS_EXCHANGE_RESPONSE        = 0x23;
    public static final byte GRADIENT_HEARTBEAT_REQUEST    = 0x24;
    public static final byte GRADIENT_HEARTBEAT_RESPONSE   = 0x25;
    public static final byte LEADER_SELECTION_REQUEST      = 0x26;
    public static final byte LEADER_SELECTION_RESPONSE     = 0x27;
    // STUN MSGS
    public static final byte ECHO_REQUEST                  = 0x28;
    public static final byte ECHO_RESPONSE                 = 0x29;
    public static final byte ECHO_CHANGE_IP_AND_PORT_REQUEST = 0x2a;
    public static final byte ECHO_CHANGE_IP_AND_PORT_RESPONSE = 0x2b;
    public static final byte ECHO_CHANGE_PORT_REQUEST      = 0x2c;
    public static final byte ECHO_CHANGE_PORT_RESPONSE     = 0x2d;
    public static final byte SERVER_HOST_CHANGE_REQUEST    = 0x2e;
    public static final byte SERVER_HOST_CHANGE_RESPONSE   = 0x2f;
    // HOLE PUNCHING MSGS
    public static final byte GO_MSG                        = 0x30;
    public static final byte HP_FINISHED                   = 0x31;
    public static final byte HP_FEASABILITY_REQUEST        = 0x32;
    public static final byte HP_FEASABILITY_RESPONSE       = 0x33;
    public static final byte HOLE_PUNCHING_REQUEST         = 0x34;
    public static final byte HOLE_PUNCHING_RESPONSE        = 0x35;
    public static final byte HOLE_PUNCHING_RESPONSE_ACK    = 0x36;
    public static final byte 
                          INTERLEAVED_PRC_OPENHOLE_REQUEST  = 0x37;
    public static final byte 
                          INTERLEAVED_PRC_OPENHOLE_RESPONSE = 0x38;
    public static final byte 
                        INTERLEAVED_PRC_SERVER_REQ_PRED_MSG = 0x39;
    public static final byte 
       INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_REQUEST = 0x3a;
    public static final byte 
      INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_RESPONSE = 0x3b;
    public static final byte 
            INTERLEAVED_PRP_SERVERS_REQ_AVAILABLE_PORTS_MSG = 0x3c;
    public static final byte PRC_OPENHOLE_REQUEST          = 0x3d;
    public static final byte PRC_OPENHOLE_RESPONSE         = 0x3e;
    public static final byte PRC_SERVER_REQ_CONSEC_MSG     = 0x3f;
    public static final byte PRP_SEND_PORTS_ZSERVER_REQUEST= 0x40;
    public static final byte 
                            PRP_SEND_PORTS_ZSERVER_RESPONSE = 0x41;
    public static final byte 
                        PRP_SERVER_REQ_AVAILABLE_PORTS_MSG  = 0x42;
    public static final byte HP_REGISTER_REQUEST           = 0x43;
    public static final byte HP_REGISTER_RESPONSE          = 0x44;
    public static final byte RELAY_CLIENT_TO_SERVER        = 0x45;
    public static final byte RELAY_SERVER_TO_CLIENT        = 0x46;
    public static final byte SHP_INITIATE_SHP              = 0x47;
    public static final byte SHP_OPENHOLE_REQUEST          = 0x48;
    public static final byte SHP_OPENHOLE_INITIATOR        = 0x49;
    public static final byte HP_UNREGISTER_REQUEST         = 0x4a;
    public static final byte HP_UNREGISTER_RESPONSE        = 0x4b;
    public static final byte PARENT_KEEP_ALIVE_REQUEST     = 0x4c;
    public static final byte PARENT_KEEP_ALIVE_RESPONSE    = 0x4d;
    public static final byte DELETE_CONNECTION             = 0x4e;
    public static final byte PRP_PREALLOCATED_PORTS_REQUEST= 0x4f;
    public static final byte PRP_PREALLOCATED_PORTS_RESPONSE= 0x50;
    public static final byte HP_KEEP_ALIVE_REQUEST         = 0x51;
    public static final byte HP_KEEP_ALIVE_RESPONSE        = 0x52;
    public static final byte REPORT_REQUEST                = 0x53;
    public static final byte REPORT_RESPONSE               = 0x54;
    // TEST MSGS
    public static final byte PING                          = 0x56;
    public static final byte PONG                          = 0x57;
    //
    // NB: RANGE OF +VE BYTES ENDS AT 0x7F
    
    
    
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
            ByteBuf buffer) throws MessageDecodingException {

        // PEERSEARCH MSGS
        switch (opKod) {
            case CONNECT_REQUEST:
                return ConnectMsgFactory.Request.fromBuffer(buffer);
            case CONNECT_RESPONSE:
                return ConnectMsgFactory.Response.fromBuffer(buffer);
            case DISCONNECT_REQUEST:
                return DisconnectMsgFactory.Request.fromBuffer(buffer);
            case DISCONNECT_RESPONSE:
                return DisconnectMsgFactory.Response.fromBuffer(buffer);
            // GRADIENT MSGS
            case SETS_EXCHANGE_REQUEST:
                return SetsExchangeMsgFactory.Request.fromBuffer(buffer);
            case SETS_EXCHANGE_RESPONSE:
                return SetsExchangeMsgFactory.Response.fromBuffer(buffer);
            case TARGET_UTILITY_PROBE_REQUEST:
                return GradientSearchMsgFactory.Request.fromBuffer(buffer);
            case TARGET_UTILITY_PROBE_RESPONSE:
                return GradientSearchMsgFactory.Response.fromBuffer(buffer);
            // STUN MSGS
            case ECHO_REQUEST:
                return EchoMsgFactory.Request.fromBuffer(buffer);
            case ECHO_RESPONSE:
                return EchoMsgFactory.Response.fromBuffer(buffer);
            case ECHO_CHANGE_IP_AND_PORT_REQUEST:
                return EchoChangeIpAndPortMsgFactory.Request.fromBuffer(buffer);
            case ECHO_CHANGE_IP_AND_PORT_RESPONSE:
                return EchoChangeIpAndPortMsgFactory.Response.fromBuffer(buffer);
            case ECHO_CHANGE_PORT_REQUEST:
                return EchoChangePortMsgFactory.Request.fromBuffer(buffer);
            case ECHO_CHANGE_PORT_RESPONSE:
                return EchoChangePortMsgFactory.Response.fromBuffer(buffer);
            case SERVER_HOST_CHANGE_REQUEST:
                return ServerHostChangeMsgFactory.Request.fromBuffer(buffer);
            case SERVER_HOST_CHANGE_RESPONSE:
                return ServerHostChangeMsgFactory.Response.fromBuffer(buffer);
            case REPORT_REQUEST:
                return ReportMsgFactory.Request.fromBuffer(buffer);
            case REPORT_RESPONSE:
                return ReportMsgFactory.Response.fromBuffer(buffer);
            // HOLE PUNCHING MSGS
            case GO_MSG:
                return GoMsgFactory.Request.fromBuffer(buffer);
            case DELETE_CONNECTION:
                return DeleteConnectionMsgFactory.fromBuffer(buffer);
            case HOLE_PUNCHING_REQUEST:
                return HolePunchingMsgFactory.Request.fromBuffer(buffer);
            case HOLE_PUNCHING_RESPONSE:
                return HolePunchingMsgFactory.Response.fromBuffer(buffer);
            case HOLE_PUNCHING_RESPONSE_ACK:
                return HolePunchingMsgFactory.ResponseAck.fromBuffer(buffer);
            case HP_FINISHED:
                return HpFinishedMsgFactory.Request.fromBuffer(buffer);
            case INTERLEAVED_PRC_OPENHOLE_REQUEST:
                return Interleaved_PRC_OpenHoleMsgFactory.Request.fromBuffer(buffer);
            case INTERLEAVED_PRC_OPENHOLE_RESPONSE:
                return Interleaved_PRC_OpenHoleMsgFactory.Response.fromBuffer(buffer);
            case INTERLEAVED_PRC_SERVER_REQ_PRED_MSG:
                return Interleaved_PRC_ServersRequestForPredictionMsgFactory.Request.fromBuffer(buffer);
            case INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_REQUEST:
                return Interleaved_PRP_ConnectMsgFactory.Request.fromBuffer(buffer);
            case INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_RESPONSE:
                return Interleaved_PRP_ConnectMsgFactory.Response.fromBuffer(buffer);
            case INTERLEAVED_PRP_SERVERS_REQ_AVAILABLE_PORTS_MSG:
                return Interleaved_PRP_ServerRequestForAvailablePortsMsgFactory.Request.fromBuffer(buffer);
            case PRC_OPENHOLE_REQUEST:
                return PRC_OpenHoleMsgFactory.Request.fromBuffer(buffer);
            case PRC_OPENHOLE_RESPONSE:
                return PRC_OpenHoleMsgFactory.Response.fromBuffer(buffer);
            case PRC_SERVER_REQ_CONSEC_MSG:
                return PRC_ServerRequestForConsecutiveMsgFactory.Request.fromBuffer(buffer);
            case PRP_SEND_PORTS_ZSERVER_REQUEST:
                return PRP_ConnectMsgFactory.Request.fromBuffer(buffer);
            case PRP_SEND_PORTS_ZSERVER_RESPONSE:
                return PRP_ConnectMsgFactory.Response.fromBuffer(buffer);
            case PRP_SERVER_REQ_AVAILABLE_PORTS_MSG:
                return PRP_ServerRequestForAvailablePortsMsgFactory.Request.fromBuffer(buffer);
            case PRP_PREALLOCATED_PORTS_REQUEST:
                return PRP_PreallocatedPortsMsgFactory.Request.fromBuffer(buffer);
            case PRP_PREALLOCATED_PORTS_RESPONSE:
                return PRP_PreallocatedPortsMsgFactory.Response.fromBuffer(buffer);
            case HP_REGISTER_REQUEST:
                return HpRegisterMsgFactory.Request.fromBuffer(buffer);
            case HP_REGISTER_RESPONSE:
                return HpRegisterMsgFactory.Response.fromBuffer(buffer);
            case HP_UNREGISTER_REQUEST:
                return HpUnregisterMsgFactory.Request.fromBuffer(buffer);
            case HP_UNREGISTER_RESPONSE:
                return HpUnregisterMsgFactory.Response.fromBuffer(buffer);
            case RELAY_CLIENT_TO_SERVER:
                return RelayRequestMsgFactory.Request.fromBuffer(buffer);
            case RELAY_SERVER_TO_CLIENT:
                return RelayRequestMsgFactory.Response.fromBuffer(buffer);
            case SHP_INITIATE_SHP:
                return SHP_InitiateSimpleHolePunchingMsgFactory.Request.fromBuffer(buffer);
            case SHP_OPENHOLE_INITIATOR:
                return SHP_OpenHoleMsgFactory.Initiator.fromBuffer(buffer);
            case PARENT_KEEP_ALIVE_REQUEST:
                return ParentKeepAliveMsgFactory.Request.fromBuffer(buffer);
            case PARENT_KEEP_ALIVE_RESPONSE:
                return ParentKeepAliveMsgFactory.Response.fromBuffer(buffer);
            case HP_KEEP_ALIVE_REQUEST:
                return HpKeepAliveMsgFactory.Request.fromBuffer(buffer);
            case HP_KEEP_ALIVE_RESPONSE:
                return HpKeepAliveMsgFactory.Response.fromBuffer(buffer);
            case HP_FEASABILITY_REQUEST:
                return HpConnectMsgFactory.Request.fromBuffer(buffer);
            case HP_FEASABILITY_RESPONSE:
                return HpConnectMsgFactory.Response.fromBuffer(buffer);
            case SHUFFLE_REQUEST:
                return ShuffleMsgFactory.Request.fromBuffer(buffer);
            case SHUFFLE_RESPONSE:
                return ShuffleMsgFactory.Response.fromBuffer(buffer);
                // TEST MSGS
            case PING:
                return TConnectionMsgFactory.Ping.fromBuffer(buffer);
            case PONG:
                return TConnectionMsgFactory.Pong.fromBuffer(buffer);
            default:
                break;
        }

        return null;
    }
}
