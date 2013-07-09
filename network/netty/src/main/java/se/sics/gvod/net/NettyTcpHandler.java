package se.sics.gvod.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.net.events.NetworkException;
import se.sics.gvod.net.msgs.RewriteableMsg;

public class NettyTcpHandler extends SimpleChannelInboundHandler<RewriteableMsg> {

	private static final Logger logger = LoggerFactory.getLogger(NettyTcpHandler.class);

	private final NettyNetwork component;
	private final InetAddress addr;
	private final int port;

	public NettyTcpHandler(NettyNetwork component, InetAddress addr, int port) {
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
			component.networkException(new NetworkException(inetAddress, Transport.TCP));
		}

		component.exceptionCaught(ctx, cause);
		logger.error(cause.getMessage());
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		component.channelUnregistered(ctx);
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, RewriteableMsg msg) throws Exception {
		SocketAddress srcAddr = ctx.channel().remoteAddress();

		// TODO Check if this really need to be done
		if (srcAddr instanceof InetSocketAddress) {
			InetSocketAddress is = (InetSocketAddress) srcAddr;

			msg.getSource().setIp(is.getAddress());
			msg.getSource().setPort(is.getPort());

			msg.getDestination().setIp(addr);
			msg.getDestination().setPort(port);

			// TODO - this is terrible code. All we need to do is change
			// the VodAddress in VodMsg, not Address in RewriteableMsg
			msg.rewriteDestination(msg.getDestination());
			msg.rewritePublicSource(msg.getSource());

			Channel c = ctx.channel();
			if (c instanceof DatagramChannel) {
				DatagramChannel channel = (DatagramChannel) c;
				component.deliverMessage(msg, channel);
			} else {
				logger.warn("Received a message over a non-DatagramChannel of type {}",
						c.getClass());
			}
		}
	}
}
