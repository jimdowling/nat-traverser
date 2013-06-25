/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.net;

import java.net.InetSocketAddress;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.socket.DatagramChannel;

/**
 *
 * @author jdowling
 */
public class NettySession {

    /** The remote address. */
    private final InetSocketAddress remotePublicSocketAddress;
    /** The local address. */
    private final InetSocketAddress localSocketAddress;

    /**
     * 
     */
    private final ConnectionlessBootstrap bootstrap;
    /**
     *
     */
    private final DatagramChannel channel;

    public NettySession(InetSocketAddress remotePublicSocketAddress, InetSocketAddress localSocketAddress, ConnectionlessBootstrap bootstrap, DatagramChannel channel) {
        this.remotePublicSocketAddress = remotePublicSocketAddress;
        this.localSocketAddress = localSocketAddress;
        this.bootstrap = bootstrap;
        this.channel = channel;
    }


    
}
