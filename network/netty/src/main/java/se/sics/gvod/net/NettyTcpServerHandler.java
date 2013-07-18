package se.sics.gvod.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

/**
 * @author Steffen Grohsschmiedt
 */
public class NettyTcpServerHandler extends NettyStreamHandler {

	public NettyTcpServerHandler(NettyNetwork component) {
		super(component, Transport.TCP);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		SocketChannel channel = (SocketChannel) ctx.channel();
		getComponent().addLocalSocket(channel.remoteAddress(), channel);
		super.channelActive(ctx);
	}
}
