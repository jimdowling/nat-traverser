/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.msgs;

/**
 *
 * @author jdowling
 */
public class Bytes {

    public static final int BYTE_SIZE_INT = 4;
    public static final int BYTE_SIZE_DOUBLE = 8;
    public static final int BYTE_SIZE_LONG = 8;
    public static final int BYTE_SIZE_TIMESTAMP = 7;

    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                    (byte) (value >>> 24),
                    (byte) (value >> 16 & 0xff),
                    (byte) (value >> 8 & 0xff),
                    (byte) (value & 0xff)};
    }

    public static int byteArrayToInt(byte[] b) throws MessageEncodingException {
        if (b.length != 4) {
            throw new MessageEncodingException("Array not 4 bytes length");
        }
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shiftBy = (4 - 1 - i) * 8;
            value += (b[i] & 0x0FF) << shiftBy;
        }
        return value;
    }

    public static final byte[] longToByteArray(long value) {
        return new byte[]{
                    (byte) (value >>> 56),
                    (byte) (value >> 48 & 0xff),
                    (byte) (value >> 40 & 0xff),
                    (byte) (value >> 32 & 0xff),
                    (byte) (value >> 24 & 0xff),
                    (byte) (value >> 16 & 0xff),
                    (byte) (value >> 8 & 0xff),
                    (byte) (value & 0xff)
                };
    }

    public static final long byteArrayToLong(byte[] b) throws MessageEncodingException {
        if (b.length != 8) {
            throw new MessageEncodingException("Array not 8 bytes length");
        }
        long accum = 0;
        int i = 0;
        for (int shiftBy = 56; shiftBy >= 0; shiftBy -= 8) {
            accum += ((long) (b[i] & 0xff)) << shiftBy;
            i++;
        }
        return accum;
    }

    public static final byte[] doubleToByteArray(double value) {
        long num = Double.doubleToLongBits(value);
        return longToByteArray(num);
    }

    public static final double byteArrayToDouble(byte[] b) throws MessageEncodingException {
        if (b.length != BYTE_SIZE_DOUBLE) {
            throw new MessageEncodingException("Array not"
                    + BYTE_SIZE_DOUBLE + "8 bytes length");
        }
        long num = byteArrayToLong(b);
        return Double.longBitsToDouble(num);

    }



    public static final long fourBytesToLong(byte[] value) throws MessageDecodingException {
        if (value.length < 4) {
            throw new MessageDecodingException("Byte array too short!");
        }
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        int temp3 = value[3] & 0xFF;
        return (((long) temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
    }

    public static final String byteArrayToString(byte[] value) {
        StringBuilder bld = new StringBuilder();
        for (byte b : value) {
            char c = (char) b;
            bld.append(c);
        }
        return bld.toString();
    }

    /*
     * returns 1 or 0, where 1 is true, 0 is false
     */
//    public static final void writeBoolean(ChannelBuffer buffer, boolean value) throws MessageEncodingException {
//        if (value) {
//            writeUnsignedintAsOneByte(buffer, 1);
//        }
//        writeUnsignedintAsOneByte(buffer, 0);
//    }
    /*
     * expects 1, or 0 as a parameter
     * 1 - true, 0 - false
     */



//    public static final Timestamp byteArrayToDate(ChannelBuffer buffer) throws MessageDecodingException {
//        byte[] value = new byte[7];
//        buffer.readBytes(value);
//        if (value.length != 7) {
//            throw new MessageDecodingException("array length must be 7 elements!");
//        }
//        Timestamp cal = new Timestamp();
//        byte[] year = new byte[2];
//        year[0] = value[0];
//        year[1] = value[1];
//        cal.set(twoBytesToInt(year), oneByteToUnsignedInt(value[2]), oneByteToUnsignedInt(value[3]),
//                oneByteToUnsignedInt(value[4]), oneByteToUnsignedInt(value[5]), oneByteToUnsignedInt(value[6]));
//        return cal;
//    }

//    public static final byte[] dateToByteArray(Timestamp value) throws MessageEncodingException {
//        if (value == null) {
//            throw new MessageEncodingException("timestamp value is null");
//        }
//        byte[] b = new byte[7];
//        System.arraycopy(unsignedintToTwoBytes(value.year), 0, b, 0, 2);	//2 bytes
//        b[2] = unsignedintToOneByte(value.month);							//1 byte
//        b[3] = unsignedintToOneByte(value.day);					//1 byte
//        b[4] = unsignedintToOneByte(value.hour);					//1 byte
//        b[5] = unsignedintToOneByte(value.minute);						//1 byte
//        b[6] = unsignedintToOneByte(value.second);						//1 byte
//        return b;
//    }

//    public static final byte[] IpToByteArray(String ip) throws MessageEncodingException {
//        String[] ipBlocks = ip.split("\\.");
//        if (ipBlocks.length != 4) {
//            throw new MessageEncodingException("IP address format should be xxx.xxx.xxx.xxx, where xxx is between 0 and 255");
//        }
//        byte[] byteIP = new byte[4];
//        for (int i = 0; i < 4; i++) {
//            int ipInt = Integer.valueOf(ipBlocks[i]);
//            byteIP[i] = unsignedintToOneByte(ipInt);
//        }
//        return byteIP;
//    }

//    public static final String byteArrayToIpAsString(byte[] ip) throws MessageEncodingException {
//        if (ip.length != 4) {
//            throw new MessageEncodingException("ip address must have 4 bytes length");
//        }
//        StringBuffer stBuf = new StringBuffer();
//        for (int i = 0; i < 4; i++) {
//            int ipInt = oneByteToUnsignedInt(ip[i]);
//            stBuf.append(ipInt);
//            stBuf.append('.');
//        }
//        return stBuf.substring(0, stBuf.length() - 1);
//    }
}
