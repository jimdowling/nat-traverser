/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

/**
 *
 * @author jdowling
 */
public class OpKod {

    public static final byte CONNECT_REQUEST       = 0x01;
    public static final byte CONNECT_RESPONSE      = 0x02;
    public static final byte DISCONNECT_REQUEST    = 0x03;
    public static final byte DISCONNECT_RESPONSE   = 0x04;
    public static final byte DATAOFFER             = 0x05;
    public static final byte LEAVE                 = 0x06;
    public static final byte SHUFFLE_REQUEST       = 0x07;
    public static final byte SHUFFLE_RESPONSE      = 0x08;
    public static final byte REPORT_REQUEST        = 0x09;
    public static final byte REPORT_RESPONSE       = 0x0a;
    public static final byte REFERENCES_REQUEST    = 0x0b;
    public static final byte REFERENCES_RESPONSE   = 0x0c;
    public static final byte UPLOADING_RATE_REQUEST= 0x0d;
    public static final byte UPLOADING_RATE_RESPONSE=0x0e;
    public static final byte D_REQUEST             = 0x0f;
    public static final byte D_RESPONSE            = 0x10;
    public static final byte PIECE_NOT_AVAILABLE   = 0x11;
    public static final byte SATURATED             = 0x12;
    public static final byte ACK                   = 0x13;
    public static final byte HASH_REQUEST          = 0x14;
    public static final byte HASH_RESPONSE         = 0x15;
    public static final byte VOD_MESSAGE           = 0x16;
    public static final byte REWRITEABLE_MESSAGE   = 0x17;
    // OTHER MSGS
    public static final byte RELAY_REQUEST         = 0x18;
    public static final byte RELAY_RESPONSE        = 0x19;
    public static final byte RELAY_ONEWAY          = 0x1a;
    public static final byte PONG                  = 0x1b;
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
    // PEERSEARCH MESSAGES
    public static final byte SEARCH_REQUEST                = 0x60;
    public static final byte SEARCH_RESPONSE               = 0x62;
    //
    // NB: RANGE OF +VE BYTES ENDS AT 0x7F
    //

    // Byte for the msg header
    private final int b;


    // Not public
    protected OpKod(int b) {
        this.b = b;
    }

    // useful to write to a ChannelBuffer, when encoding
    public final byte getByte() {
        return (byte) this.b;
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
