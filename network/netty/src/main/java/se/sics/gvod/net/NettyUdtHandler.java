package se.sics.gvod.net;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.msgs.RewriteableMsg;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NettyUdtHandler extends NettyBaseHandler<RewriteableMsg> {

	private static final Logger logger = LoggerFactory.getLogger(NettyUdtHandler.class);

	public NettyUdtHandler(NettyNetwork component) {
		super(component);
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, RewriteableMsg msg) throws Exception {
		SocketAddress remoteAddress = ctx.channel().remoteAddress();

		if (remoteAddress instanceof InetSocketAddress) {
            updateAddress(msg, ctx, (InetSocketAddress) remoteAddress);
			getComponent().deliverMessage(msg);
		}
	}

	@Override
	protected Transport getProtocol() {
		return Transport.UDT;
	}
}
