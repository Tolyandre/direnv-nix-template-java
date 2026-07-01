package com.example.ratelimiter;

import org.junit.jupiter.api.Test;

/**
 * Stage 1 test suite — you write these.
 *
 * <p>TDD loop: write ONE failing test, run it (RED), make it pass with the
 * simplest code (GREEN), then refactor. Repeat. Do not write implementation
 * ahead of a test that demands it.
 *
 * <p>Suggested first tests (delete these TODOs as you go):
 *   - allows requests up to the limit within a single window
 *   - rejects the request that exceeds the limit
 *   - allows requests again once the window has elapsed (use ManualTimeSource#advance)
 *   - a partially-elapsed window does NOT reset the budget
 *   - constructor rejects invalid config (maxRequests <= 0, null window, ...)
 *
 * Later stages (new test classes / methods): per-client isolation, thread-safety
 * under concurrency, and sliding-window accuracy. See EXERCISE.md.
 */
class FixedWindowRateLimiterTest {

    // TODO: write your first failing test here.
    @Test
    void writeYourFirstTest() {
        // Example scaffolding to get you started — replace with a real assertion:
        //   ManualTimeSource time = new ManualTimeSource();
        //   RateLimiter limiter = new FixedWindowRateLimiter(3, Duration.ofSeconds(1), time);
        //   assertTrue(limiter.tryAcquire("client-1"));
    }
}
