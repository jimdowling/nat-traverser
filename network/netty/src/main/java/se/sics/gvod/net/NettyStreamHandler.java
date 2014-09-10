package se.sics.gvod.net;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.msgs.RewriteableMsg;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Handler for stream based protocols.
 *
 * @author Steffen Grohsschmiedt
 */
public class NettyStreamHandler extends NettyBaseHandler<RewriteableMsg> {

	private static final Logger logger = LoggerFactory.getLogger(NettyStreamHandler.class);

	public NettyStreamHandler(NettyNetwork component, Transport protocol) {
		super(component, protocol);
	}

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        getComponent().channelInactive(ctx, getProtocol());
        super.channelInactive(ctx);
    }

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, RewriteableMsg msg) throws Exception {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();

        if (remoteAddress instanceof InetSocketAddress) {
            updateAddress(msg, ctx, (InetSocketAddress) remoteAddress);
            getComponent().deliverMessage(msg);
        }
	}
}
