/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.events.NetworkException;
import se.sics.gvod.net.msgs.RewriteableMsg;

/**
 *
 * @author jdowling
 */
public class NettyHandler extends SimpleChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyHandler.class);
    private final NettyNetwork component;
    private final InetAddress addr;
    private final int port;

    public NettyHandler(NettyNetwork component, InetAddress addr, int port) {
        this.component = component;
        this.addr = addr;
        this.port = port;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        logger.trace("Channel connected");
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {

        // channel is connectionless
        Channel channel = ctx.getChannel();
        SocketAddress address = channel.getRemoteAddress();
        InetSocketAddress inetAddress = null;
        if (address != null && address instanceof InetSocketAddress) {
            inetAddress = (InetSocketAddress) address;
            component.networkException(new NetworkException(inetAddress, Transport.UDP));
        }

        Channel c = e.getChannel();
        component.exceptionCaught(ctx, e);

        StringBuilder sb = new StringBuilder();
        Object prob = ctx.getAttachment();
        if (prob != null) {
            sb.append(prob.getClass().getCanonicalName()).append(":");
        }
        sb.append(e.getCause().getMessage());

        logger.error(sb.toString());
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        component.channelClosed(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {


        RewriteableMsg rewrittenMsg = (RewriteableMsg) e.getMessage();

        // session-less UDP means that remoteAddresses cannot be found in
        // the channel object, but only in the MessageEvent object.
        SocketAddress srcAddr = e.getRemoteAddress();

        if (srcAddr instanceof InetSocketAddress) {
            InetSocketAddress is = (InetSocketAddress) srcAddr;
            
            rewrittenMsg.getSource().setIp(is.getAddress());
            rewrittenMsg.getSource().setPort(is.getPort());

            rewrittenMsg.getDestination().setIp(addr);
            rewrittenMsg.getDestination().setPort(port);
            
            //  TODO - this is terrible code. All we need to do is change
            // the VodAddress in VodMsg, not Address in RewriteableMsg
            rewrittenMsg.rewriteDestination(rewrittenMsg.getDestination());
            rewrittenMsg.rewritePublicSource(rewrittenMsg.getSource());
            
                // TODO - for UPNP, the port on which the data is sent from the NAT
                // may not be the same as the mapped port - see
                // https://tools.ietf.org/html/rfc4380.
                // In this case, we should check if it is Upnp, and if so, then
                // don't re-write the source address.


                //  UPNP Port-mapped + Symmetric NAT (different port out than in),
                //  but same IP
//                if (is.getAddress().equals(rewrittenMsg.getSource().getIp()) == false) {
//                    rewrittenMsg.rewritePublicSource(newSrc);
//                } else {
//                    logger.info("UPnP + Symmetric packet recvd from " + rewrittenMsg.getSource().getId());
//                }
//            } else {
////                logger.trace("No nat: public {} private {}", newSrc.getId(), src.getId());
//            }


            Channel c = e.getChannel();
            if (c instanceof DatagramChannel) {
                DatagramChannel channel = (DatagramChannel) c;
                component.deliverMessage(rewrittenMsg, channel);
            } else {
                logger.warn("Received a message over a non-DatagramChannel of type {}", c.getClass());
            }
        }
    }
}
