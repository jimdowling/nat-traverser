package se.sics.gvod.nat.hp.client.events;


import se.sics.kompics.Response;

public final class RegisterWithRendezvousServerResponse extends Response
{
   private final ResponseType responseType;
   private final RegisterWithRendezvousServerRequest request;
   public enum ResponseType
   {
       OK, FAILED
   }
    public RegisterWithRendezvousServerResponse(RegisterWithRendezvousServerRequest request,
            ResponseType responseType)
    {
        super(request);
        this.request = request;
        this.responseType = responseType;
    }

    public ResponseType getResponseType()
    {
        return responseType;
    }

    public RegisterWithRendezvousServerRequest getRequest()
    {
        return request;
    }

}
