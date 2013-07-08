/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.util;

import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.gvod.address.Address;
import se.sics.gvod.common.DescriptorBuffer;
import se.sics.gvod.common.Utility;
import se.sics.gvod.common.UtilityLS;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.NoTimeoutId;
import se.sics.gvod.timer.TimeoutId;

/**
 *
 * @author jdowling
 */
public class UserTypesEncoderFactory {

    public static final int CHUNK_SZ = 2;
    public static final int PORT_SZ = 2;
    public static final int VOD_ADDRESS_LEN_NO_PARENTS = 4 + 2 + 4 + 4 + 2;
    public static final int ADDRESS_LEN = 10;
    public static final int UTILITY_LEN = 2 + 8 + 2;
    public static final int UUID_LEN = 16;
    public static final int GVOD_NODE_DESCRIPTOR_LEN = VOD_ADDRESS_LEN_NO_PARENTS + 2 + 2 + UTILITY_LEN;
    public static final String STRING_CHARSET = "UTF-8";
    public static final int NAT_LEN = 1 /*
             * type
             */
            + 1 /*
             * mp
             */
            + 1 /*
             * ap
             */
            + 1 /*
             * fp
             */
            + 1 /*
             * aap
             */
            + 1 /*
             * delta
             */
            + 8 /*
             * ruleExpirationTime
             */
            + ADDRESS_LEN /*
             * upnp
             */;

    ;

    public static void writeUnsignedintAsOneByte(ByteBuf buffer, int value) throws MessageEncodingException {
        if ((value >= Math.pow(2, 8)) || (value < 0)) {
            throw new MessageEncodingException("Integer value < 0 or " + value + " is larger than 2^15");
        }
        buffer.writeByte((byte) (value & 0xFF));
    }

    public static void writeUnsignedintAsTwoBytes(ByteBuf buffer, int value) throws MessageEncodingException {
        byte[] result = new byte[2];
        if ((value >= Math.pow(2, 16)) || (value < 0)) {
            throw new MessageEncodingException("Integer value < 0 or " + value + " is larger than 2^31");
        }
        result[0] = (byte) ((value >>> 8) & 0xFF);
        result[1] = (byte) (value & 0xFF);
        buffer.writeBytes(result);
    }

    
    public static void writeTimeoutId(ByteBuf buffer, TimeoutId id)
            throws MessageEncodingException {
        if (id instanceof NoTimeoutId) {
            buffer.writeInt(-1);
        } else {
            buffer.writeInt(id.getId());
        }
    }
    
    
    public static void writeVodAddress(ByteBuf buffer, VodAddress addr)
            throws MessageEncodingException {
        writeAddress(buffer, addr.getPeerAddress());
        buffer.writeInt(addr.getOverlayId());
        writeUnsignedintAsOneByte(buffer, addr.getNatPolicy());
        writeListAddresses(buffer, addr.getParents());
    }

    public static void writeListVodAddresses(ByteBuf buffer,
            List<VodAddress> addresses)
            throws MessageEncodingException {
        if (addresses == null) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
            return;
        }
        UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, addresses.size());
        for (VodAddress addr : addresses) {
            writeVodAddress(buffer, addr);
        }
    }

    public static void writeListAddresses(ByteBuf buffer,
            Set<Address> addresses)
            throws MessageEncodingException {
        if (addresses == null || addresses.isEmpty()) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
            return;
        }
        UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, addresses.size());
        for (Address addr : addresses) {
            writeAddress(buffer, addr);
        }
    }

    public static void writeListRtts(ByteBuf buffer,
            List<Integer> rtts)
            throws MessageEncodingException {
        if (rtts == null) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
            return;
        }
        UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, rtts.size());
        for (Integer rtt : rtts) {
            writeUnsignedintAsTwoBytes(buffer, rtt);
        }
    }

    public static void writeListVodNodeDescriptors(ByteBuf buffer,
            List<VodDescriptor> nodeDescriptors)
            throws MessageEncodingException {
        if (nodeDescriptors == null) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
            return;
        }
        writeUnsignedintAsTwoBytes(buffer, nodeDescriptors.size());
        for (VodDescriptor node : nodeDescriptors) {
            writeVodNodeDescriptor(buffer, node);
        }
    }

    public static void writeVodNodeDescriptor(ByteBuf buffer,
            VodDescriptor nodeDescriptor)
            throws MessageEncodingException {
        UserTypesEncoderFactory.writeVodAddress(buffer, nodeDescriptor.getVodAddress());
        UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, nodeDescriptor.getAge());
        UserTypesEncoderFactory.writeUtility(buffer, nodeDescriptor.getUtility());
        UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, nodeDescriptor.getMtu());
    }

    public static void writeCollectionInts(ByteBuf buffer,
            Collection<Integer> collectionInts) throws MessageEncodingException {
        if (collectionInts == null) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
        } else {
            if (collectionInts.size() > 65535) {
                throw new IllegalArgumentException("max number of ints is 65535");
            }
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, collectionInts.size());
            for (Integer i : collectionInts) {
                buffer.writeInt(i);
            }
        }
    }

    public static void writeIntegerSet(ByteBuf buffer, Set<Integer> integers) throws MessageEncodingException {
        if (integers == null) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
        } else {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, integers.size());
            for (Integer integer : integers) {
                buffer.writeInt(integer);
            }
        }
    }

    public static void writeLongSet(ByteBuf buffer, Set<Long> longs) throws MessageEncodingException {
        /*
         * The first entry of an encoded Collection is always the length stored
         * as two bytes
         */
        if (longs == null) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
        } else {
            if (longs.size() > 65535) {
                throw new IllegalArgumentException("Max size of a collection is 65535");
            }
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, longs.size());
            for (Long l : longs) {
                buffer.writeLong(l);
            }
        }
    }

    public static void writeMapIntInts(ByteBuf buffer, Map<Integer, Integer> overlayUtilities)
            throws MessageEncodingException {
        if (overlayUtilities == null) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
        } else {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, overlayUtilities.size());
            for (Entry<Integer, Integer> i : overlayUtilities.entrySet()) {
                Integer key = i.getKey();
                Integer val = i.getValue();
                if (key == null) {
                    buffer.writeInt(-1);
                } else {
                    buffer.writeInt(key);
                }
                if (val == null) {
                    buffer.writeInt(-1);
                } else {
                    buffer.writeInt(val);
                }
            }
        }
    }

    public static void writeSetUnsignedTwoByteInts(ByteBuf buffer,
            Set<Integer> ports)
            throws MessageEncodingException {
        if (ports == null) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
        } else {
            if (ports.size() > 65535) {
                throw new IllegalArgumentException("max number of ints is 65535");
            }
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, ports.size());
            for (Integer entry : ports) {
                writeUnsignedintAsTwoBytes(buffer, entry);
            }
        }
    }

    public static void writeAddress(ByteBuf buffer, Address addr)
            throws MessageEncodingException {
        byte[] bytes;
        int port = 0;
        int id = 0;
        if (addr == null) {
            try {
                // This shouldn't block on reverse DNS lookup
                bytes = InetAddress.getByName("127.0.0.1").getAddress();
            } catch (UnknownHostException ex) {
                Logger.getLogger(UserTypesEncoderFactory.class.getName()).log(Level.SEVERE, null, ex);
                throw new MessageEncodingException(("Could not create localhost IP addr to marshall null Address"));
            }
        } else {
            // TODO - this cannot be a 
            bytes = addr.getIp().getAddress();
            port = addr.getPort();
            id = addr.getId();
        }
        // sometimes you get Ipv6 addresses. Shouldn't happen, unless some uses
        // InetAddress.getLocalHost() to generate an IP address.
        assert (bytes.length == 4);
        buffer.writeBytes(bytes);
        writeUnsignedintAsTwoBytes(buffer, port);
        buffer.writeInt(id);
    }

    public static void writeUtility(ByteBuf buffer, Utility utility)
            throws MessageEncodingException {
        // 2 bytes = chunk, offset;  8 bytes = piece
        // chunk can be -10, for a seed.
        UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, utility.getImplType().ordinal());
        if (utility.getImplType() == Utility.Impl.VodUtility) {
            UtilityVod vod = (UtilityVod) utility;
            UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, vod.getAvailableBandwidth());
            buffer.writeShort(vod.getChunk());
            buffer.writeLong(vod.getPiece());
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, vod.getOffset());

        } else if (utility instanceof UtilityLS) {
        } else {
        }
    }

    public static void writeArrayBytes(ByteBuf buffer, byte[] bytes)
            throws MessageEncodingException {
        if (bytes == null) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
        } else {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, bytes.length);
            buffer.writeBytes(bytes);
        }
    }

    /**
     *
     * @param buffer
     * @param availablePieces [numPiece][numSubpieces], where numSubpieces is
     * constant.
     * @throws MessageEncodingException
     */
    public static void writeArrayArrayBytes(ByteBuf buffer, byte[][] availablePieces)
            throws MessageEncodingException {
        if (buffer == null) {
            throw new MessageEncodingException("buffer or bytes was null.");
        }
        if (availablePieces == null) {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, 0);
        } else {
            UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, availablePieces.length);
//        UserTypesEncoderFactory.writeUnsignedintAsTwoBytes(buffer, availablePieces.length); BitField.NUM_SUBPIECES_PER_PIECE
            for (int i = 0; i < availablePieces.length; i++) {
                buffer.writeBytes(availablePieces[i]);
            }
        }
    }

    public static void writeUUID(ByteBuf buffer, UUID uuid)
            throws MessageEncodingException {
        long lsb = 0;
        long msb = 0;
        if (uuid != null) {
            lsb = uuid.getLeastSignificantBits();
            msb = uuid.getMostSignificantBits();
        }
        buffer.writeLong(lsb);
        buffer.writeLong(msb);
    }

    public static void writeDescriptorBuffer(ByteBuf buffer,
            DescriptorBuffer descBuf)
            throws MessageEncodingException {
        writeVodAddress(buffer, descBuf.getFrom());
        writeListVodNodeDescriptors(buffer, descBuf.getPublicDescriptors());
        writeListVodNodeDescriptors(buffer, descBuf.getPrivateDescriptors());
    }

    public static void writeNat(ByteBuf buffer,
            Nat nat)
            throws MessageEncodingException {
        int type = nat.getType().ordinal();
        UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, type);
        int mPolicy = nat.getMappingPolicy().ordinal();
        UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, mPolicy);
        int aPolicy = nat.getAllocationPolicy().ordinal();
        UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, aPolicy);
        int fPolicy = nat.getFilteringPolicy().ordinal();
        UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, fPolicy);
        int aaPolicy = nat.getAlternativePortAllocationPolicy().ordinal();
        UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, aaPolicy);
        UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, nat.getDelta());
        buffer.writeLong(nat.getBindingTimeout());
        UserTypesEncoderFactory.writeAddress(buffer, nat.getPublicUPNPAddress());
    }

    public static void writeStringLength256(ByteBuf buffer, String str)
            throws MessageEncodingException {
        if (str == null) {
            writeUnsignedintAsOneByte(buffer, 0);
        } else {
            if (str.length() > 255) {
                throw new MessageEncodingException("String length > 255 : " + str);
            }
            byte[] strBytes;
            try {
                strBytes = str.getBytes(STRING_CHARSET);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(UserTypesEncoderFactory.class.getName()).log(Level.SEVERE, null, ex);
                throw new MessageEncodingException("Unsupported chartset when encoding string: "
                        + STRING_CHARSET);
            }
            int len = strBytes.length;
            writeUnsignedintAsOneByte(buffer, len);
            buffer.writeBytes(strBytes);
        }
    }

    public static void writeStringLength65536(ByteBuf buffer, String str)
            throws MessageEncodingException {
        byte[] strBytes;
        if (str == null) {
            writeUnsignedintAsTwoBytes(buffer, 0);
        } else {
            try {
                strBytes = str.getBytes(STRING_CHARSET);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(UserTypesEncoderFactory.class.getName()).log(Level.SEVERE, null, ex);
                throw new MessageEncodingException("Unsupported chartset when encoding string: "
                        + STRING_CHARSET);
            }
            int len = strBytes.length;
            if (len > VodConfig.DEFAULT_MTU - 42) {
                throw new MessageEncodingException("Tried to write more bytes to "
                        + "writeString65536 than the MTU size. Attempted to write #bytes: " + len);
            }
            writeUnsignedintAsTwoBytes(buffer, len);
            buffer.writeBytes(strBytes);
        }
    }

    public static void writeBytesLength65536(ByteBuf buffer, byte[] bytes)
            throws MessageEncodingException {
        if (bytes == null) {
            writeUnsignedintAsTwoBytes(buffer, 0);
        } else {
            if (bytes.length > 65535) {
                throw new MessageEncodingException("Tried to write more bytes to "
                        + "writeBytesLen65536 than allowed. Attempted: " + bytes.length);
            }
            int len = bytes.length;
            writeUnsignedintAsTwoBytes(buffer, len);
            buffer.writeBytes(bytes);
        }
    }

    public static void writeBoolean(ByteBuf buffer, boolean b)
            throws MessageEncodingException {
        UserTypesEncoderFactory.writeUnsignedintAsOneByte(buffer, b == true ? 1 : 0);
    }

    public static int getCollectionIntsLength(Collection<Integer> listInts) {
        if (listInts == null) {
            return 2;
        }
        return 2 + listInts.size() * 4;
    }

    public static int getMapIntIntsLength(Map<Integer, Integer> mapIntInts) {
        if (mapIntInts == null) {
            return 2;
        }
        return 2 + (mapIntInts.size() * 4 * 2);
    }

    public static int getStringLength256(String str) {
        if (str == null) {
            return 1;
        }
        return str.length() + 1;
    }

    public static int getStringLength65356(String str) {
        if (str == null) {
            return 2;
        }
        return str.length() + 2;
    }

    public static int getBytesLength65356(byte[] bytes) {
        if (bytes == null) {
            return 2;
        }
        return bytes.length + 2;
    }

    public static int getArrayArraySize(byte[][] bytes) {
        int sz = 0;
        if (bytes == null) {
            return 2;
        }
        for (int i = 0; i < bytes.length; i++) {
            sz += bytes[i].length;
        }
        return sz;
    }

    public static int getArraySize(byte[] bytes) {
        if (bytes == null) {
            return 2;
        }
        return 2 + bytes.length;
    }

    public static int getListIntegerSize(List<Integer> ints) {
        int sz = 2 /*
                 * len
                 */;
        if (ints == null) {
            return sz;
        }
        sz += (ints.size() * 2);
        return sz;
    }

    public static int getListVodAddressSize(List<VodAddress> addresses) {
        int sz = 2 /*
                 * len
                 */;
        if (addresses == null) {
            return sz;
        }
        sz += (addresses.size() * VOD_ADDRESS_LEN_NO_PARENTS);
        return sz;
    }

    public static int getListAddressSize(Set<Address> addresses) {
        int sz = 2 /*
                 * len
                 */;
        if (addresses == null) {
            return sz;
        }
        sz += (addresses.size() * ADDRESS_LEN);
        return sz;
    }

    public static int getListGVodNodeDescriptorSize(List<VodDescriptor> nodes) {
        if (nodes == null) {
            return 2;
        }

        return 2 /*
                 * len
                 */ + (GVOD_NODE_DESCRIPTOR_LEN * nodes.size());
    }

    public static int getDescriptorBufferSize(DescriptorBuffer buffer) {
        int sz = VOD_ADDRESS_LEN_NO_PARENTS
                + GVOD_NODE_DESCRIPTOR_LEN * (buffer.getPublicDescriptors().size() + buffer.getPrivateDescriptors().size());
        return sz;
    }

    public static int getNatSize() {
        return 1 /*
                 * type
                 */
                + 1 /*
                 * mp
                 */
                + 1 /*
                 * ap
                 */
                + 1 /*
                 * fp
                 */
                + 1 /*
                 * aap
                 */
                + 1 /*
                 * delta
                 */
                + 8 /*
                 * ruleExpirationTime
                 */
                + ADDRESS_LEN /*
                 * upnp
                 */;
    }
}
