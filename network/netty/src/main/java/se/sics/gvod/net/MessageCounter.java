package se.sics.gvod.net;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:bruno@factor45.org">Bruno de Carvalho</a>
 * @author Steffen Grohsschmiedt
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

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.readMessages.incrementAndGet();
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msgs, ChannelPromise promise) throws Exception {
        promise.addListener(new GenericFutureListener<Future<? super Void>>() {

            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                MessageCounter.this.readMessages.getAndIncrement();
            }
        });
        super.write(ctx, msgs, promise);
    }

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
