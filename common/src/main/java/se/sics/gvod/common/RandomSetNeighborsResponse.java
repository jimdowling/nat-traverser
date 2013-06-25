package se.sics.gvod.common;

import se.sics.kompics.Response;

public final class RandomSetNeighborsResponse extends Response {

    /**
     * the current set of Cyclon neighbors, time-stamped (to show freshness).
     */
    private final VodNeighbors neighbors;
    private final Utility utility;

    public RandomSetNeighborsResponse(RandomSetNeighborsRequest request,
            VodNeighbors neighbors, UtilityVod utility) {
        super(request);
        this.neighbors = neighbors;
        this.utility = new UtilityVod(utility.getChunk(),utility.getPiece(),utility.getOffset());
    }

    public VodNeighbors getNeighbors() {
        return neighbors;
    }

    public Utility getUtility() {
        return utility;
    }
}
