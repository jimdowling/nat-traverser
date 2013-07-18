package se.sics.gvod.net;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.msgs.RewriteableMsg;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Steffen Grohsschmiedt
 */
public class NettyStreamHandler extends NettyBaseHandler<RewriteableMsg> {

	private static final Logger logger = LoggerFactory.getLogger(NettyStreamHandler.class);

	public NettyStreamHandler(NettyNetwork component, Transport protocol) {
		super(component, protocol);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RewriteableMsg msg) throws Exception {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();

        if (remoteAddress instanceof InetSocketAddress) {
            updateAddress(msg, ctx, (InetSocketAddress) remoteAddress);
            getComponent().deliverMessage(msg);
        }
	}
}
