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
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.Inet4Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.common.msgs.DirectMsgNettyFactory;
import se.sics.gvod.common.msgs.Encodable;
import se.sics.gvod.config.VodConfig;
import se.sics.gvod.net.events.*;
import se.sics.gvod.net.msgs.RewriteableMsg;
import se.sics.gvod.net.util.UtilThreadFactory;
import se.sics.gvod.timer.SchedulePeriodicTimeout;
import se.sics.gvod.timer.Timeout;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The
 * <code>NettyNetwork</code> class.
 *
 * @author Jim Dowling <jdowling@sics.se>
 * @author Steffen Grohsschmiedt
 */
public final class NettyNetwork extends ComponentDefinition {

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
    private Class<? extends MsgFrameDecoder> msgDecoderClass;
    /**
     * Locally bound socket
     */
    private Random rand;
    private NettyNetwork component;
    private Map<Integer, InetSocketAddress> udpPortsToSockets = new HashMap<Integer, InetSocketAddress>();
    private Map<InetSocketAddress, Bootstrap> udpSocketsToBootstraps = new HashMap<InetSocketAddress, Bootstrap>();
    private Map<InetSocketAddress, DatagramChannel> udpSocketsToChannels = new HashMap<InetSocketAddress, DatagramChannel>();
    private Map<Integer, InetSocketAddress> tcpPortsToSockets = new HashMap<Integer, InetSocketAddress>();
    private Map<InetSocketAddress, ServerBootstrap> tcpSocketsToServerBootstraps = new HashMap<InetSocketAddress, ServerBootstrap>();
    private Map<InetSocketAddress, Bootstrap> tcpSocketsToBootstraps = new HashMap<InetSocketAddress, Bootstrap>();
    private Map<InetSocketAddress, SocketChannel> tcpSocketsToChannels = new HashMap<InetSocketAddress, SocketChannel>();
    private Map<Integer, InetSocketAddress> udtPortsToSockets = new HashMap<Integer, InetSocketAddress>();
    private Map<InetSocketAddress, ServerBootstrap> udtSocketsToServerBootstraps = new HashMap<InetSocketAddress, ServerBootstrap>();
    private Map<InetSocketAddress, Bootstrap> udtSocketsToBootstraps = new HashMap<InetSocketAddress, Bootstrap>();
    private Map<InetSocketAddress, UdtChannel> udtSocketsToChannels = new HashMap<InetSocketAddress, UdtChannel>();
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

    /**
     * Instantiates a new Netty network component.
     */
    public NettyNetwork() {
        this.component = this;
        // IPv4 addresses over IPv6 addresses when querying DNS
        // For java, Linux is still a dual IPv6/v4 address stack, while windows
        // uses a single address.
        System.setProperty("java.net.preferIPv4Stack", "true");

        subscribe(handleRewriteableMessage, net);
        subscribe(handlePortBindRequest, netControl);
        subscribe(handlePortAllocRequest, netControl);
        subscribe(handlePortDeleteRequest, netControl);
        subscribe(handleCloseConnectionRequest, netControl);
        subscribe(handleByteCounterTimeout, timer);
        subscribe(handleInit, control);
        subscribe(handleStop, control);
    }
    /**
     * The handle init.
     */
    Handler<NettyInit> handleInit = new Handler<NettyInit>() {
        @Override
        public void handle(NettyInit init) {
            rand = new Random(init.getSeed());
            maxPacketSize = init.getMTU();
            if (maxPacketSize <= 0) {
                throw new IllegalArgumentException(
                        "Netty problem: max Packet Size must be set to greater than zero.");
            }

            msgDecoderClass = init.getMsgDecoderClass();
            DirectMsgNettyFactory.Base.setMsgFrameDecoder(msgDecoderClass);

            enableBandwidthStats = init.isEnableBandwidthStats();

            if (enableBandwidthStats) {
                SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(0, 1000);
                ByteCounterTimeout bct = new ByteCounterTimeout(spt);
                trigger(spt, timer);
            }
        }
    };
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
     * Close all connections.
     */
    Handler<Stop> handleStop = new Handler<Stop>() {
        @Override
        public void handle(Stop event) {
            for (InetSocketAddress address : udpSocketsToBootstraps.keySet()) {
                closeSocket(address, Transport.UDP);
            }

            for (InetSocketAddress address : tcpSocketsToServerBootstraps.keySet()) {
                closeServerSocket(address, Transport.TCP);
            }
            for (InetSocketAddress address : tcpSocketsToBootstraps.keySet()) {
                closeSocket(address, Transport.TCP);
            }

            for (InetSocketAddress address : udtSocketsToServerBootstraps.keySet()) {
                closeServerSocket(address, Transport.UDT);
            }
            for (InetSocketAddress address : udtSocketsToBootstraps.keySet()) {
                closeSocket(address, Transport.UDT);
            }
        }
    };
    /**
     * Send the received message over the network using the specified protocol.
     */
    Handler<RewriteableMsg> handleRewriteableMessage = new Handler<RewriteableMsg>() {
        @Override
        public void handle(RewriteableMsg msg) {

            if (msg.getDestination().getIp().equals(msg.getSource().getIp())
                    && msg.getDestination().getPort() == msg.getSource().getPort()) {
                // deliver locally
                trigger(msg, net);
                return;
            }

            if (!(msg instanceof Encodable)) {
                throw new Error("Netty can only serialize instances of Encodable. You need to "
                        + "make this class implement Encodable: " + msg.getClass());
            }

            Transport protocol = msg.getProtocol();
            if (protocol == Transport.UDP) {
                sendUdp(msg);
            } else if (protocol == Transport.TCP) {
                sendTcp(msg);
            } else if (protocol == Transport.UDT) {
                sendUdt(msg);
            } else {
                throw new Error("Unknown Transport type");
            }
        }
    };
    /**
     * Start listening as a server on the given port.
     */
    Handler<PortBindRequest> handlePortBindRequest = new Handler<PortBindRequest>() {
        @Override
        public void handle(PortBindRequest msg) {

            logger.debug("Received bind request for port : " + msg.getPort());
            PortBindResponse response = msg.getResponse();

            try {
                if (bindPort(msg.getIp(), msg.getPort(), msg.getTransport(),
                        msg.isBindAllNetworkIfs())) {
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
    /**
     * Used by HolePunchingClient to allocate a bunch of ports, where we don't
     * care what the ports actually are, just that they are free ports. It tries
     * to find ports in the 50,000+ range, as they are typically not used by as
     * many applications as other ports.
     *
     */
    Handler<PortAllocRequest> handlePortAllocRequest = new Handler<PortAllocRequest>() {
        @Override
        public void handle(PortAllocRequest msg) {
            int numPorts = msg.getNumPorts();

            logger.debug("Request to allocate " + numPorts + " ports for hole-punching.");

            Set<Integer> setPorts = udpPortsToSockets.keySet();
            Set<Integer> addedPorts = new HashSet<Integer>();

            for (int i = 0; i < numPorts; i++) {
                int randPort = -1;
                do {
                    // Allocate a port in the 50,000+ range.
                    randPort = 50000 + rand.nextInt(65535 - 50000);
                } while (setPorts.contains(randPort));
                if (bindPort(msg.getIp(), randPort, msg.getTransport(), 
                        true) == true) {
                    addedPorts.add(randPort);
                }
            }

            PortAllocResponse response = msg.getResponse();
            if (response == null) {
                throw new IllegalStateException("PortAllocResponse event was not set before "
                        + "sending PortAllocRequest to Netty.");
            }
            response.setAllocatedPorts(addedPorts);
            trigger(response, netControl);
        }
    };
    /**
     * Stop listening as server on the given ports.
     */
    Handler<PortDeleteRequest> handlePortDeleteRequest = new Handler<PortDeleteRequest>() {
        @Override
        public void handle(PortDeleteRequest msg) {
            Map<Integer, InetSocketAddress> portsToSockets;

            Transport protocol = msg.getTransport();
            if (protocol == Transport.UDP) {
                portsToSockets = tcpPortsToSockets;
            } else if (protocol == Transport.TCP) {
                portsToSockets = udpPortsToSockets;
            } else if (protocol == Transport.UDT) {
                portsToSockets = udtPortsToSockets;
            } else {
                throw new Error("Unknown Transport type");
            }

            Set<Integer> portsDeleted = new HashSet<Integer>();
            for (int i : msg.getPortsToDelete()) {
                InetSocketAddress address = portsToSockets.remove(i);
                if (address != null) {
                    closeServerSocket(address, msg.getTransport());
                    removeServerSocket(address, msg.getTransport());
                    portsDeleted.add(i);
                }
            }

            // TODO this is triggered before they might have been closed
            if (msg.getResponse() != null) {
                // if a response is requested, send it
                PortDeleteResponse response = msg.getResponse();
                response.setPorts(portsDeleted);
                trigger(response, netControl);
            }
        }
    };
    /**
     * Close the client socket connected to the given remote address.
     */
    Handler<CloseConnectionRequest> handleCloseConnectionRequest = new Handler<CloseConnectionRequest>() {
        @Override
        public void handle(CloseConnectionRequest msg) {
            if (msg.getTransport() == Transport.UDP) {
                throw new RuntimeException("Cannot close connection for connectionless UDP");
            }

            closeSocket(address2SocketAddress(msg.getRemoteAddress()), msg.getTransport(), msg.getResponse());
            removeSocket(address2SocketAddress(msg.getRemoteAddress()), msg.getTransport());
        }
    };

    /**
     * Start listening as a server at the given address with the given protocol.
     *
     * @param addr the address to listen at
     * @param port the port number to listen at
     * @param protocol the protocol to use
     * @return true if listening was started
     * @throws ChannelException in case binding failed
     */
    private boolean bindPort(InetAddress addr, int port, Transport protocol, 
            boolean bindAllNetworkIfs) {
        switch (protocol) {
            case TCP:
                return bindTcpPort(addr, port, bindAllNetworkIfs);
            case UDP:
                return bindUdpPort(addr, port, bindAllNetworkIfs);
            case UDT:
                return bindUdtPort(addr, port, bindAllNetworkIfs);
            default:
                throw new Error("Unknown Transport type");
        }
    }

    /**
     * Start listening as a server at the given address..
     *
     * @param addr the address to listen at
     * @param port the port number to listen at
     * @param bindAllNetworkIfs whether to bind on all network interfaces
     * @return true if listening was started
     * @throws ChannelException in case binding failed
     */
    private boolean bindUdpPort(final InetAddress addr, final int port, final boolean bindAllNetworkIfs) {

        if (udpPortsToSockets.containsKey(port)) {
            return true;
        }

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioDatagramChannel.class)
                .handler(new NettyMsgHandler(component, Transport.UDP, msgDecoderClass));

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
        bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1500));
        bootstrap.option(ChannelOption.SO_RCVBUF, RECV_BUFFER_SIZE);
        bootstrap.option(ChannelOption.SO_SNDBUF, SEND_BUFFER_SIZE);
        // bootstrap.setOption("trafficClass", trafficClass);
        // bootstrap.setOption("soTimeout", soTimeout);
        // bootstrap.setOption("broadcast", broadcast);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);

        try {
            DatagramChannel c;
            if (bindAllNetworkIfs) {
                c = (DatagramChannel) bootstrap.bind(
//                        new InetSocketAddress(port)
                        port
                        ).sync().channel();
            } else {
                c = (DatagramChannel) bootstrap.bind(
                        new InetSocketAddress(addr, port)).sync().channel();
            }

            addLocalSocket(new InetSocketAddress(addr, port), c);
            logger.debug("Successfully bound to ip:port {}:{}", addr, port);
        } catch (InterruptedException e) {
            logger.warn("Problem when trying to bind to {}:{}", addr.getHostAddress(), port);
            trigger(new Fault(e.getCause()), control);
            return false;
        }

        udpSocketsToBootstraps.put(new InetSocketAddress(addr, port), bootstrap);

        return true;
    }

    /**
     * Start listening as a server at the given address..
     *
     * @param addr the address to listen at
     * @param port the port number to listen at
     * @param bindAllNetworkIfs whether to bind on all network interfaces
     * @return true if listening was started
     * @throws ChannelException in case binding failed
     */
    private boolean bindTcpPort(InetAddress addr, int port, boolean bindAllNetworkIfs) {

        if (tcpPortsToSockets.containsKey(port)) {
            return true;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        NettyTcpServerHandler handler = new NettyTcpServerHandler(component);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler((new NettyInitializer<SocketChannel>(handler, msgDecoderClass)))
                .option(ChannelOption.SO_REUSEADDR, true);

        try {
            if (bindAllNetworkIfs) {
                bootstrap.bind(new InetSocketAddress(port)).sync();
            } else {
                bootstrap.bind(new InetSocketAddress(addr, port)).sync();
            }

            logger.debug("Successfully bound to ip:port {}:{}", addr, port);
        } catch (InterruptedException e) {
            logger.warn("Problem when trying to bind to {}:{}", addr.getHostAddress(), port);
            trigger(new Fault(e.getCause()), control);
            return false;
        }

        InetSocketAddress iAddr = new InetSocketAddress(addr, port);
        tcpPortsToSockets.put(port, iAddr);
        tcpSocketsToServerBootstraps.put(iAddr, bootstrap);

        return true;
    }

    /**
     * Start listening as a server at the given address..
     *
     * @param addr the address to listen at
     * @param port the port number to listen at
     * @param bindAllNetworkIfs whether to bind on all network interfaces
     * @return true if listening was started
     * @throws ChannelException in case binding failed
     */
    private boolean bindUdtPort(InetAddress addr, int port, boolean bindAllNetworkIfs) {

        if (udtPortsToSockets.containsKey(port)) {
            return true;
        }

        ThreadFactory bossFactory = new UtilThreadFactory("boss");
        ThreadFactory workerFactory = new UtilThreadFactory("worker");
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1, bossFactory,
                NioUdtProvider.BYTE_PROVIDER);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1, workerFactory,
                NioUdtProvider.BYTE_PROVIDER);
        NettyUdtServerHandler handler = new NettyUdtServerHandler(component);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channelFactory(NioUdtProvider.BYTE_ACCEPTOR)
                .childHandler(new NettyInitializer<UdtChannel>(handler, msgDecoderClass))
                .option(ChannelOption.SO_REUSEADDR, true);

        try {
            if (bindAllNetworkIfs) {
                bootstrap.bind(new InetSocketAddress(port)).sync();
            } else {
                bootstrap.bind(new InetSocketAddress(addr, port)).sync();
            }

            logger.debug("Successfully bound to ip:port {}:{}", addr, port);
        } catch (InterruptedException e) {
            logger.warn("Problem when trying to bind to {}:{}", addr.getHostAddress(), port);
            trigger(new Fault(e.getCause()), control);
            return false;
        }

        InetSocketAddress iAddr = new InetSocketAddress(addr, port);
        udtPortsToSockets.put(port, iAddr);
        udtSocketsToServerBootstraps.put(iAddr, bootstrap);

        return true;
    }

    /**
     * Connect to a TCP server.
     *
     * @param remoteAddress the remote address
     * @param localAddress the local address to bind to
     * @return true if connection succeeded
     * @throws ChannelException if connecting failed
     */
    private boolean connectTcp(Address remoteAddress, Address localAddress) {
        InetSocketAddress remote = address2SocketAddress(remoteAddress);
        InetSocketAddress local = address2SocketAddress(localAddress);

        if (tcpSocketsToBootstraps.containsKey(remote)) {
            return true;
        }

        EventLoopGroup group = new NioEventLoopGroup();
        NettyStreamHandler handler = new NettyStreamHandler(component, Transport.TCP);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new NettyInitializer<SocketChannel>(handler, msgDecoderClass))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .option(ChannelOption.SO_REUSEADDR, true);

        try {
            SocketChannel c = (SocketChannel) bootstrap.connect(remote, local).sync().channel();
            addLocalSocket(remote, c);
            logger.debug("Successfully connected to ip:port {}", remote.toString());
        } catch (InterruptedException e) {
            logger.warn("Problem when trying to connect to {}", remote);
            trigger(new Fault(e.getCause()), control);
            return false;
        }

        tcpSocketsToBootstraps.put(remote, bootstrap);
        return true;
    }

    /**
     * Connect to a UDT server.
     *
     * @param remoteAddress the remote address
     * @param localAddress the local address to bind to
     * @return true if connection succeeded
     * @throws ChannelException if connecting failed
     */
    private boolean connectUdt(Address remoteAddress, Address localAddress) {
        InetSocketAddress remote = address2SocketAddress(remoteAddress);
        InetSocketAddress local = address2SocketAddress(localAddress);

        if (udtSocketsToBootstraps.containsKey(remote)) {
            return true;
        }

        ThreadFactory workerFactory = new UtilThreadFactory("clientWorker");
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1, workerFactory,
                NioUdtProvider.BYTE_PROVIDER);
        NettyStreamHandler handler = new NettyStreamHandler(component, Transport.UDT);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channelFactory(NioUdtProvider.BYTE_CONNECTOR)
                .handler(new NettyInitializer<UdtChannel>(handler, msgDecoderClass))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .option(ChannelOption.SO_REUSEADDR, true);

        try {
            UdtChannel c = (UdtChannel) bootstrap.connect(remote, local).sync().channel();
            addLocalSocket(remote, c);
            logger.debug("Successfully connected to ip:port {}", remote.toString());
        } catch (InterruptedException e) {
            logger.warn("Problem when trying to connect to {}", remote.toString());
            trigger(new Fault(e.getCause()), control);
            return false;
        }

        udtSocketsToBootstraps.put(remote, bootstrap);
        return true;
    }

    /**
     * Add a {@link DatagramChannel} to the local connections.
     *
     * @param localAddress the local address
     * @param channel the channel to be added
     */
    private void addLocalSocket(InetSocketAddress localAddress, DatagramChannel channel) {
        udpPortsToSockets.put(localAddress.getPort(), localAddress);
        udpSocketsToChannels.put(localAddress, channel);
    }

    /**
     * Add a {@link SocketChannel} to the local connections.
     *
     * @param remoteAddress the remote address
     * @param channel the channel to be added
     */
    void addLocalSocket(InetSocketAddress remoteAddress, SocketChannel channel) {
        tcpSocketsToChannels.put(remoteAddress, channel);
        trigger(new NetworkSessionOpened(remoteAddress, Transport.TCP), netControl);
    }

    /**
     * Add a {@link UdtChannel} to the local connections.
     *
     * @param remoteAddress the remote address
     * @param channel the channel to be added
     */
    void addLocalSocket(InetSocketAddress remoteAddress, UdtChannel channel) {
        udtSocketsToChannels.put(remoteAddress, channel);
        trigger(new NetworkSessionOpened(remoteAddress, Transport.UDT), netControl);
    }

    private void removeServerSocket(InetSocketAddress addr, Transport protocol) {
        switch (protocol) {
            case TCP:
                tcpSocketsToServerBootstraps.remove(addr);
                break;
            case UDP:
                removeSocket(addr, Transport.UDP);
                break;
            case UDT:
                udtSocketsToServerBootstraps.remove(addr);
                break;
            default:
                throw new Error("Transport type not supported");
        }
    }

    private void closeServerSocket(InetSocketAddress addr, Transport protocol) {
        switch (protocol) {
            case TCP:
                closeSeverBootstrap(tcpSocketsToServerBootstraps.get(addr));
                break;
            case UDP:
                closeSocket(addr, Transport.UDP);
                break;
            case UDT:
                closeSeverBootstrap(udtSocketsToServerBootstraps.get(addr));
                break;
            default:
                throw new Error("Transport type not supported");
        }
    }

    private void closeSeverBootstrap(ServerBootstrap serverBootstrap) {
        serverBootstrap.childGroup().shutdownGracefully();
        serverBootstrap.group().shutdownGracefully();
    }

    /**
     * Remove a channel from the local connections and triggers the given response at the netControl port.
     *
     * @param addr the address of the channel to be removed
     * @param protocol the protocol of the channel to be removed
     */
    private void removeSocket(final InetSocketAddress addr, final Transport protocol) {
        switch (protocol) {
            case TCP:
                tcpSocketsToChannels.remove(addr);
                tcpSocketsToBootstraps.remove(addr);
                break;
            case UDP:
                udpPortsToSockets.remove(addr.getPort());
                udpSocketsToChannels.remove(addr);
                udpSocketsToBootstraps.remove(addr);
                break;
            case UDT:
                udtSocketsToChannels.remove(addr);
                udtSocketsToBootstraps.remove(addr);
                break;
            default:
                throw new Error("Transport type not supported");
        }
    }

    private void closeSocket(final InetSocketAddress addr, final Transport protocol) {
        closeSocket(addr, protocol, null);
    }

    private void closeSocket(final InetSocketAddress addr, final Transport protocol, final CloseConnectionResponse response) {
        Bootstrap bootstrap;
        switch (protocol) {
            case TCP:
                bootstrap = tcpSocketsToBootstraps.get(addr);
                break;
            case UDP:
                bootstrap = udpSocketsToBootstraps.get(addr);
                break;
            case UDT:
                bootstrap = udtSocketsToBootstraps.get(addr);
                break;
            default:
                throw new Error("Transport type not supported");
        }

        // Has been removed before
        if (bootstrap == null) {
            return;
        }

        Future future = bootstrap.group().shutdownGracefully();
        future.addListener(new GenericFutureListener<Future<?>>() {
            @Override
            public void operationComplete(Future<?> future) throws Exception {
                if (response != null) {
                    // if a response is requested, send it
                    trigger(response, netControl);
                }
            }
        });
    }

    private InetSocketAddress address2SocketAddress(Address address) {
        return new InetSocketAddress(address.getIp(), address.getPort());
    }

    /**
     * Send a message using UDP.
     *
     * @param msg the message to be sent
     * @throws ChannelException in case of connection problems
     */
    private void sendUdp(RewriteableMsg msg) {
        InetSocketAddress src = address2SocketAddress(msg.getSource());
        InetSocketAddress dest = address2SocketAddress(msg.getDestination());

        if (src == null) {
            String strError = "Source for msg " + "of type " + msg.getClass()
                    + " is not bound at network component: " + msg.getSource();
            logger.error(strError);
            trigger(new Fault(new IllegalArgumentException(strError)), control);
            return;
        }

        // use one channel per local socket
        // session-less UDP. This means that remoteAddresses cannot be found in
        // the channel object, but only in the MessageEvent object.
        DatagramChannel channel = udpSocketsToChannels.get(src);
        if (channel == null) {
            throw new IllegalStateException(
                    "Port not bound at client, cannot send message. "
                    + "Need to first allocate port: " + src.getPort());
        }
        try {
            logger.trace("Sending " + msg.getClass().getCanonicalName() + " from {} to {} ",
                    msg.getSource(), msg.getDestination());
            channel.writeAndFlush(new DatagramPacket(((Encodable) msg).toByteArray(), dest));
            totalWrittenBytes += msg.getSize();
        } catch (Exception ex) {
            logger.warn("Problem trying to write msg of type: "
                    + msg.getClass().getCanonicalName() + " with src address: "
                    + src.toString());
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Send a message to using TCP. Connects to a server on  demand.
     *
     * @param msg the message to be sent
     * @throws ChannelException in case of connection problems
     */
    private void sendTcp(RewriteableMsg msg) {
        InetSocketAddress dst = address2SocketAddress(msg.getDestination());
        SocketChannel channel = tcpSocketsToChannels.get(dst);

        if (channel == null) {
            if (connectTcp(msg.getDestination(), msg.getSource()) == false) {
                logger.warn("Channel was null when trying to write msg of type: "
                        + msg.getClass().getCanonicalName() + " with dst address: "
                        + dst.toString());
                trigger(new Fault(new IllegalStateException(
                        "Could not send message because connection could not be established to "
                        + dst.toString())), control);
                return;
            }
            channel = tcpSocketsToChannels.get(dst);
        }

        try {
            logger.trace("Sending " + msg.getClass().getCanonicalName() + " from {} to {} ",
                    msg.getSource().getId(), msg.getDestination().getId());
            channel.writeAndFlush(msg);
            totalWrittenBytes += msg.getSize();
        } catch (Exception ex) {
            logger.warn("Problem trying to write msg of type: "
                    + msg.getClass().getCanonicalName() + " with dst address: "
                    + dst.toString());
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Send a message to using UDT. Connects to a server on  demand.
     *
     * @param msg the message to be sent
     * @throws ChannelException in case of connection problems
     */
    private void sendUdt(RewriteableMsg msg) {
        InetSocketAddress dst = address2SocketAddress(msg.getDestination());
        UdtChannel channel = udtSocketsToChannels.get(dst);

        if (channel == null) {
            if (connectUdt(msg.getDestination(), msg.getSource()) == false) {
                logger.warn("Channel was null when trying to write msg of type: "
                        + msg.getClass().getCanonicalName() + " with dst address: "
                        + dst.toString());
                trigger(new Fault(new IllegalStateException(
                        "Could not send messge because connection could not be established to "
                        + dst.toString())), control);
                return;
            }
            channel = udtSocketsToChannels.get(dst);
        }

        try {
            logger.trace("Sending " + msg.getClass().getCanonicalName() + " from {} to {} ",
                    msg.getSource().getId(), msg.getDestination().getId());
            channel.writeAndFlush(msg);
            totalWrittenBytes += msg.getSize();
        } catch (Exception ex) {
            logger.warn("Problem trying to write msg of type: "
                    + msg.getClass().getCanonicalName() + " with dst address: "
                    + dst.toString());
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Deliver a message to the upper components.
     *
     * @param msg the message to be delivered
     */
    final void deliverMessage(RewriteableMsg msg) {
        logger.trace("Receiving " + msg.getClass().getCanonicalName() + " source {} dest {} ",
                msg.getSource().getId(), msg.getDestination().getId());
        trigger(msg, net);
        totalReadBytes += msg.getSize();
    }

    /**
     * Forward an exception to the upper components.
     *
     * @param ctx the channel handler context
     * @param e the caught exception
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.warn("Fault for " + e.getCause().getMessage());
        trigger(new Fault(e.getCause()), control);
    }

    /**
     * Network exception.
     *
     * @param event the event
     */
    final void networkException(NetworkException event) {
        logger.trace("NetworkException for " + event.getRemoteAddress().toString());
        trigger(event, netControl);
    }

    /**
     * Remove a connection after it was lost or closed remotely and inform the upper components.
     *
     * @param ctx the channel handler context
     * @param protocol the protocol
     */
    final void channelInactive(ChannelHandlerContext ctx, Transport protocol) {
        SocketAddress addr = ctx.channel().remoteAddress();;

        if (addr instanceof InetSocketAddress) {
            InetSocketAddress remoteAddress = (InetSocketAddress) addr;
            trigger(new NetworkSessionClosed(remoteAddress, protocol), netControl);
            // Schedule a thread secure close event for this component
            trigger(new CloseConnectionRequest(0, new Address(remoteAddress.getAddress(), remoteAddress.getPort(), 0), protocol), positive(NatNetworkControl.class));
            logger.trace("Channel closed");
        }
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
