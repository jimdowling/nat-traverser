/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common;

import se.sics.gvod.config.VodConfig;

/**
 *
 * @author jdowling
 */
public class UtilityVod implements Utility {
    
    /**
     * 1 byte = availableBandwidth, 2 bytes = chunk, 8 bytes = piece, 4 bytes = offset
     */
    private int chunk;
    private long piece;
    private final int offset;
    private int availableBandwidth;

    // TODO: use this order seeds by their bandwidth/uptime.
    // The best nodes in the swarm act as servers for peer-assisted downloading.
    public UtilityVod(int chunk) {
        this(chunk, 0);
    }

    public UtilityVod(int chunk, int offset) {
        this(chunk, chunk * 128, 0);
    }

    public UtilityVod(int chunk, long piece, int offset) {
        this(chunk, piece, offset, 0);
    }
    
    public UtilityVod(int chunk, long piece, int offset, int availableBandwidth) {
        this.availableBandwidth = availableBandwidth;
        this.chunk = chunk;
        this.piece = piece;
        this.offset = offset;
    }

    public int getAvailableBandwidth() {
        return availableBandwidth;
    }

    public void setAvailableBandwidth(short availableBandwidth) {
        this.availableBandwidth = availableBandwidth;
    }

    /**
     *
     * @return download position, VodConfig.SEEDER_UTILITY (previously -10) means I'm a seeder - whole file downloaded.
     */
    public int getChunk() {
        return chunk;
    }

    public long getPiece() {
        return piece;
    }

    public int getOffset() {
        return offset;
    }

    public int getPieceOffset() {
        return offset * 128;
    }
    
    public boolean notInBittorrentSet(UtilityVod utility) {
        return (utility.isSeeder() == false && utility.getPiece() < getPiece() - getPieceOffset()
                    || utility.getPiece() > getPiece() + getPieceOffset());        
    }
    
    public boolean validInUpperSet(UtilityVod utility) {
        return utility.getChunk() < getChunk() + getOffset();        
    }
    
    public boolean memberUpperSet(UtilityVod utility) {
        return utility.getChunk() >= getChunk() + getOffset();        
    }

    //TODO prendre en compte les pieces déjà dl
    public void setChunk(int chunk) {
        this.chunk = chunk;
        this.piece = chunk * 128;
    }

    public void setChunkOnly(int chunk) {
        this.chunk = chunk;
    }

    public void addPiece() {
        piece++;
    }

    public void setPiece(long piece) {
        this.piece = piece;
    }

    public void addChunk() {
        chunk++;
    }

    public int getSizeInBytes() {
        return 2 /*chunk*/ + 8 /*piece*/ + 2 /*offset*/ + 2 /* available b/w */;
    }

    public boolean isSeeder() {
        if (chunk >= VodConfig.SEEDER_UTILITY_VALUE) {
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("Chunk = %d,\tPiece = %d,\tOffset = %d,\tB/W = %d", chunk, piece, offset, availableBandwidth);
    }
    
    public int getTotalUtility(Self self) {
        // don't add utility for b/w to Natt'd nodes. Prefer open
        // nodes, even if they don't have higher available b/w
        if (getChunk() != VodConfig.SEEDER_UTILITY_VALUE || !self.isOpen()) {
            return getChunk();
        }
        return getChunk() + getAvailableBandwidth();
    }

    @Override
    public int getValue() {
        if (getChunk() != VodConfig.SEEDER_UTILITY_VALUE) {
            return getChunk();
        }
        return getChunk() + getAvailableBandwidth();
    }

    @Override
    public Utility clone() {
        return new UtilityVod(chunk, piece, offset, availableBandwidth);
    }

    @Override
    public Impl getImplType() {
        return Utility.Impl.VodUtility;
    }

}
