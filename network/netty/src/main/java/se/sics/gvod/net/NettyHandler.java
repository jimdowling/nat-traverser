/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.common.msgs.DirectMsgNetty;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.net.events.NetworkException;
import se.sics.gvod.net.msgs.RewriteableMsg;

/**
 * 
 * @author jdowling
 */
public class NettyHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private static final Logger logger = LoggerFactory.getLogger(NettyHandler.class);
	private static final BaseMsgFrameDecoder decoder = new BaseMsgFrameDecoder();
	
	private final NettyNetwork component;
	private final InetAddress addr;
	private final int port;

	public NettyHandler(NettyNetwork component, InetAddress addr, int port) {
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

		// channel is connectionless
		Channel channel = ctx.channel();
		SocketAddress address = channel.remoteAddress();
		InetSocketAddress inetAddress = null;
		if (address != null && address instanceof InetSocketAddress) {
			inetAddress = (InetSocketAddress) address;
			component.networkException(new NetworkException(inetAddress, Transport.UDP));
		}

		component.exceptionCaught(ctx, cause);

		StringBuilder sb = new StringBuilder();
		// TODO Not sure for what this code was good for
//		Object prob = ctx.getAttachment();
//		if (prob != null) {
//			sb.append(prob.getClass().getCanonicalName()).append(":");
//		}
		sb.append(cause.getMessage());

		logger.error(sb.toString());
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		component.channelUnregistered(ctx);
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		RewriteableMsg rewrittenMsg = (RewriteableMsg) decoder.parse(msg.content());

		// session-less UDP means that remoteAddresses cannot be found in
		// the channel object, but only in the MessageEvent object.
		SocketAddress srcAddr = msg.sender();

		if (srcAddr instanceof InetSocketAddress) {
			InetSocketAddress is = (InetSocketAddress) srcAddr;

			rewrittenMsg.getSource().setIp(is.getAddress());
			rewrittenMsg.getSource().setPort(is.getPort());

			rewrittenMsg.getDestination().setIp(addr);
			rewrittenMsg.getDestination().setPort(port);

			// TODO - this is terrible code. All we need to do is change
			// the VodAddress in VodMsg, not Address in RewriteableMsg
			rewrittenMsg.rewriteDestination(rewrittenMsg.getDestination());
			rewrittenMsg.rewritePublicSource(rewrittenMsg.getSource());

			// TODO - for UPNP, the port on which the data is sent from the NAT
			// may not be the same as the mapped port - see
			// https://tools.ietf.org/html/rfc4380.
			// In this case, we should check if it is Upnp, and if so, then
			// don't re-write the source address.

			// UPNP Port-mapped + Symmetric NAT (different port out than in),
			// but same IP
			// if (is.getAddress().equals(rewrittenMsg.getSource().getIp()) ==
			// false) {
			// rewrittenMsg.rewritePublicSource(newSrc);
			// } else {
			// logger.info("UPnP + Symmetric packet recvd from " +
			// rewrittenMsg.getSource().getId());
			// }
			// } else {
			// // logger.trace("No nat: public {} private {}", newSrc.getId(),
			// src.getId());
			// }

			Channel c = ctx.channel();
			if (c instanceof DatagramChannel) {
				DatagramChannel channel = (DatagramChannel) c;
				component.deliverMessage(rewrittenMsg, channel);
			} else {
				logger.warn("Received a message over a non-DatagramChannel of type {}",
						c.getClass());
			}
		}
	}
}

