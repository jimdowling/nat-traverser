package se.sics.gvod.common;

/**
 * TODO: Rewrite this to use Java's BitSet - more efficient one bit, instead
 * of of byte used
 *
 * Container of a byte array representing set and unset bits.
 */
public class BitField {

    public static final int NUM_SUBPIECES_PER_PIECE=16;
    public static final int NUM_PIECES_PER_CHUNK=128;
    public static final int SUBPIECE_SIZE=1024;

    private final byte[] subpieceField;
    private final byte[] piecefield;
    private final byte[] chunkfield;
    private final int size, numPieces, numChunks;
    private int firstUncompletedBit = 0;
    private int firstUncompletedPiece = 0;
    private int firstUncompletedChunk = 0;
    private int total = 0;

    /**
     * Creates a new BitField that represents <code>size</code> unset bits.
     */
    public BitField(int size) {
        this.size = size;
        if (size % NUM_SUBPIECES_PER_PIECE == 0) {
            this.numPieces = size / NUM_SUBPIECES_PER_PIECE;
        } else {
            this.numPieces = (size / NUM_SUBPIECES_PER_PIECE) + 1;
        }
        if (numPieces % NUM_PIECES_PER_CHUNK == 0) {
            this.numChunks = numPieces / NUM_PIECES_PER_CHUNK;
        } else {
            this.numChunks = (numPieces / NUM_PIECES_PER_CHUNK) + 1;
        }
        int arraysize = ((size - 1) / 8) + 1;
        subpieceField = new byte[arraysize];
        arraysize = ((((size - 1) / NUM_SUBPIECES_PER_PIECE)) / 8) + 1;
        piecefield = new byte[arraysize];
        arraysize = (((size - 1) / (2 * 1024)) / 8) + 1;
        chunkfield = new byte[arraysize];
    }

    /**
     * Creates a new BitField that represents <code>size</code> bits as set by
     * the given byte array. This will make a copy of the array. Extra bytes
     * will be ignored.
     * 
     * @exception ArrayOutOfBoundsException
     *                if give byte array is not large enough.
     */
    /*  public BitField (byte[] bitfield, int size){
    this.size = size;
    int arraysize = ((size - 1) / 8) + 1;
    this.bitfield = new byte[arraysize];

    // XXX - More correct would be to check that unused bits are
    // cleared or clear them explicitly ourselves.
    System.arraycopy(bitfield, 0, this.bitfield, 0, arraysize);
    }
     */
    /**
     * This returns the actual byte array used. Changes to this array effect
     * this BitField. Note that some bits at the end of the byte array are
     * supposed to be always unset if they represent bits bigger then the size
     * of the bitfield.
     */
    public byte[] getSubpieceField() {
        return subpieceField;
    }

    /**
     * Return the size of the BitField. The returned value is one bigger then
     * the last valid bit number (since bit numbers are counted from zero).
     */
    public int size() {
        return size;
    }

    public int pieceFieldSize() {
        // this is final, no sync problem with Sender thread.
        return numPieces;
    }

    public byte[] getChunkfield() {
        return chunkfield;
    }

//    public byte[] getPiecefield() {
//        return piecefield;
//    }
    public int getPieceFieldLength() {
//        synchronized (piecefield) {
            return piecefield.length;
//        }

    }

    public int getChunkFieldSize() {
        return numChunks;
    }

    public byte[][] getAvailablePieces(UtilityVod utility) {
        byte[][] result = new byte[utility.getOffset()][NUM_SUBPIECES_PER_PIECE];
        for (int i = 0; i < utility.getOffset(); i++) {
            for (int j = 0; j < NUM_SUBPIECES_PER_PIECE; j++) {
                if ((utility.getChunk() + i) * NUM_SUBPIECES_PER_PIECE + j >= getPieceFieldLength()) {
                    result[i][j] = 0;
                } else {
//                    synchronized (piecefield) {
                        result[i][j] = piecefield[(utility.getChunk() + i) * NUM_SUBPIECES_PER_PIECE + j];
//                    }
                }
            }
        }
        return result;
    }

    public byte[][] getAvailablePiecesInIdx(UtilityVod utility, int idxStart, int idxSize) {
        if (utility.getChunk() != 0) {
            return null;
        }
        byte[][] result = new byte[idxSize][NUM_SUBPIECES_PER_PIECE];
        for (int i = 0; i < idxSize; i++) {
            for (int j = 0; j < NUM_SUBPIECES_PER_PIECE; j++) {
                if ((idxStart + i) * NUM_SUBPIECES_PER_PIECE + j >= getPieceFieldLength()) {
                    result[i][j] = 0;
                } else {
//                    synchronized (piecefield) {
                        result[i][j] = piecefield[(idxStart + i) * NUM_SUBPIECES_PER_PIECE + j];
//                    }
                }
            }
        }
        return result;
    }

    /**
     * Sets the given bit to true.
     * 
     * @exception IndexOutOfBoundsException
     *                if bit is smaller then zero bigger then size (inclusive).
     */
    public void set(int bit, boolean setPiece) {
        if (bit < 0 || bit >= size) {
            throw new IndexOutOfBoundsException(Integer.toString(bit));
        }
        int index = bit / 8;
        int mask = NUM_PIECES_PER_CHUNK >> (bit % 8);
        subpieceField[index] |= mask;

        while (firstUncompletedBit < size && get(firstUncompletedBit)) {
            firstUncompletedBit++;
        }

        if (setPiece) {
            completePiecefield(index);
        }
    }

    public void remove(int bit) {
        if (bit < 0 || bit >= size) {
            throw new IndexOutOfBoundsException(Integer.toString(bit));
        }

        if (get(bit)) {
            int index = bit / 8;
            int mask = NUM_PIECES_PER_CHUNK >> (bit % 8);
            subpieceField[index] ^= mask;
            if (firstUncompletedBit > bit) {
                firstUncompletedBit = bit;
            }
            removePiece(index);
        }
    }

    public void removePiece(int index) {
        index = index - (index % 2);
        int piece = index / 2;
        if (getPiece(index / 2)) {
            int mask = NUM_PIECES_PER_CHUNK >> ((index / 2) % 8);
            index = index / NUM_SUBPIECES_PER_PIECE;
//            synchronized (piecefield) {
                piecefield[index] ^= mask;
//            }
            if (firstUncompletedPiece > piece) {
                firstUncompletedPiece = piece;
            }
            removeChunk(index);
        }
    }

    public void removeChunk(int index) {
        index = index - (index % NUM_SUBPIECES_PER_PIECE);
        int chunk = index / NUM_SUBPIECES_PER_PIECE;
        if (getChunk(chunk)) {
            int mask = NUM_PIECES_PER_CHUNK >> ((index / NUM_SUBPIECES_PER_PIECE) % 8);
            index = index / NUM_PIECES_PER_CHUNK;
            chunkfield[index] |= mask;
            if (firstUncompletedChunk > chunk) {
                firstUncompletedChunk = chunk;
            }
        }
    }

    public void setPiece(int piece) {
        completePiecefield(piece * 2);
    }
    /* if we have all the subpieces for this part of the array we check
     * if we have a complet piece
     */

    private void completePiecefield(int index) {
        int mask;
        if (subpieceField[index] == -1 || index == subpieceField.length - 1) {
            index = index - (index % 2);
            boolean flag = true;
            int lim = index + 2;
            if (lim > subpieceField.length) {
                lim = subpieceField.length;
            }
            for (int i = index; i < lim; i++) {
                if (i == subpieceField.length - 1 && (size % 8) != 0 && subpieceField[i] != -1) {
                    for (int j = i * 8; j < i * 8 + (size % 8); j++) {
                        if (!get(j)) {
                            flag = false;
                            break;
                        }
                    }
                    if (!flag) {
                        break;
                    }
                } else if (subpieceField[i] != -1) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                mask = NUM_PIECES_PER_CHUNK >> ((index / 2) % 8);
                index = index / NUM_SUBPIECES_PER_PIECE;
//                synchronized (piecefield) {
                    piecefield[index] |= mask;
//                }
                total++;

                int limite;
                if (size % NUM_SUBPIECES_PER_PIECE == 0) {
                    limite = (size / NUM_SUBPIECES_PER_PIECE);
                } else {
                    limite = (size / NUM_SUBPIECES_PER_PIECE) + 1;
                }

                while (firstUncompletedPiece < limite && getPiece(firstUncompletedPiece)) {
                    firstUncompletedPiece++;
                }
                completeChunkfield(index);
            }

        }


    }

    public int getTotal() {
        return total;
    }

    private void completeChunkfield(int index) {

        // we don't need to synchronize access to piecefield here, as it
        // is only the GVod thread that is calling this code - it is a reader.
        // the sender thread also only reads, so we shouldnt get a concurrent
        // modication exception.
        int pieceFieldLength = getPieceFieldLength();
        if (piecefield[index] == -1 || index == pieceFieldLength - 1) {
            index = index - (index % NUM_SUBPIECES_PER_PIECE);
            boolean flag = true;
            int lim = index + NUM_SUBPIECES_PER_PIECE;
            if (lim > pieceFieldLength) {
                lim = pieceFieldLength;
            }
            for (int i = index; i < lim; i++) {
                if ((numPieces % NUM_SUBPIECES_PER_PIECE) != 0 && i == pieceFieldLength - 1 && piecefield[i] != -1) {
                    for (int j = i * 8; j < i * 8 + (numPieces % 8); j++) {
                        if (!getPiece(j)) {
                            flag = false;
                            break;
                        }
                    }
                    if (!flag) {
                        break;
                    }
                } else if (piecefield[i] != -1) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                int mask = NUM_PIECES_PER_CHUNK >> ((index / NUM_SUBPIECES_PER_PIECE) % 8);
                index = index / NUM_PIECES_PER_CHUNK;
                chunkfield[index] |= mask;
                while (firstUncompletedChunk < chunkfield.length * 8 && getChunk(firstUncompletedChunk)) {
                    firstUncompletedChunk++;
                }
            }

        }
    }

    /**
     * Return true if the bit is set or false if it is not.
     *
     * @exception IndexOutOfBoundsException
     *                if bit is smaller then zero bigger then size (inclusive).
     */
    public boolean get(int bit) {
        if (bit < 0 || bit >= size) {
            throw new IndexOutOfBoundsException(Integer.toString(bit));
        }

        int index = bit / 8;
        int mask = NUM_PIECES_PER_CHUNK >> (bit % 8);
        return (subpieceField[index] & mask) != 0;
    }

    public boolean getPiece(int piece) {
        if (piece < 0 || piece > (size / NUM_SUBPIECES_PER_PIECE) + 1) {
            int lim = (size / NUM_SUBPIECES_PER_PIECE) + 1;
            System.out.println("size = " + size + " lim = " + lim);
            throw new IndexOutOfBoundsException(Integer.toString(piece));
        }

        int index = piece / 8;
        int mask = NUM_PIECES_PER_CHUNK >> (piece % 8);

        // TODO synchronize access to the piecefield - sender thread reads
        // and kompics components write to it.
        boolean res;
//        synchronized (piecefield) {
            res = (piecefield[index] & mask) != 0;
//        }
            return res;
    }

    public boolean getChunk(int chunk) {
        if (chunk < 0 || chunk >= chunkfield.length * 8) {
            throw new IndexOutOfBoundsException(Integer.toString(chunk));
        }

        int index = chunk / 8;
        int mask = NUM_PIECES_PER_CHUNK >> (chunk % 8);
        return (chunkfield[index] & mask) != 0;
    }

    @Override
    public String toString() {
        // Not very efficient
        StringBuffer sb = new StringBuffer("BitField[");
        for (int i = 0; i
                < size; i++) {
            if (get(i)) {
                sb.append(' ');
                sb.append(i);
            }

        }
        sb.append(" ]");

        return sb.toString();
    }

    public String getHumanReadable() {
        StringBuffer sb = new StringBuffer("BitField[");
        for (int i = 0; i
                < size; i++) {
            if (get(i)) {
                sb.append('+');
            } else {
                sb.append('-');
            }

        }
        sb.append("]\npiecefield[");
        for (int i = 0; i
                < (size / NUM_SUBPIECES_PER_PIECE) + 1; i++) {
            if (getPiece(i)) {
                sb.append('+');
            } else {
                sb.append('-');
            }

        }
        sb.append("]\nchunkfield[");
        for (int i = 0; i
                < (size / 2048) + 1; i++) {
            if (getChunk(i)) {
                sb.append('+');
            } else {
                sb.append('-');
            }

        }
        sb.append("]");
        return sb.toString();
    }

    public String getHumanReadable2() {
        StringBuffer sb = new StringBuffer("BitField[");
        int count = 0;
        for (int i = 0; i
                < size; i++) {
            if (get(i)) {
                count++;
            }

        }
        float p = (float) count / size;
        sb.append(p * 100 + "%]\npiecefield[");
        count =
                0;
        int limite;
        if (size % NUM_SUBPIECES_PER_PIECE == 0) {
            limite = (size / NUM_SUBPIECES_PER_PIECE);
        } else {
            limite = (size / NUM_SUBPIECES_PER_PIECE) + 1;
        }
        for (int i = 0; i < limite; i++) {
            if (getPiece(i)) {
                count++;
            }

        }
        p = (float) count / (size / NUM_SUBPIECES_PER_PIECE);
        sb.append(p * 100 + "%]\nchunkfield[");
        for (int i = 0; i
                < (size / 2048) + 1; i++) {
            if (getChunk(i)) {
                sb.append('+');
            } else {
                sb.append('-');
            }

        }
        sb.append("]");
        return sb.toString();
    }

    public String getChunkHumanReadable() {
        StringBuffer sb = new StringBuffer("ChunkField[");
        for (int i = 0; i < (size / 2048) + 1; i++) {
            if (getChunk(i)) {
                sb.append('+');
            } else {
                sb.append('-');
            }

        }
        sb.append("]");
        return sb.toString();
    }

    public String getPiecesHummanRedable(int utility, int marge) {
        StringBuffer sb = new StringBuffer("PiecesField[");
        for (int i = 0; i < marge; i++) {
            for (int j = 0; j < NUM_PIECES_PER_CHUNK; j++) {
                if ((utility + i) * NUM_PIECES_PER_CHUNK + j >= numPieces) {
                    break;
                }
                if (getPiece((utility + i) * NUM_PIECES_PER_CHUNK + j)) {
                    sb.append('+');
                } else {
                    sb.append('-');
                }
            }
            sb.append('|');
        }
        sb.append("]");
        return sb.toString();
    }

    public int getFirstUncompletedBit() {
        return firstUncompletedBit;
    }

    public int getFirstUncompletedPiece() {
        return firstUncompletedPiece;
    }

    public int getFirstUncompletedChunk() {
        return firstUncompletedChunk;
    }

    public void setFirstUncompletedPiece(int firstUncompletedPiece) {
        this.firstUncompletedPiece = firstUncompletedPiece;
        this.firstUncompletedBit = firstUncompletedPiece * NUM_SUBPIECES_PER_PIECE;
    }



    public int setFirstUncompletedChunk(int utility) {
        firstUncompletedChunk = utility;
        while (firstUncompletedChunk < chunkfield.length * 8
                && getChunk(firstUncompletedChunk)) {
            firstUncompletedChunk++;
        }

        firstUncompletedPiece = firstUncompletedChunk * NUM_PIECES_PER_CHUNK;
        if (firstUncompletedPiece > numPieces) {
            firstUncompletedPiece = numPieces;
        }
        while (firstUncompletedPiece < getPieceFieldLength() * 8 && getPiece(firstUncompletedPiece)) {
            firstUncompletedPiece++;
        }

        firstUncompletedBit = firstUncompletedPiece * NUM_SUBPIECES_PER_PIECE;
        if (firstUncompletedBit > size) {
            firstUncompletedBit = size;
        }
        while (firstUncompletedBit < size && get(firstUncompletedBit)) {
            firstUncompletedBit++;
        }
        if (firstUncompletedChunk >= numChunks) {
            return -10;
        } else {
            return firstUncompletedChunk;
        }
    }
}
