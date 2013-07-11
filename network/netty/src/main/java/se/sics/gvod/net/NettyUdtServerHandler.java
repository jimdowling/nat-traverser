package se.sics.gvod.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.udt.UdtChannel;

import java.net.InetAddress;

public class NettyUdtServerHandler extends NettyUdtHandler {

	public NettyUdtServerHandler(NettyNetwork component, InetAddress addr, int port) {
		super(component, addr, port);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		UdtChannel channel = (UdtChannel) ctx.channel();
		getComponent().addLocalUdtSocket(channel, channel.remoteAddress());
		super.channelActive(ctx);
	}
}
