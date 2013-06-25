package se.sics.gvod.common;

/**
 * The <code>Block</code> class.
 *
 * @author Cosmin Arad <cosmin@sics.se>
 * @version $Id: Block.java 1789 2010-03-05 13:37:21Z jdowling $
 */
public final class Block {

    private final int pieceIndex;
    private final int subpieceIndex;
    private final long timeRequested;

    public Block(int pieceIndex, int subpieceIndex,
            long timeRequested) {
        super();
        this.pieceIndex = pieceIndex;
        this.subpieceIndex = subpieceIndex;
        this.timeRequested = timeRequested;
    }

    public long getTimeRequested() {
        return timeRequested;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }

    public int getSubpieceIndex() {
        return subpieceIndex;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + subpieceIndex;
        result = prime * result + pieceIndex;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Block other = (Block) obj;
        if (subpieceIndex != other.subpieceIndex) {
            return false;
        }
        if (pieceIndex != other.pieceIndex) {
            return false;
        }
        return true;
    }
}
