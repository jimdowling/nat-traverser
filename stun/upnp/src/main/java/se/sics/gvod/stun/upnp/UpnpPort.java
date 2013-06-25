package se.sics.gvod.stun.upnp;

import se.sics.kompics.PortType;
import se.sics.gvod.stun.upnp.events.MapPortRequest;
import se.sics.gvod.stun.upnp.events.MapPortResponse;
import se.sics.gvod.stun.upnp.events.MapPortsRequest;
import se.sics.gvod.stun.upnp.events.MapPortsResponse;
import se.sics.gvod.stun.upnp.events.MappedPortsChanged;
import se.sics.gvod.stun.upnp.events.ShutdownUpnp;
import se.sics.gvod.stun.upnp.events.UnmapPortsRequest;
import se.sics.gvod.stun.upnp.events.UnmapPortsResponse;
import se.sics.gvod.stun.upnp.events.UpnpGetPublicIpRequest;
import se.sics.gvod.stun.upnp.events.UpnpGetPublicIpResponse;



public final class UpnpPort extends PortType {
	{
//		negative(MapPortRequest.class);
//                positive(MapPortResponse.class);
		negative(MapPortsRequest.class);
                positive(MapPortsResponse.class);

                negative(UnmapPortsRequest.class);
                positive(UnmapPortsResponse.class);

                negative(UpnpGetPublicIpRequest.class);
                positive(UpnpGetPublicIpResponse.class);

		negative(ShutdownUpnp.class);

                positive(MappedPortsChanged.class);
	}
}
