package se.sics.gvod.net;

import java.util.concurrent.atomic.AtomicLong;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

public class NettyPipelineFactory implements ChannelPipelineFactory {

    private ChannelGroup channelGroup = null;
    private OrderedMemoryAwareThreadPoolExecutor pipelineExecutor = null;
//    final private SSLContext context;
//SSLContext context = CryptoUtils.initTlsContext(KEYSTORE_PATH, KS_PASS,
//TRUSTSTORE_PATH, TS_PASS, TLS_SESSION_CACHE_SIZE);
    private final NettyHandler handler;

    /**
     * Constructor
     * @param channelGroup
     * @param pipelineExecutor
     * @param answer
     * @param max max connection
     */
    public NettyPipelineFactory(NettyHandler handler, ChannelGroup channelGroup,
            OrderedMemoryAwareThreadPoolExecutor pipelineExecutor) {
        super();
        this.handler = handler;
        this.channelGroup = channelGroup;
        this.pipelineExecutor = pipelineExecutor;
    }

    /**
     * Initiate the Pipeline for the newly active connection with ObjectXxcoder.
     * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
     */
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

//              SSLEngine engine = context.createSSLEngine();
//		engine.setUseClientMode(false);
//		engine.setWantClientAuth(false);
//		engine.setEnableSessionCreation(true);

//		pipeline.addLast("TLS", new SslHandler(engine));
//		pipeline.addLast("Timeout", new Disconnector(new
//HashedWheelTimer(WORKER_THREAD_POOL.getThreadFactory()),
//				SESSION_TIMEOUT,SESSION_TIMEOUT,SESSION_TIMEOUT,
//TimeUnit.MILLISECONDS));

        //ZlibWrapper.GZIP
        // 6 is the default compression level, 9 is max compression
//        pipeline.addLast("deflater", new ZlibEncoder(ZlibWrapper.GZIP, 9));
//        pipeline.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB));

//              pipeline.addLast("Logging", new ChannelLogger()); //logs using our own API


//        ByteCounter byteCounter =
//                new ByteCounter("--- CLIENT-COUNTER :: ", totalReadBytes, totalWrittenBytes);
        MessageCounter messageCounter =
                new MessageCounter("--- CLIENT-MSGCOUNTER :: ");


//        pipeline.addFirst("byteCounter", byteCounter);
        pipeline.addLast("decoder", new VodFrameDecoder());
        pipeline.addLast("encoder", new VodFrameEncoder());
//        pipeline.addLast("decoder", new ObjectDecoder(NettyNetwork.MAX_OBJECT_SIZE));
//        pipeline.addLast("encoder", new ObjectEncoder(1650));
//        pipeline.addLast("msgCounter", messageCounter);


        // no pipelineExecutor is needed, as the NettyNetwork component
        // should not have to wait for I/O processing. It simply dispatches
        // the thread
//        pipeline.addLast("pipelineExecutor", new ExecutionHandler(
//                pipelineExecutor));

        pipeline.addLast("handler", handler);
        return pipeline;
    }
}
