package se.sics.gvod.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.net.events.NetworkException;

public abstract class NettyBaseHandler<I> extends SimpleChannelInboundHandler<I> {

	private static final Logger logger = LoggerFactory.getLogger(NettyTcpHandler.class);

	private final NettyNetwork component;
	private final InetAddress addr;
	private final int port;

	public NettyBaseHandler(NettyNetwork component, InetAddress addr, int port) {
		this.component = component;
		this.addr = addr;
		this.port = port;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		logger.trace("Channel connected");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Channel channel = ctx.channel();
		SocketAddress address = channel.remoteAddress();
		InetSocketAddress inetAddress = null;

		if (address != null && address instanceof InetSocketAddress) {
			inetAddress = (InetSocketAddress) address;
			component.networkException(new NetworkException(inetAddress, getProtocol()));
		}

		component.exceptionCaught(ctx, cause);
		logger.error(cause.getMessage());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		component.channelInactive(ctx, getProtocol());
		super.channelInactive(ctx);
	}
	
	protected abstract Transport getProtocol();

	protected InetAddress getAddr() {
		return addr;
	}

	protected int getPort() {
		return port;
	}

	protected NettyNetwork getComponent() {
		return component;
	}
}
