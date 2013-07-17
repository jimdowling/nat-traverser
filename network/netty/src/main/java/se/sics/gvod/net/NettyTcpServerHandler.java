package se.sics.gvod.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

import java.net.InetAddress;

public class NettyTcpServerHandler extends NettyTcpHandler {

	public NettyTcpServerHandler(NettyNetwork component) {
		super(component);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		SocketChannel channel = (SocketChannel) ctx.channel();
		getComponent().addLocalTcpSocket(channel, channel.remoteAddress());
		super.channelActive(ctx);
	}
}
