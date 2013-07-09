package se.sics.gvod.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;

public class NettyTcpInitializer extends ChannelInitializer<DatagramChannel> {

	// final private SSLContext context;
	// SSLContext context = CryptoUtils.initTlsContext(KEYSTORE_PATH, KS_PASS,
	// TRUSTSTORE_PATH, TS_PASS, TLS_SESSION_CACHE_SIZE);
	private final NettyTcpHandler handler;

	private Class<? extends MsgFrameDecoder> msgDecoderClass;

	/**
	 * Constructor
	 * 
	 * @param channelGroup
	 * @param pipelineExecutor
	 * @param answer
	 * @param max
	 *            max connection
	 */
	public NettyTcpInitializer(NettyTcpHandler handler,
			Class<? extends MsgFrameDecoder> msgDecoderClass) {
		super();
		this.handler = handler;
		this.msgDecoderClass = msgDecoderClass;
	}

	/**
	 * Initiate the Pipeline for the newly active connection with ObjectXxcoder.
	 */
	@Override
	protected void initChannel(DatagramChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		// SSLEngine engine = context.createSSLEngine();
		// engine.setUseClientMode(false);
		// engine.setWantClientAuth(false);
		// engine.setEnableSessionCreation(true);

		// pipeline.addLast("TLS", new SslHandler(engine));
		// pipeline.addLast("Timeout", new Disconnector(new
		// HashedWheelTimer(WORKER_THREAD_POOL.getThreadFactory()),
		// SESSION_TIMEOUT,SESSION_TIMEOUT,SESSION_TIMEOUT,
		// TimeUnit.MILLISECONDS));

		// ZlibWrapper.GZIP
		// 6 is the default compression level, 9 is max compression
		// pipeline.addLast("deflater", new ZlibEncoder(ZlibWrapper.GZIP, 9));
		// pipeline.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB));

		// pipeline.addLast("Logging", new ChannelLogger()); //logs using our
		// own API

		// ByteCounter byteCounter =
		// new ByteCounter("--- CLIENT-COUNTER :: ", totalReadBytes,
		// totalWrittenBytes);
		// MessageCounter messageCounter = new MessageCounter("--- CLIENT-MSGCOUNTER :: ");

		// pipeline.addFirst("byteCounter", byteCounter);
		pipeline.addLast("decoder", msgDecoderClass.newInstance());
		pipeline.addLast("encoder", new MsgFrameEncoder());
		// pipeline.addLast("decoder", new ObjectDecoder(NettyNetwork.MAX_OBJECT_SIZE));
		// pipeline.addLast("encoder", new ObjectEncoder(1650));
		// pipeline.addLast("msgCounter", messageCounter);

		// no pipelineExecutor is needed, as the NettyNetwork component
		// should not have to wait for I/O processing. It simply dispatches
		// the thread
		// pipeline.addLast("pipelineExecutor", new ExecutionHandler(
		// pipelineExecutor));

		pipeline.addLast("handler", handler);
	}
}
