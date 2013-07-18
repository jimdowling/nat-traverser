package se.sics.gvod.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:bruno@factor45.org">Bruno de Carvalho</a>
 * @author Steffen Grohsschmiedt
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

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            this.totalReadBytes.addAndGet(((ByteBuf) msg).readableBytes());
        }

		super.channelRead(ctx, msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            final int size = ((ByteBuf) msg).readableBytes();

            promise.addListener(new GenericFutureListener<Future<? super Void>>() {

                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    ByteCounter.this.totalWrittenBytes.addAndGet(size);
                }
            });
        }
		super.write(ctx, msg, promise);
	}

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
