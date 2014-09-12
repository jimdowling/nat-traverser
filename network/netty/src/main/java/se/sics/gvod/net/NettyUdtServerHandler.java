package se.sics.gvod.net;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.udt.UdtChannel;

/**
 * Handler for a UDT server. Add new connections to the set of local connections.
 *
 * @author Steffen Grohsschmiedt
 */
@ChannelHandler.Sharable
public class NettyUdtServerHandler extends NettyStreamHandler {

	public NettyUdtServerHandler(NettyNetwork component) {
		super(component, Transport.UDT);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		UdtChannel channel = (UdtChannel) ctx.channel();
		getComponent().addLocalSocket(channel.remoteAddress(), channel);
		super.channelActive(ctx);
	}
}
