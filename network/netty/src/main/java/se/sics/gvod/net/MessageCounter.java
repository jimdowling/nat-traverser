package se.sics.gvod.net;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.MessageList;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:bruno@factor45.org">Bruno de Carvalho</a>
 */
public class MessageCounter extends ChannelDuplexHandler {

	// internal vars ----------------------------------------------------------

	private final String id;
	private final AtomicLong writtenMessages;
	private final AtomicLong readMessages;

	// constructors -----------------------------------------------------------

	public MessageCounter(String id) {
		this.id = id;
		this.writtenMessages = new AtomicLong();
		this.readMessages = new AtomicLong();
	}

	// SimpleChannelHandler ---------------------------------------------------

	// TODO Netty 4 unchecked code transformation
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageList<Object> msgs)
			throws Exception {
		this.readMessages.addAndGet(msgs.size());
		super.messageReceived(ctx, msgs);
	}

	// TODO Netty 4 unchecked code transformation
	@Override
	public void write(ChannelHandlerContext ctx, MessageList<Object> msgs, ChannelPromise promise)
			throws Exception {
		final int size = msgs.size();
		promise.addListener(new GenericFutureListener<Future<? super Void>>() {

			@Override
			public void operationComplete(Future<? super Void> future) throws Exception {
				MessageCounter.this.readMessages.addAndGet(size);
			}
		});

		super.write(ctx, msgs, promise);
	}

	// TODO Netty 4 unchecked code transformation
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println(this.id + ctx.channel() + " -> sent: " + this.getWrittenMessages()
				+ ", recv: " + this.getReadMessages());
		super.channelUnregistered(ctx);
	}

	// getters & setters ------------------------------------------------------

	public long getWrittenMessages() {
		return writtenMessages.get();
	}

	public long getReadMessages() {
		return readMessages.get();
	}
}
