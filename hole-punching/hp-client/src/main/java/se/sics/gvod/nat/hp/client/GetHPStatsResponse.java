package se.sics.gvod.nat.hp.client;

import java.util.EnumMap;
import se.sics.kompics.Response;
import se.sics.gvod.common.hp.HPMechanism;

public final class GetHPStatsResponse extends Response
{
    private final EnumMap<HPMechanism, HPStats> stats;
    private final Object attachment;
    public GetHPStatsResponse(GetHPStatsRequest request,
            Object attachment,
            EnumMap<HPMechanism, HPStats> stats)
    {
        super(request);
        this.attachment = attachment;
        this.stats = stats;
    }

    public Object getAttachment()
    {
        return attachment;
    }
    

    public EnumMap<HPMechanism, HPStats> getStats()
    {
        return stats;
    }
    
}
