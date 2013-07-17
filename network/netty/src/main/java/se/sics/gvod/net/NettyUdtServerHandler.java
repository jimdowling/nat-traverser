package se.sics.gvod.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.udt.UdtChannel;

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
