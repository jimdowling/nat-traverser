package se.sics.gvod.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class UtilThreadFactory implements ThreadFactory {

	private static final AtomicInteger counter = new AtomicInteger();

	private final String name;

	public UtilThreadFactory(final String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(final Runnable runnable) {
		return new Thread(runnable, name + '-' + counter.getAndIncrement());
	}
}
