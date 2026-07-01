package com.example.ratelimiter;

import java.time.Duration;

/**
 * STARTING POINT for Stage 1 (and beyond).
 *
 * <p>This is a deliberately empty stub: every method throws so your first test
 * fails for the right reason (RED). Drive the real implementation out with tests.
 *
 * <p>You are free to change this class however you like — add fields, change the
 * constructor, extract collaborators, or replace it entirely with a different
 * algorithm as you reach later stages.
 */
public final class FixedWindowRateLimiter implements RateLimiter {

    private final int maxRequests;
    private final Duration window;
    private final TimeSource timeSource;

    public FixedWindowRateLimiter(int maxRequests, Duration window, TimeSource timeSource) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.timeSource = timeSource;
    }

    @Override
    public boolean tryAcquire(String clientId) {
        throw new UnsupportedOperationException("TODO: implement Stage 1 — write a failing test first");
    }
}
