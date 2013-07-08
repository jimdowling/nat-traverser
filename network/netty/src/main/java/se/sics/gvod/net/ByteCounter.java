package se.sics.gvod.net;

import io.netty.buffer.ByteBuf;
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
public class ByteCounter extends ChannelDuplexHandler {

	// internal vars ----------------------------------------------------------

	private final String id;
	private final AtomicLong totalReadBytes;
	private final AtomicLong totalWrittenBytes;

	// constructors -----------------------------------------------------------

	public ByteCounter(String id, AtomicLong totalReadBytes, AtomicLong totalWrittenBytes) {
		this.id = id;
		this.totalWrittenBytes = totalWrittenBytes;
		this.totalReadBytes = totalReadBytes;
	}

	// ChannelInboundHandlerAdapter -------------------------------------------

	// TODO Netty 4 unchecked code transformation
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageList<Object> msgs)
			throws Exception {
		for (ByteBuf m : msgs.<ByteBuf> cast()) {
			// TODO Capacity is probably bigger than the actual size
			this.totalReadBytes.addAndGet(m.capacity());
		}

		super.messageReceived(ctx, msgs);
	}

	// TODO Netty 4 unchecked code transformation
	@Override
	public void write(ChannelHandlerContext ctx, MessageList<Object> msgs, ChannelPromise promise)
			throws Exception {
		int size = 0;
		for (ByteBuf m : msgs.<ByteBuf> cast()) {
			// TODO Capacity is probably bigger than the actual size
			size += m.capacity();
		}
		
		final int finalSize = size;
		promise.addListener(new GenericFutureListener<Future<? super Void>>() {

			@Override
			public void operationComplete(Future<? super Void> future) throws Exception {
				ByteCounter.this.totalWrittenBytes.addAndGet(finalSize);
			}
		});
		
		super.write(ctx, msgs, promise);
	}

	// TODO Netty 4 unchecked code transformation
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		System.out.println(this.id + ctx.channel() + " -> sent: " + this.getWrittenBytes()
				+ "b, recv: " + this.getReadBytes() + "b");
	}

	// getters & setters ------------------------------------------------------

	public long getWrittenBytes() {
		return totalWrittenBytes.get();
	}

	public long getReadBytes() {
		return totalReadBytes.get();
	}
}
