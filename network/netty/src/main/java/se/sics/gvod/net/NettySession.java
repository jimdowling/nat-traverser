/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package se.sics.gvod.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.DatagramChannel;

import java.net.InetSocketAddress;

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
	private final Bootstrap bootstrap;
	/**
     *
     */
	private final DatagramChannel channel;

	public NettySession(InetSocketAddress remotePublicSocketAddress,
			InetSocketAddress localSocketAddress, Bootstrap bootstrap, DatagramChannel channel) {
		this.remotePublicSocketAddress = remotePublicSocketAddress;
		this.localSocketAddress = localSocketAddress;
		this.bootstrap = bootstrap;
		this.channel = channel;
	}
}
