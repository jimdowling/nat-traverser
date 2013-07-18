package se.sics.gvod.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

/**
 * @author Steffen Grohsschmiedt
 */
public class NettyInitializer<C extends Channel> extends ChannelInitializer<C> {
	private final NettyBaseHandler handler;

	private Class<? extends MsgFrameDecoder> msgDecoderClass;

    /**
     *
     * @param handler
     * @param msgDecoderClass
     */
	public NettyInitializer(NettyBaseHandler handler,
                            Class<? extends MsgFrameDecoder> msgDecoderClass) {
		super();
		this.handler = handler;
		this.msgDecoderClass = msgDecoderClass;
	}

	/**
	 * Initiate the Pipeline for the newly active connection with ObjectXxcoder.
	 */
	@Override
	protected void initChannel(C ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("decoder", msgDecoderClass.newInstance());
		pipeline.addLast("encoder", new MsgFrameEncoder());
		pipeline.addLast("handler", handler);
	}
}
