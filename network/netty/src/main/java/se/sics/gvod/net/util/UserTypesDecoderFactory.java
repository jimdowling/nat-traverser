/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net.util;

import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.gvod.address.Address;
import se.sics.gvod.common.BitField;
import se.sics.gvod.common.DescriptorBuffer;
import se.sics.gvod.common.Utility;
import se.sics.gvod.common.UtilityVod;
import se.sics.gvod.common.VodDescriptor;
import se.sics.gvod.common.msgs.MessageDecodingException;
import se.sics.gvod.net.Nat;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.timer.NoTimeoutId;
import se.sics.gvod.timer.TimeoutId;
import se.sics.gvod.timer.UUID;

/**
 *
 * @author jdowling
 */
public class UserTypesDecoderFactory {

    public static int readUnsignedIntAsOneByte(ByteBuf buffer) {
        byte value = buffer.readByte();
        return value & 0xFF;
    }

    public static int readUnsignedIntAsTwoBytes(ByteBuf buffer) //            throws MessageDecodingException
    {
        byte[] bytes = new byte[2];
        buffer.readBytes(bytes);
        int temp0 = bytes[0] & 0xFF;
        int temp1 = bytes[1] & 0xFF;
        return ((temp0 << 8) + temp1);
    }

    public static boolean readBoolean(ByteBuf buffer)
            throws MessageDecodingException {
        int i = readUnsignedIntAsOneByte(buffer);
        if (i == 0) {
            return false;
        }
        if (i == 1) { 
            return true;
        }
        throw new MessageDecodingException("the in parameter not equal nor to 1, nor 0");
    }

    public static Address readAddress(ByteBuf buffer) throws MessageDecodingException {
        InetAddress ip;
        ip = readInetAddress(buffer);
        int port = buffer.readUnsignedShort();
        int id = buffer.readInt();

        Address addr = new Address(ip, port, id);
        try {
            if (addr.equals(new Address(InetAddress.getByName("127.0.0.1"), 0, 0))) {
                return null;
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(UserTypesDecoderFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addr;
    }

    public static java.util.UUID readUUID(ByteBuf buffer)
            throws MessageDecodingException {
        long lsb = buffer.readLong();
        long msb = buffer.readLong();
        if (lsb == 0 && msb == 0) {
            return null;
        }
        return new java.util.UUID(msb, lsb);
    }

    public static TimeoutId readTimeoutId(ByteBuf buffer)
            throws MessageDecodingException {
        int id = buffer.readInt();
        if (id == -1) {
            return new NoTimeoutId();
        }
        return new UUID(id);
    }

    public static String readStringLength256(ByteBuf buffer)
            throws MessageDecodingException {
        int len = readIntAsOneByte(buffer);
        if (len == 0) {
            return null;
        } else {
            return readString(buffer, len);
        }
    }

    private static String readString(ByteBuf buffer, int len)
            throws MessageDecodingException {
        byte[] bytes = new byte[len];
        buffer.readBytes(bytes);
        try {
            return new String(bytes, UserTypesEncoderFactory.STRING_CHARSET);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UserTypesDecoderFactory.class.getName()).log(Level.SEVERE, null, ex);
            throw new MessageDecodingException(ex.getMessage());
        }
    }

    
   
    public static String readStringLength65536(ByteBuf buffer)
            throws MessageDecodingException {
        int len = readUnsignedIntAsTwoBytes(buffer);
        if (len == 0) {
            return null;
        } else {
            return readString(buffer, len);
        }
    }

    public static byte[] readBytesLength65536(ByteBuf buffer)
            throws MessageDecodingException {
        int len = readUnsignedIntAsTwoBytes(buffer);
        if (len == 0) {
            return null;
        } else {
    
            byte[] bytes = new byte[len];
            buffer.readBytes(bytes);
            return bytes;
        }
    }

    public static int readIntAsOneByte(ByteBuf buffer)
            throws MessageDecodingException {
        return readUnsignedIntAsOneByte(buffer);
    }

    public static InetAddress readInetAddress(ByteBuf buffer)
            throws MessageDecodingException {
        byte[] ipBytes = new byte[4];
        buffer.readBytes(ipBytes);
        try {
            return InetAddress.getByAddress(ipBytes);
        } catch (UnknownHostException ex) {
            Logger.getLogger(UserTypesDecoderFactory.class.getName()).log(Level.SEVERE, null, ex);
            throw new MessageDecodingException(ex.getMessage());
        }
    }

    public static Utility readUtility(ByteBuf buffer) {
        int type = readUnsignedIntAsOneByte(buffer);
        if (type == Utility.Impl.VodUtility.ordinal()) {
            int availableBandwidth = readUnsignedIntAsOneByte(buffer);
        // Up to max 65,536 chunks
            int chunk = buffer.readShort();
            long piece = buffer.readLong();
            int offset = readUnsignedIntAsTwoBytes(buffer);
            return new UtilityVod(chunk, piece, offset, availableBandwidth);
        } else if (type == Utility.Impl.LsUtility.ordinal()) {
            return null;
        } else {
            return null;
        }
    }


    public static VodAddress readVodAddress(ByteBuf buffer)
            throws MessageDecodingException {
        Address addr = readAddress(buffer);
        int overlayId = buffer.readInt();
        int natPolicy = readUnsignedIntAsOneByte(buffer);
        Set<Address> parents = readListAddresses(buffer);
        return new VodAddress(addr, overlayId, (short) natPolicy, parents);
    }

    public static List<VodAddress> readListVodAddresses(ByteBuf buffer)
            throws MessageDecodingException {
        int len = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        List<VodAddress> addrs = new ArrayList<VodAddress>();
        for (int i = 0; i < len; i++) {
            addrs.add(readVodAddress(buffer));
        }
        return addrs;
    }

    public static Set<Address> readListAddresses(ByteBuf buffer)
            throws MessageDecodingException {
        int len = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        Set<Address> addrs = new HashSet<Address>();
        for (int i = 0; i < len; i++) {
            addrs.add(readAddress(buffer));
        }
        return addrs;
    }

    public static List<Integer> readListRtts(ByteBuf buffer)
            throws MessageDecodingException {
        int len = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        List<Integer> rtts = new ArrayList<Integer>();
        for (int i = 0; i < len; i++) {
            rtts.add(readUnsignedIntAsTwoBytes(buffer));
        }
        return rtts;
    }

    public static List<Integer> readListInts(ByteBuf buffer)
            throws MessageDecodingException {
        int len = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        List<Integer> addrs = new ArrayList<Integer>();
        for (int i = 0; i < len; i++) {
            addrs.add(buffer.readInt());
        }
        return addrs;
    }
    
    public static Set<Integer> readSetInts(ByteBuf buffer)
            throws MessageDecodingException {
        int len = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        Set<Integer> addrs = new HashSet<Integer>();
        for (int i = 0; i < len; i++) {
            addrs.add(buffer.readInt());
        }
        return addrs;
    }
    
    public static Map<Integer,Integer> readMapIntInts(ByteBuf buffer)
            throws MessageDecodingException {
        int len = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        Map<Integer,Integer> mapIntInts = new HashMap<Integer,Integer>();
        for (int i = 0; i < len; i++) {
            Integer key = buffer.readInt();
            Integer val = buffer.readInt();
            if (key != -1 || val != -1) {
                mapIntInts.put(key,val);
            }
        }
        return mapIntInts;
    }    
    
    public static byte[] readArrayBytes(ByteBuf buffer)
            throws MessageDecodingException {
        if (buffer == null) {
            throw new MessageDecodingException("buffer was null.");
        }
        int len = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);

        if (len == 0) {
            return null;
        } else {
            byte[] bytes = new byte[len];
            buffer.readBytes(bytes);
            return bytes;
        }
    }

    public static byte[][] readArrayArrayBytes(ByteBuf buffer)
            throws MessageDecodingException {
        if (buffer == null) {
            throw new MessageDecodingException("buffer was null.");
        }
        int len = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        if (len == 0) {
            return null;
        }

        byte[][] bytes = new byte[len][BitField.NUM_SUBPIECES_PER_PIECE];
        for (int i = 0; i < len; i++) {
            buffer.readBytes(bytes[i]);
        }
        return bytes;
    }

    public static VodDescriptor readVodNodeDescriptor(ByteBuf buffer)
            throws MessageDecodingException {
        VodAddress addr = UserTypesDecoderFactory.readVodAddress(buffer);
        int age = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        Utility utility = UserTypesDecoderFactory.readUtility(buffer);
        int mtu = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        long numberOfIndexEntries = buffer.readLong();
        long numberOfEntries = buffer.readLong();
        int partitionsNumber = buffer.readInt();

        int partitionIdLength = buffer.readInt();
        BitSet set = fromByteArray(buffer.readBytes(partitionIdLength).array());

        return new VodDescriptor(addr, utility, age, mtu, numberOfEntries, partitionsNumber, set);
    }

    private static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    public static List<VodDescriptor> readListVodNodeDescriptors(ByteBuf buffer)
            throws MessageDecodingException {
        int len = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        List<VodDescriptor> addrs = new ArrayList<VodDescriptor>();
        for (int i = 0; i < len; i++) {
            addrs.add(readVodNodeDescriptor(buffer));
        }
        return addrs;
    }

    public static Set<Integer> readSetUnsignedTwoByteInts(ByteBuf buffer)
            throws MessageDecodingException {
        int numEntries = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        Set<Integer> entries = new HashSet<Integer>();
        for (int i = 0; i < numEntries; i++) {
            int entry = readUnsignedIntAsTwoBytes(buffer);
            entries.add(entry);
        }
        return entries;
    }


    public static DescriptorBuffer readDescriptorBuffer(ByteBuf buffer)
            throws MessageDecodingException {
        VodAddress src = readVodAddress(buffer);
        List<VodDescriptor> publicDescs = readListVodNodeDescriptors(buffer);
        List<VodDescriptor> privateDescs = readListVodNodeDescriptors(buffer);
        return new DescriptorBuffer(src, publicDescs, privateDescs);
    }
    public static Nat readNat(ByteBuf buffer)
            throws MessageDecodingException {
//    private final Type type;
//    private final MappingPolicy mappingPolicy;
//    private final AllocationPolicy allocationPolicy;
//    private final FilteringPolicy filteringPolicy;
//    private final AlternativePortAllocationPolicy alternativePortAllocationPolicy;
//    private final int delta;
//    private long ruleExpirationTime;
//    private final Address publicUPNPAddress;
        int t = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
        Nat.Type type = Nat.Type.values()[t];
        int mp = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
        Nat.MappingPolicy mappingPolicy = Nat.MappingPolicy.values()[mp];
        int ap = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
        Nat.AllocationPolicy allocationPolicy = Nat.AllocationPolicy.values()[ap];
        int fp = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
        Nat.FilteringPolicy filteringPolicy = Nat.FilteringPolicy.values()[fp];
        int aap = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
        Nat.AlternativePortAllocationPolicy altAllocationPolicy =
                Nat.AlternativePortAllocationPolicy.values()[aap];
        int delta = UserTypesDecoderFactory.readUnsignedIntAsOneByte(buffer);
        long ruleExpirationTime = buffer.readLong();
        Address upnpAddress = UserTypesDecoderFactory.readAddress(buffer);
        if (upnpAddress == null) {
            return new Nat(type, mappingPolicy, allocationPolicy, filteringPolicy, 
                    delta, ruleExpirationTime);
        } else {
            return new Nat(type, upnpAddress, mappingPolicy, allocationPolicy,
                    filteringPolicy);
        }

    }
    
    public static Set<Integer> readIntegerSet(ByteBuf buffer) {
        int size = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        Set<Integer> integers = new HashSet<Integer>(size);
        for(int i = 0; i < size; i++) {
            int l = buffer.readInt();
            integers.add(l);
        }
        return integers;
    }
    
    public static Set<Long> readLongSet(ByteBuf buffer) {
        int size = UserTypesDecoderFactory.readUnsignedIntAsTwoBytes(buffer);
        Set<Long> longs = new HashSet<Long>(size);
        for(int i = 0; i < size; i++) {
            long l = buffer.readLong();
            longs.add(l);
        }
        return longs;
    }
    
}
