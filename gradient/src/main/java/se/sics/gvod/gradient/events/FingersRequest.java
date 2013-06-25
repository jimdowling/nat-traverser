package se.sics.gvod.gradient.events;

import se.sics.kompics.Request;

/**
 * view event raised always after shuffling/gradient-exchange/vod-exchange. 
 * 
 * @author jim
 */
public class FingersRequest extends Request
{
    private final int size;
    private final int chunk;

    public FingersRequest(int chunk, int size) {
        this.chunk = chunk;
        this.size = size;
    }

    public int getSize() {
        return size;
    }
    public int getChunk() {
        return chunk;
    }

}
