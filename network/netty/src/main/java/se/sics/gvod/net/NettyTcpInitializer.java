package se.sics.gvod.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class NettyTcpInitializer extends ChannelInitializer<SocketChannel> {
	private final NettyTcpHandler handler;

	private Class<? extends MsgFrameDecoder> msgDecoderClass;

    /**
     *
     * @param handler
     * @param msgDecoderClass
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
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("decoder", msgDecoderClass.newInstance());
		pipeline.addLast("encoder", new MsgFrameEncoder());
		pipeline.addLast("handler", handler);
	}
}
