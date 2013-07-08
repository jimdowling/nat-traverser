/**
 * This file is part of the Kompics component model runtime.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package se.sics.gvod.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.gvod.address.Address;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.Encodable;
import se.sics.gvod.common.msgs.MessageEncodingException;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.events.BandwidthStats;
import se.sics.gvod.net.events.NetworkException;
import se.sics.gvod.net.events.NetworkSessionClosed;
import se.sics.gvod.net.events.PortAllocRequest;
import se.sics.gvod.net.events.PortAllocResponse;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.net.events.PortDeleteRequest;
import se.sics.gvod.net.events.PortDeleteResponse;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Fault;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;

/**
 * The <code>NettyNetwork</code> class.
 * 
 * @author Jim Dowling <jdowling@sics.se>
 */
public final class NettyNetwork extends ComponentDefinition {

	// 1868836467 is the data packet size
	// public static final int MAX_OBJECT_SIZE = 1868836468;
	public static final int MAX_OBJECT_SIZE = 1048576; // default
	private static final int CHANNEL_POOLSIZE = 200;
	private static final int CONNECT_TIMEOUT_MS = 5000;
	private static final int RECV_BUFFER_SIZE = 65536;
	private static final int SEND_BUFFER_SIZE = 65536;
	private static final Logger logger = LoggerFactory.getLogger(NettyNetwork.class);
	/**
	 * The Network port.
	 */
	Negative<VodNetwork> net = negative(VodNetwork.class);
	/**
	 * The NetworkControl port.
	 */
	Negative<NatNetworkControl> netControl = negative(NatNetworkControl.class);
	Positive<Timer> timer = positive(Timer.class);
	private int maxPacketSize;
	private boolean bindAllNetworkIfs;
	private Class<? extends MsgFrameDecoder> msgDecoderClass;
	/**
	 * locally bound sockets
	 */
	private Set<InetSocketAddress> udpSockets = new HashSet<InetSocketAddress>();
	private Map<Integer, InetSocketAddress> portsToSockets = new HashMap<Integer, InetSocketAddress>();
	// private CopyOnWriteArraySet<InetSocketAddress> udpSockets = new
	// CopyOnWriteArraySet<InetSocketAddress>();
	// private ConcurrentMap<Integer, InetSocketAddress> portsToSockets = new
	// ConcurrentHashMap<Integer, InetSocketAddress>();
	/**
	 * locally bound sockets
	 */
	private Set<Bootstrap> bootstraps = new HashSet<Bootstrap>();
	private Map<InetSocketAddress, Bootstrap> socketsToBootstraps = new HashMap<InetSocketAddress, Bootstrap>();
	/**
	 * locally bound sockets
	 */
	private Set<DatagramChannel> channels = new HashSet<DatagramChannel>();
	private Map<InetSocketAddress, DatagramChannel> socketsToChannels = new HashMap<InetSocketAddress, DatagramChannel>();
	/**
	 * Locally bound socket
	 */
	private InetAddress localInetAddress;
	private InetAddress upnpIp;
	private int upnpPort;
	private Random rand;
	private NettyNetwork component;
	private ConcurrentMap<InetSocketAddress, InetSocketAddress> upnpLocalSocket = new ConcurrentHashMap<InetSocketAddress, InetSocketAddress>();
	// Bandwidth Measurement statistics
	private boolean enableBandwidthStats;
	private long prevTotalWrote;
	private long prevTotalRead;
	private static long totalWrittenBytes, totalReadBytes;
	private static AtomicLong lastSecRead = new AtomicLong();
	private static AtomicLong lastSecWrote = new AtomicLong();
	// 60 samples stored
	private static LinkedList<Integer> lastMinWrote = new LinkedList<Integer>();
	private static LinkedList<Integer> lastHourWrote = new LinkedList<Integer>();
	// 60 mins stored
	private static LinkedList<Integer> lastMinRead = new LinkedList<Integer>();
	// 12 hours stored
	private static LinkedList<Integer> lastHourRead = new LinkedList<Integer>();
	private int bwSampleCounter = 0;

	private class ByteCounterTimeout extends Timeout {

		public ByteCounterTimeout(SchedulePeriodicTimeout spt) {
			super(spt);
		}
	}

//	public class MyObjectSizeEstimator extends DefaultObjectSizeEstimator {
//
//		@Override
//		public int estimateSize(Object o) {
//			if (o instanceof RewriteableMsg) {
//				return ((RewriteableMsg) o).getSize() + 8;
//			}
//			return super.estimateSize(o);
//		}
//	}

	/**
	 * Instantiates a new Netty network component.
	 */
	public NettyNetwork() {
		this.component = this;
		// IPv4 addresses over IPv6 addresses when querying DNS
		// For java, Linux is still a dual IPv6/v4 address stack, while windows
		// uses
		// a single address.
		System.setProperty("java.net.preferIPv4Stack", "true");

		subscribe(handleRewriteableMessage, net);
		subscribe(handlePortBindRequest, netControl);
		subscribe(handlePortAllocRequest, netControl);
		subscribe(handlePortDeleteRequest, netControl);
		subscribe(handleByteCounterTimeout, timer);
		subscribe(handleInit, control);
		subscribe(handleStop, control);
	}

	/**
	 * The handle init.
	 */
	Handler<ByteCounterTimeout> handleByteCounterTimeout = new Handler<ByteCounterTimeout>() {
		@Override
		public void handle(ByteCounterTimeout event) {

			lastSecWrote.set(totalWrittenBytes - prevTotalWrote);
			lastSecRead.set(totalReadBytes - prevTotalRead);
			prevTotalWrote = totalWrittenBytes;
			prevTotalRead = totalReadBytes;

			if (lastSecWrote.longValue() > VodConfig.getMaxUploadBwCapacity()) {
				VodConfig.setMaxUploadBwCapacity(lastSecWrote.longValue());
			}

			if (lastMinRead.size() == 60) {
				lastMinRead.removeLast();
			}
			lastMinRead.addFirst((int) lastSecRead.longValue());
			if (lastMinWrote.size() == 60) {
				lastMinWrote.removeLast();
			}
			lastMinWrote.addFirst((int) lastSecWrote.longValue());
			if (bwSampleCounter == 60) {
				if (lastHourRead.size() == 24) {
					lastHourRead.removeLast();
				}
				lastHourRead.addFirst(getLast(lastMinRead));
				if (lastHourWrote.size() == 24) {
					lastHourWrote.removeLast();
				}
				lastHourWrote.addFirst(getLast(lastMinWrote));
				bwSampleCounter = 0;
			} else {
				bwSampleCounter++;
			}

			trigger(new BandwidthStats((int) lastSecRead.longValue(),
					(int) lastSecWrote.longValue()), netControl);
		}
	};

	private int getLast(LinkedList<Integer> last) {
		int count = 0;
		for (int i = 0; i < last.size(); i++) {
			count += last.get(i);
		}
		return count;
	}

	/**
	 * The handle init.
	 */
	Handler<Stop> handleStop = new Handler<Stop>() {
		@Override
		public void handle(Stop event) {
			for (Bootstrap bootstrap : bootstraps) {
				if (bootstrap != null) {
					bootstrap.group().shutdownGracefully();
				}
			}
		}
	};
	Handler<NettyInit> handleInit = new Handler<NettyInit>() {
		@Override
		public void handle(NettyInit init) {
			rand = new Random(init.getSeed());
			maxPacketSize = init.getMTU();
			bindAllNetworkIfs = init.isBindToAllNetInterfaces();
			if (maxPacketSize <= 0) {
				throw new IllegalArgumentException(
						"Netty problem: max Packet Size must be set to greater than zero.");
			}

			msgDecoderClass = init.getMsgDecoderClass();
			DirectMsgNettyFactory.setMsgFrameDecoder(msgDecoderClass);

			enableBandwidthStats = init.isEnableBandwidthStats();

			localInetAddress = init.getSelf().getIp();
			upnpIp = init.getUpnp().getIp();
			upnpPort = init.getUpnp().getPort();

			bindPort(localInetAddress, init.getSelf().getPort(), upnpIp, upnpPort);

			// upnpLocalSocket.put(new InetSocketAddress(upnpIp, upnpPort),
			// new InetSocketAddress(localInetAddress,
			// init.getSelf().getPort()));

			Address alt = init.getAlt();
			if (alt != null) {
				bindPort(alt.getIp(), alt.getPort(), upnpIp, alt.getPort());
			}

			if (enableBandwidthStats) {
				SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(0, 1000);
				ByteCounterTimeout bct = new ByteCounterTimeout(spt);
				trigger(spt, timer);
			}
		}
	};
	/**
	 * The handle message.
	 */
	Handler<RewriteableMsg> handleRewriteableMessage = new Handler<RewriteableMsg>() {
		@Override
		public void handle(RewriteableMsg message) {

			if (message.getDestination().getIp().equals(message.getSource().getIp())
					&& message.getDestination().getPort() == message.getSource().getPort()) {
				// deliver locally
				trigger(message, net);
				return;
			}

			send(message);
		}
	};

	private InetSocketAddress address2SocketAddress(Address address) {
		return new InetSocketAddress(address.getIp(), address.getPort());
	}

	private long ip2long(InetAddress address) {
		return new BigInteger(address.getAddress()).longValue();
	}

	Handler<PortBindRequest> handlePortBindRequest = new Handler<PortBindRequest>() {
		@Override
		public void handle(PortBindRequest message) {

			logger.debug("Received bind request for port : " + message.getPort());
			PortBindResponse response = message.getResponse();

			// TODO - no upnp support here
			try {
				if (bindPort(localInetAddress, message.getPort(), localInetAddress,
						message.getPort())) {
					response.setStatus(PortBindResponse.Status.SUCCESS);
				} else {
					response.setStatus(PortBindResponse.Status.FAIL);
				}
			} catch (ChannelException e) {
				response.setStatus(PortBindResponse.Status.PORT_ALREADY_BOUND);
			}
			trigger(response, netControl);
		}
	};
	Handler<PortAllocRequest> handlePortAllocRequest = new Handler<PortAllocRequest>() {
		@Override
		public void handle(PortAllocRequest message) {
			int numPorts = message.getNumPorts();

			logger.debug("Request to allocate " + numPorts + " ports.");

			Set<Integer> setPorts = portsToSockets.keySet();
			Set<Integer> addedPorts = new HashSet<Integer>();

			for (int i = 0; i < numPorts; i++) {
				int randPort = -1;
				do {
					// Allocate a port in the 50,000+ range.
					randPort = 50000 + rand.nextInt(65535 - 50000);
				} while (setPorts.contains(randPort));
				// TODO - no upnp support here
				if (bindPort(localInetAddress, randPort, localInetAddress, randPort) == true) {
					addedPorts.add(randPort);
				}
			}

			PortAllocResponse response = message.getResponse();
			if (response == null) {
				throw new IllegalStateException(
						"PortAllocResponse event was not set before sending PortAllocRequest to Netty.");
			}
			response.setAllocatedPorts(addedPorts);
			trigger(response, netControl);
		}
	};
	Handler<PortDeleteRequest> handlePortDeleteRequest = new Handler<PortDeleteRequest>() {
		@Override
		public void handle(PortDeleteRequest message) {
			Set<Integer> p = message.getPortsToDelete();
			Set<Integer> setPorts = portsToSockets.keySet();

			Set<InetSocketAddress> socketsToRemove = new HashSet<InetSocketAddress>();

			Set<Integer> portsDeleted = new HashSet<Integer>();
			for (int i : p) {
				if (setPorts.contains(i)) {
					socketsToRemove.add(portsToSockets.get(i));
					portsDeleted.add(i);
				}
			}
			for (InetSocketAddress toRemove : socketsToRemove) {
				removeLocalSocket(toRemove);
			}
			if (message.getResponse() != null) { // if a response is requested,
													// send it
				PortDeleteResponse response = message.getResponse();
				response.setPorts(portsDeleted);
				trigger(response, netControl);
			}
		}
	};

	// TODO Netty 4 unchecked code transformation
	private boolean bindPort(InetAddress addr, int port, InetAddress upnpIp, int upnpPort) {

		if (portsToSockets.containsKey(port)) {
			return true;
		}

		// Configure the pipeline factory.
		NettyHandler handler = new NettyHandler(component, addr, port);

		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioDatagramChannel.class)
				.handler(new NettyClientInitializer(handler, msgDecoderClass));

		// Allow packets as large as up to 1600 bytes (default is 768).
		// You could increase or decrease this value to avoid truncated packets
		// or to improve memory footprint respectively.
		//
		// Please also note that a large UDP packet might be truncated or
		// dropped by your router no matter how you configured this option.
		// In UDP, a packet is truncated or dropped if it is larger than a
		// certain size, depending on router configuration. IPv4 routers
		// truncate and IPv6 routers drop a large packet. That's why it is
		// safe to send small packets in UDP.

		// use 'receiveBufferSizePredictor' when you want to change the
		// predictor
		// in the middle of a connection
		bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1500));
		// see
		// http://docs.jboss.org/netty/3.1/api/org/jboss/netty/channel/ChannelConfig.html

		// see
		// http://docs.jboss.org/netty/3.2/api/org/jboss/netty/channel/socket/DatagramChannelConfig.html
		bootstrap.option(ChannelOption.SO_RCVBUF, RECV_BUFFER_SIZE);
		bootstrap.option(ChannelOption.SO_SNDBUF, SEND_BUFFER_SIZE);
		// bootstrap.setOption("trafficClass", trafficClass);
		// bootstrap.setOption("soTimeout", soTimeout);
		// bootstrap.setOption("broadcast", broadcast);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS);
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);

		try {
			// TODO - bind on all network interfaces ??
			DatagramChannel c;
			if (bindAllNetworkIfs) {
				c = (DatagramChannel) bootstrap.bind(new InetSocketAddress(port)).sync().channel();
			} else {
				c = (DatagramChannel) bootstrap.bind(new InetSocketAddress(addr, port)).sync()
						.channel();
			}

			addLocalSocket(c, new InetSocketAddress(addr, port));

			upnpLocalSocket.put(new InetSocketAddress(upnpIp, upnpPort), new InetSocketAddress(
					addr, port));

			logger.info("Successfully bound to ip:port {}:{}", addr, port);
		} catch (InterruptedException e) {
			logger.warn("Problem when trying to bind to {}:{}", addr.getHostAddress(), port);
			trigger(new Fault(e.getCause()), control);
			return false;
		}

		bootstraps.add(bootstrap);
		socketsToBootstraps.put(new InetSocketAddress(addr, port), bootstrap);

		return true;
	}

	private void addLocalSocket(DatagramChannel channel, InetSocketAddress addr) {
		udpSockets.add(addr);
		portsToSockets.put(addr.getPort(), addr);
		channels.add(channel);
		socketsToChannels.put(addr, channel);
	}

	private boolean removeLocalSocket(InetSocketAddress addr) {
		boolean res;

		if (udpSockets.remove(addr) == false) {
			res = false;
		} else {
			int port = addr.getPort();
			portsToSockets.remove(port);
			DatagramChannel c = socketsToChannels.remove(addr);
			res = (c == null) ? false : channels.remove(c);

		// TODO find the according functions in netty 4
//			if (c != null) {
//				if (c.isBound()) {
//					c.unbind();
//				}
//			}
		}
		return res;
	}

	// TODO Netty 4 unchecked code transformation
	private void send(RewriteableMsg message) {
		
		InetSocketAddress upnpSocket = address2SocketAddress(message.getSource());
		InetSocketAddress src = upnpLocalSocket.get(upnpSocket);
		InetSocketAddress dest = address2SocketAddress(message.getDestination());

		if (src == null) {
			String strError = "Source for msg " + "of type " + message.getClass()
					+ " is not bound at network component: " + message.getSource();
			logger.error(strError);
			trigger(new Fault(new IllegalArgumentException(strError)), control);
			return;
		}

		// use one channel per local socket
		// session-less UDP. This means that remoteAddresses cannot be found in
		// the channel object, but only in the MessageEvent object.
		DatagramChannel channel = socketsToChannels.get(src);
		if (channel == null) {
			// Note: assumption here that UPnP port is the same as local port.
			if (bindPort(localInetAddress, message.getSource().getPort(), localInetAddress, message
					.getSource().getPort()) == false) {
				logger.warn("Channel was null when trying to write message of type: "
						+ message.getClass().getCanonicalName() + " with src address: "
						+ src.toString());
				trigger(new Fault(new IllegalStateException(
						"Port not bound at client, cannot send message. "
								+ "Need to first allocate port: " + src.getPort())), control);
				return;
			}
			channel = socketsToChannels.get(src);
		}
		try {
			logger.trace("Sending " + message.getClass().getCanonicalName() + " from {} to {} ",
					message.getSource().getId(), message.getDestination().getId());
			// TODO Solve the encodable problem
			channel.write(new DatagramPacket(((Encodable) message).toByteArray(), dest));
			totalWrittenBytes += message.getSize();
		} catch (NullPointerException ex) {
			logger.warn("Problem trying to write message of type: "
					+ message.getClass().getCanonicalName() + " with src address: "
					+ src.toString());
			trigger(new Fault(ex), control);
		} catch (MessageEncodingException ex) {
			logger.warn("Problem trying to write message of type: "
					+ message.getClass().getCanonicalName() + " with src address: "
					+ src.toString());
			trigger(new Fault(ex), control);
		}
	}

	final void deliverMessage(RewriteableMsg message, DatagramChannel session) {

		logger.trace("Receiving " + message.getClass().getCanonicalName() + " source {} dest {} ",
				message.getSource().getId(), message.getDestination().getId());
		// UDP is session-less
		trigger(message, net);
		totalReadBytes += message.getSize();
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
		logger.warn("Fault for " + e.getCause().getMessage());
		trigger(new Fault(e.getCause()), control);
	}

	/**
	 * Network exception.
	 * 
	 * @param event
	 *            the event
	 */
	final void networkException(NetworkException event) {
		logger.trace("NetworkException for " + event.getRemoteAddress().toString());
		trigger(event, netControl);
	}

	final void channelUnregistered(ChannelHandlerContext ctx) {

		Channel c = ctx.channel();
		SocketAddress localAddr = c.localAddress();
		InetSocketAddress clientSocketAddress = null;
		if (localAddr instanceof InetSocketAddress) {
			clientSocketAddress = (InetSocketAddress) localAddr;
			removeLocalSocket(clientSocketAddress);
			logger.trace("Channel closed");
		}

		trigger(new NetworkSessionClosed(clientSocketAddress, Transport.UDP), netControl);
	}

	public InetAddress getLocalInetAddress() {
		return localInetAddress;
	}

	public static long getNumBytesReadLastSec() {
		return lastSecRead.longValue();
	}

	public static long getNumBytesReadLastMin() {

		return lastSecRead.longValue();
	}

	public static long getNumBytesWroteLastSec() {
		return lastSecWrote.longValue();
	}
}
