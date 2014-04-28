package se.sics.gvod.gradient.events;

import java.util.List;
import se.sics.gvod.common.VodDescriptor;
import se.sics.kompics.Response;

/**
 * view event raised always after shuffling/gradient-exchange/vod-exchange. 
 * 
 * @author jim
 */
public class FingersResponse extends Response
{
    private final List<VodDescriptor> nodes;

    public FingersResponse(FingersRequest request, List<VodDescriptor> nodes) {
        super(request);
        this.nodes = nodes;
    }

    public List<VodDescriptor> getNodes() {
        return nodes;
    }

}
