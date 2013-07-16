package se.sics.gvod.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.net.msgs.RewriteableMsg;

public class NettyUdpHandler extends NettyBaseHandler<DatagramPacket> {

	private static final Logger logger = LoggerFactory.getLogger(NettyUdpHandler.class);

	private final MsgFrameDecoder decoder;

	public NettyUdpHandler(NettyNetwork component, InetAddress addr, int port,
			Class<? extends MsgFrameDecoder> msgDecoderClass) {
		super(component, addr, port);

		try {
			this.decoder = msgDecoderClass.newInstance();
		} catch (Exception e) {
			throw new Error(e.getMessage());
		}
	}

	@Override
	protected Transport getProtocol() {
		return Transport.UDP;
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

			rewrittenMsg.getDestination().setIp(getAddr());
			rewrittenMsg.getDestination().setPort(getPort());

			// TODO - for UPNP, the port on which the data is sent from the NAT
			// may not be the same as the mapped port - see
			// https://tools.ietf.org/html/rfc4380.
			// In this case, we should check if it is Upnp, and if so, then
			// don't re-write the source address.

			Channel c = ctx.channel();
			if (c instanceof DatagramChannel) {
				getComponent().deliverMessage(rewrittenMsg);
			} else {
				logger.warn("Received a message over a non-DatagramChannel of type {}",
						c.getClass());
			}
		}
	}
}
