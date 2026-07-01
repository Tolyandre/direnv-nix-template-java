package com.example.ratelimiter;

import java.time.Duration;
import java.util.Objects;

/**
 * STARTING POINT for Stage 1 (and beyond).
 *
 * <p>
 * This is a deliberately empty stub: every method throws so your first test
 * fails for the right reason (RED). Drive the real implementation out with
 * tests.
 *
 * <p>
 * You are free to change this class however you like — add fields, change the
 * constructor, extract collaborators, or replace it entirely with a different
 * algorithm as you reach later stages.
 */
public final class FixedWindowRateLimiter implements RateLimiter {

    private final int maxRequests;
    private final long windowNanos;
    private final TimeSource timeSource;

    private int requestCount;
    private long windowStartTime;

    public FixedWindowRateLimiter(int maxRequests, Duration window, TimeSource timeSource) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be positive");
        }

        this.maxRequests = maxRequests;

        this.timeSource = Objects.requireNonNull(timeSource, "timeSource");
        Objects.requireNonNull(window, "window");
        this.windowNanos = window.toNanos();

        this.requestCount = 0;
        this.windowStartTime = timeSource.nanoTime();
    }

    @Override
    public boolean tryAcquire(String clientId) {
        long currentTime = timeSource.nanoTime();
        if (currentTime - windowStartTime >= windowNanos) {
            requestCount = 0;
            windowStartTime = currentTime;
        }

        if (requestCount < maxRequests) {
            requestCount++;
            return true;
        }
        return false;
    }
}
