package se.sics.gvod.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.net.msgs.RewriteableMsg;

public class NettyTcpHandler extends NettyBaseHandler<RewriteableMsg> {

	private static final Logger logger = LoggerFactory.getLogger(NettyTcpHandler.class);

	public NettyTcpHandler(NettyNetwork component, InetAddress addr, int port) {
		super(component, addr, port);
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, RewriteableMsg msg) throws Exception {
		SocketAddress srcAddr = ctx.channel().remoteAddress();

		if (srcAddr instanceof InetSocketAddress) {
			InetSocketAddress is = (InetSocketAddress) srcAddr;

			msg.getSource().setIp(is.getAddress());
			msg.getSource().setPort(is.getPort());

			msg.getDestination().setIp(getAddr());
			msg.getDestination().setPort(getPort());

			Channel c = ctx.channel();
			if (c instanceof SocketChannel) {
				getComponent().deliverMessage(msg);
			} else {
				logger.warn("Received a message over a non-SocketChannel of type {}", c.getClass());
			}
		}
	}

	@Override
	protected Transport getProtocol() {
		return Transport.TCP;
	}
}
