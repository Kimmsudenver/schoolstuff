package com.hygenics.crawlerclient;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Custom thread factory to use with examples.
 */
public class CustomUtilThreadFactory implements ThreadFactory {

    private static final AtomicInteger counter = new AtomicInteger();

    private final String name;

    public CustomUtilThreadFactory(final String name) {
        this.name = name;
    }

    public Thread newThread(final Runnable runnable) {
        return new Thread(runnable, name + '-' + counter.getAndIncrement());
    }
}

