package se.sics.gvod.net;

import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:bruno@factor45.org">Bruno de Carvalho</a>
 */
public class ByteCounter extends SimpleChannelUpstreamHandler {

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

    // SimpleChannelUpstreamHandler -------------------------------------------

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        if (e.getMessage() instanceof ChannelBuffer) {
            this.totalReadBytes.addAndGet(((ChannelBuffer) e.getMessage())
                    .readableBytes());
        }

        super.messageReceived(ctx, e);
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e)
            throws Exception {
        super.writeComplete(ctx, e);
        this.totalWrittenBytes.addAndGet(e.getWrittenAmount());
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        super.channelClosed(ctx, e);
        System.out.println(this.id + ctx.getChannel() + " -> sent: " +
                           this.getWrittenBytes() + "b, recv: " +
                           this.getReadBytes() + "b");
    }

    // getters & setters ------------------------------------------------------

    public long getWrittenBytes() {
        return totalWrittenBytes.get();
    }

    public long getReadBytes() {
        return totalReadBytes.get();
    }
}
