/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jdowling
 */
public enum OpCode {

    // VOD MSGS
    CONNECT_REQUEST((byte) 0x01),
    CONNECT_RESPONSE((byte) 0x02),
    DISCONNECT_REQUEST((byte) 0x03),
    DISCONNECT_RESPONSE((byte) 0x04),
    DATAOFFER((byte) 0x05),
    LEAVE((byte) 0x06),
    REFERENCES_REQUEST((byte) 0x13),
    REFERENCES_RESPONSE((byte) 0x14),
    UPLOADING_RATE_REQUEST((byte) 0x17),
    UPLOADING_RATE_RESPONSE((byte) 0x18),
    D_REQUEST((byte) 0x19),
    D_RESPONSE((byte) 0x20),
    PIECE_NOT_AVAILABLE((byte) 0x21),
    SATURATED((byte) 0x22),
    ACK((byte) 0x23),
    HASH_REQUEST((byte) 0x24),
    HASH_RESPONSE((byte) 0x25),
    GVOD_MESSAGE((byte) 0x26),
    REWRITEABLE_MESSAGE((byte) 0x27),
    // BOOTSTRAP MSGS
    BOOTSTRAP_REQUEST((byte) 0x28),
    BOOTSTRAP_RESPONSE((byte) 0x29),
    BOOTSTRAP_HEARTBEAT((byte) 0x30),
    BOOTSTRAP_ADD_OVERLAY_REQUEST((byte) 0x31),
    BOOTSTRAP_ADD_OVERLAY_RESPONSE((byte) 0x32),
    DOWNLOAD_COMPLETED((byte) 0x33),
    MONITOR_MSG((byte) 0x34),
    // LIVE STREAMING MSGS
    INTER_AS_GOSSIP_REQUEST((byte) 0x35),
    INTER_AS_GOSSIP_RESPONSE((byte) 0x36),
    //INTRA_AS_GOSSIP_REQUEST((byte) 0x37),
    //INTRA_AS_GOSSIP_RESPONSE((byte) 0x38),
    // VIDEO MESSAGES

    VIDEO_CONNECTION_REQUEST((byte) 0x39),
    VIDEO_CONNECTION_RESPONSE((byte) 0x3a),
    VIDEO_CONNECTION_DISCONNECT((byte) 0x3b),
    VIDEO_PIECES_ADVERTISEMENT((byte) 0x3c),
    VIDEO_PIECES_REQUEST((byte) 0x3d),
    VIDEO_PIECES_PIECEDATA((byte) 0x3e),
    VIDEO_PIECES_PIECES((byte) 0x3f),
    //GRADIENT MSGS
    TARGET_UTILITY_PROBE_REQUEST((byte) 0x40),
    TARGET_UTILITY_PROBE_RESPONSE((byte) 0x41),
    SETS_EXCHANGE_REQUEST((byte) 0x42),
    SETS_EXCHANGE_RESPONSE((byte) 0x43),
    GRADIENT_HEARTBEAT_REQUEST((byte) 0x44),
    GRADIENT_HEARTBEAT_RESPONSE((byte) 0x45),
    LEADER_SELECTION_REQUEST((byte) 0x46),
    LEADER_SELECTION_RESPONSE((byte) 0x47),
    // STUN MSGS
    ECHO_REQUEST((byte) 0x48),
    ECHO_RESPONSE((byte) 0x49),
    ECHO_CHANGE_IP_AND_PORT_REQUEST((byte) 0x4a),
    ECHO_CHANGE_IP_AND_PORT_RESPONSE((byte) 0x4b),
    ECHO_CHANGE_PORT_REQUEST((byte) 0x4c),
    ECHO_CHANGE_PORT_RESPONSE((byte) 0x4d),
    SERVER_HOST_CHANGE_REQUEST((byte) 0x4e),
    SERVER_HOST_CHANGE_RESPONSE((byte) 0x4f),
    // HOLE PUNCHING MSGS
    GO_MSG((byte) 0x50),
    HP_FINISHED((byte) 0x51),
    HP_FEASABILITY_REQUEST((byte) 0x52),
    HP_FEASABILITY_RESPONSE((byte) 0x53),
    HOLE_PUNCHING_REQUEST((byte) 0x54),
    HOLE_PUNCHING_RESPONSE((byte) 0x55),
    HOLE_PUNCHING_RESPONSE_ACK((byte) 0x56),
    INTERLEAVED_PRC_OPENHOLE_REQUEST((byte) 0x57),
    INTERLEAVED_PRC_OPENHOLE_RESPONSE((byte) 0x58),
    INTERLEAVED_PRC_SERVER_REQ_PRED_MSG((byte) 0x59),
    INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_REQUEST((byte) 0x5a),
    INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_RESPONSE((byte) 0x60),
    INTERLEAVED_PRP_SERVERS_REQ_AVAILABLE_PORTS_MSG((byte) 0x61),
    PRC_OPENHOLE_REQUEST((byte) 0x62),
    PRC_OPENHOLE_RESPONSE((byte) 0x63),
    PRC_SERVER_REQ_CONSEC_MSG((byte) 0x64),
    PRP_SEND_PORTS_ZSERVER_REQUEST((byte) 0x65),
    PRP_SEND_PORTS_ZSERVER_RESPONSE((byte) 0x66),
    PRP_SERVER_REQ_AVAILABLE_PORTS_MSG((byte) 0x67),
    HP_REGISTER_REQUEST((byte) 0x68),
    HP_REGISTER_RESPONSE((byte) 0x69),
    RELAY_CLIENT_TO_SERVER((byte) 0x70),
    RELAY_SERVER_TO_CLIENT((byte) 0x71),
    SHP_INITIATE_SHP((byte) 0x72),
    SHP_OPENHOLE_REQUEST((byte) 0x73),
    SHP_OPENHOLE_INITIATOR((byte) 0x74),
    HP_UNREGISTER_REQUEST((byte) 0x75),
    HP_UNREGISTER_RESPONSE((byte) 0x76),
    PARENT_KEEP_ALIVE_REQUEST((byte) 0x77),
    PARENT_KEEP_ALIVE_RESPONSE((byte) 0x78),
    DELETE_CONNECTION((byte) 0x79),
    PRP_PREALLOCATED_PORTS_REQUEST((byte) 0x7a),
    PRP_PREALLOCATED_PORTS_RESPONSE((byte) 0x7b),
    HP_KEEP_ALIVE_REQUEST((byte) 0x7c),
    HP_KEEP_ALIVE_RESPONSE((byte) 0x7d),
    //
    // RANGE OF +VE BYTES ENDS AT 0x7F
    //
    // OTHER MSGS
    RELAY_REQUEST((byte) 0x81),
    RELAY_RESPONSE((byte) 0x82),
    RELAY_ONEWAY((byte) 0x83),
    PONG((byte) 0x084),
    // STUN REPORTING
    REPORT_REQUEST((byte) 0x09),
    REPORT_RESPONSE((byte) 0x10),
    // CROUPIER MSGS
    SHUFFLE_REQUEST((byte) 0x07),
    SHUFFLE_RESPONSE((byte) 0x08),;
    private int b;

    OpCode(int b) {
        this.b = b;
    }
    private static Map<Byte, OpCode> cache = null;

    // useful to write to a ChannelBuffer, when encoding
    public byte getByte() {
        return (byte) this.b;
    }

    // useful to determine which enum value matches byte, when decoding
    public static OpCode fromByte(Byte b) {
        if (cache == null) {
            cache = new HashMap<Byte,OpCode>();
            for (OpCode opcode : values()) {
                cache.put(opcode.getByte(), opcode);
            }
        }
        if (cache.containsKey(b) == false) {
            throw new IllegalArgumentException("No Opcode for byte: " + b);
        }
        return cache.get(b);
    }

    public static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }

    public int getInt() {
        return (int) b & 0xFF;
    }
}
