package se.sics.gvod.nat.hp.client;

import se.sics.kompics.Event;
import se.sics.kompics.Request;

public final class GetHPStatsRequest extends Request
{
    private final Object attachment;
    public GetHPStatsRequest(Object attachment)
    {
     this.attachment = attachment;
    }

    public Object getAttachment()
    {
        return attachment;
    }
    
}
