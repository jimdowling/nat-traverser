package se.sics.gvod.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.Inet4Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.events.NetworkException;
import se.sics.gvod.net.msgs.RewriteableMsg;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Superclass providing the basic operations for our handlers. Subclasses should
 * handle received messages.
 *
 * @author Steffen Grohsschmiedt
 */
public abstract class NettyBaseHandler<I> extends SimpleChannelInboundHandler<I> {

    private static final Logger logger = LoggerFactory.getLogger(NettyBaseHandler.class);
    private final NettyNetwork component;
    private final Transport protocol;

    public NettyBaseHandler(NettyNetwork component, Transport protocol) {
        this.component = component;
        this.protocol = protocol;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.trace("Channel connected.");
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

    protected Transport getProtocol() {
        return protocol;
    }

    protected RewriteableMsg updateAddress(RewriteableMsg msg, ChannelHandlerContext ctx, InetSocketAddress remoteAddress)
            throws Exception {
        logger.trace("updating address of " + msg.getClass() + " src " + msg.getSource().getId()
                + " dest: " + msg.getDestination());
        if (remoteAddress.getAddress() instanceof Inet4Address == false) {
            throw new IllegalArgumentException("You are using ipv6 network addresses. "
                    + "They should be ipv4 network addresses");
        }
        InetAddress ip = (Inet4Address) remoteAddress.getAddress();

        if (ip == null) {
            throw new NullPointerException("NettyBaseHandler: IpAddress was NULL");
        }

        msg.getSource().setIp(ip);
        msg.getSource().setPort(remoteAddress.getPort());

        msg.getDestination().setIp(getAddress(ctx));
        msg.getDestination().setPort(getPort(ctx));

        logger.trace("updated address of " + msg.getClass() + " src " + msg.getSource() + " dest: "
                + msg.getDestination());

        // TODO - for UPNP, the port on which the data is sent from the NAT
        // may not be the same as the mapped port - see
        // https://tools.ietf.org/html/rfc4380.
        // In this case, we should check if it is Upnp, and if so, then
        // don't re-write the source address.

        msg.setProtocol(getProtocol());
        return msg;
    }

    // Should return an IPv4 address
    protected InetAddress getAddress(ChannelHandlerContext ctx) {
        Channel c = ctx.channel();
        SocketAddress s = c.localAddress();
        InetSocketAddress i = (InetSocketAddress) s;
        if (i.getAddress() instanceof Inet4Address == false) {
            throw new IllegalStateException("ipv6 addresses not supported - should be ipv4");
        }
        return (Inet4Address) i.getAddress();
    }

    protected int getPort(ChannelHandlerContext ctx) {
        return ((InetSocketAddress) ctx.channel().localAddress()).getPort();
    }

    protected NettyNetwork getComponent() {
        return component;
    }
}
