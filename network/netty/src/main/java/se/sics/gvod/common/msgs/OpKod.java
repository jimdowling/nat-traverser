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
public class OpKod {

    public static final OpKod CONNECT_REQUEST       = new OpKod(0x01);
    public static final OpKod CONNECT_RESPONSE      = new OpKod(0x02);
    public static final OpKod DISCONNECT_REQUEST    = new OpKod(0x03);
    public static final OpKod DISCONNECT_RESPONSE   = new OpKod(0x04);
    public static final OpKod DATAOFFER             = new OpKod(0x05);
    public static final OpKod LEAVE                 = new OpKod(0x06);
    public static final OpKod SHUFFLE_REQUEST       = new OpKod(0x07);
    public static final OpKod SHUFFLE_RESPONSE      = new OpKod(0x08);
    public static final OpKod REPORT_REQUEST        = new OpKod(0x09);
    public static final OpKod REPORT_RESPONSE       = new OpKod(0x0a);
    public static final OpKod REFERENCES_REQUEST    = new OpKod(0x0b);
    public static final OpKod REFERENCES_RESPONSE   = new OpKod(0x0c);
    public static final OpKod UPLOADING_RATE_REQUEST= new OpKod(0x0d);
    public static final OpKod UPLOADING_RATE_RESPONSE=new OpKod(0x0e);
    public static final OpKod D_REQUEST             = new OpKod(0x0f);
    public static final OpKod D_RESPONSE            = new OpKod(0x10);
    public static final OpKod PIECE_NOT_AVAILABLE   = new OpKod(0x11);
    public static final OpKod SATURATED             = new OpKod(0x12);
    public static final OpKod ACK                   = new OpKod(0x13);
    public static final OpKod HASH_REQUEST          = new OpKod(0x14);
    public static final OpKod HASH_RESPONSE         = new OpKod(0x15);
    public static final OpKod VOD_MESSAGE           = new OpKod(0x16);
    public static final OpKod REWRITEABLE_MESSAGE   = new OpKod(0x17);
    // OTHER MSGS
    public static final OpKod RELAY_REQUEST         = new OpKod(0x18);
    public static final OpKod RELAY_RESPONSE        = new OpKod(0x19);
    public static final OpKod RELAY_ONEWAY          = new OpKod(0x1a);
    public static final OpKod PONG                  = new OpKod(0x1b);
    //GRADIENT MSGS
    public static final OpKod TARGET_UTILITY_PROBE_REQUEST  = new OpKod(0x20);
    public static final OpKod TARGET_UTILITY_PROBE_RESPONSE = new OpKod(0x21);
    public static final OpKod SETS_EXCHANGE_REQUEST         = new OpKod(0x22);
    public static final OpKod SETS_EXCHANGE_RESPONSE        = new OpKod(0x23);
    public static final OpKod GRADIENT_HEARTBEAT_REQUEST    = new OpKod(0x24);
    public static final OpKod GRADIENT_HEARTBEAT_RESPONSE   = new OpKod(0x25);
    public static final OpKod LEADER_SELECTION_REQUEST      = new OpKod(0x26);
    public static final OpKod LEADER_SELECTION_RESPONSE     = new OpKod(0x27);
    // STUN MSGS
    public static final OpKod ECHO_REQUEST                  = new OpKod(0x28);
    public static final OpKod ECHO_RESPONSE                 = new OpKod(0x29);
    public static final OpKod ECHO_CHANGE_IP_AND_PORT_REQUEST = new OpKod(0x2a);
    public static final OpKod ECHO_CHANGE_IP_AND_PORT_RESPONSE = new OpKod(0x2b);
    public static final OpKod ECHO_CHANGE_PORT_REQUEST      = new OpKod(0x2c);
    public static final OpKod ECHO_CHANGE_PORT_RESPONSE     = new OpKod(0x2d);
    public static final OpKod SERVER_HOST_CHANGE_REQUEST    = new OpKod(0x2e);
    public static final OpKod SERVER_HOST_CHANGE_RESPONSE   = new OpKod(0x2f);
    // HOLE PUNCHING MSGS
    public static final OpKod GO_MSG                        = new OpKod(0x30);
    public static final OpKod HP_FINISHED                   = new OpKod(0x31);
    public static final OpKod HP_FEASABILITY_REQUEST        = new OpKod(0x32);
    public static final OpKod HP_FEASABILITY_RESPONSE       = new OpKod(0x33);
    public static final OpKod HOLE_PUNCHING_REQUEST         = new OpKod(0x34);
    public static final OpKod HOLE_PUNCHING_RESPONSE        = new OpKod(0x35);
    public static final OpKod HOLE_PUNCHING_RESPONSE_ACK    = new OpKod(0x36);
    public static final OpKod 
                          INTERLEAVED_PRC_OPENHOLE_REQUEST  = new OpKod(0x37);
    public static final OpKod 
                          INTERLEAVED_PRC_OPENHOLE_RESPONSE = new OpKod(0x38);
    public static final OpKod 
                        INTERLEAVED_PRC_SERVER_REQ_PRED_MSG = new OpKod(0x39);
    public static final OpKod 
       INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_REQUEST = new OpKod(0x3a);
    public static final OpKod 
      INTERLEAVED_PRP_SEND_AVAILABLE_PORTS_ZSERVER_RESPONSE = new OpKod(0x3b);
    public static final OpKod 
            INTERLEAVED_PRP_SERVERS_REQ_AVAILABLE_PORTS_MSG = new OpKod(0x3c);
    public static final OpKod PRC_OPENHOLE_REQUEST          = new OpKod(0x3d);
    public static final OpKod PRC_OPENHOLE_RESPONSE         = new OpKod(0x3e);
    public static final OpKod PRC_SERVER_REQ_CONSEC_MSG     = new OpKod(0x3f);
    public static final OpKod PRP_SEND_PORTS_ZSERVER_REQUEST= new OpKod(0x40);
    public static final OpKod 
                            PRP_SEND_PORTS_ZSERVER_RESPONSE = new OpKod(0x41);
    public static final OpKod 
                        PRP_SERVER_REQ_AVAILABLE_PORTS_MSG  = new OpKod(0x42);
    public static final OpKod HP_REGISTER_REQUEST           = new OpKod(0x43);
    public static final OpKod HP_REGISTER_RESPONSE          = new OpKod(0x44);
    public static final OpKod RELAY_CLIENT_TO_SERVER        = new OpKod(0x45);
    public static final OpKod RELAY_SERVER_TO_CLIENT        = new OpKod(0x46);
    public static final OpKod SHP_INITIATE_SHP              = new OpKod(0x47);
    public static final OpKod SHP_OPENHOLE_REQUEST          = new OpKod(0x48);
    public static final OpKod SHP_OPENHOLE_INITIATOR        = new OpKod(0x49);
    public static final OpKod HP_UNREGISTER_REQUEST         = new OpKod(0x4a);
    public static final OpKod HP_UNREGISTER_RESPONSE        = new OpKod(0x4b);
    public static final OpKod PARENT_KEEP_ALIVE_REQUEST     = new OpKod(0x4c);
    public static final OpKod PARENT_KEEP_ALIVE_RESPONSE    = new OpKod(0x4d);
    public static final OpKod DELETE_CONNECTION             = new OpKod(0x4e);
    public static final OpKod PRP_PREALLOCATED_PORTS_REQUEST= new OpKod(0x4f);
    public static final OpKod PRP_PREALLOCATED_PORTS_RESPONSE= new OpKod(0x50);
    public static final OpKod HP_KEEP_ALIVE_REQUEST         = new OpKod(0x51);
    public static final OpKod HP_KEEP_ALIVE_RESPONSE        = new OpKod(0x52);
    // PEERSEARCH MESSAGES
    public static final OpKod SEARCH_REQUEST                = new OpKod(0x60);
    public static final OpKod SEARCH_RESPONSE               = new OpKod(0x62);
    //
    // NB: RANGE OF +VE BYTES ENDS AT 0x7F
    //

    // Byte for the msg header
    private final int b;

    // Used for O(1) lookup of msgs when decoding a msg header
    protected static Map<Byte,OpKod> cache = new HashMap<Byte,OpKod>();

    // Not public
    protected OpKod(int b) {
        this.b = b;
        cache.put(getByte(), this);
    }

    // useful to write to a ChannelBuffer, when encoding
    public final byte getByte() {
        return (byte) this.b;
    }

    // useful to determine which enum value matches byte, when decoding
    public static OpKod fromByte(Byte b) {
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
