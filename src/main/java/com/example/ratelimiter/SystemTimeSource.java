package com.example.ratelimiter;

/**
 * Production {@link TimeSource} backed by {@link System#nanoTime()}.
 */
public final class SystemTimeSource implements TimeSource {

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
