package com.example.ratelimiter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Stage 1 test suite — you write these.
 *
 * <p>
 * TDD loop: write ONE failing test, run it (RED), make it pass with the
 * simplest code (GREEN), then refactor. Repeat. Do not write implementation
 * ahead of a test that demands it.
 *
 * <p>
 * Suggested first tests (delete these TODOs as you go):
 * - allows requests up to the limit within a single window
 * - rejects the request that exceeds the limit
 * - allows requests again once the window has elapsed (use
 * ManualTimeSource#advance)
 * - a partially-elapsed window does NOT reset the budget
 * - constructor rejects invalid config (maxRequests <= 0, null window, ...)
 *
 * Later stages (new test classes / methods): per-client isolation,
 * thread-safety
 * under concurrency, and sliding-window accuracy. See EXERCISE.md.
 */
class FixedWindowRateLimiterTest {

    private ManualTimeSource time;
    private RateLimiter limiter;

    private static final int LIMIT = 3;

    @BeforeEach
    void setUp() {
        time = new ManualTimeSource();
        limiter = new FixedWindowRateLimiter(3, Duration.ofSeconds(1), time);
    }

    private void exhaustBudget(String client) {
        for (int i = 0; i < LIMIT; i++)
            limiter.tryAcquire(client);
    }

    @Test
    void allowRequestsWithinLimit() {

        // Act & Assert
        exhaustBudget("client-1");
    }

    @Test
    void rejectRequestExceedingLimit() {
        // Arrange
        exhaustBudget("client-1");

        // Act & Assert
        assertFalse(limiter.tryAcquire("client-1"));
    }

    @Test
    void allowRequestsAfterWindowElapsed() {
        // Arrange
        exhaustBudget("client-1");
        time.advance(Duration.ofSeconds(1));

        // Act & Assert
        assertTrue(limiter.tryAcquire("client-1"));
    }

    @Test
    void partiallyElapsedWindowDoesNotResetBudget() {
        // Arrange
        exhaustBudget("client-1");
        time.advance(Duration.ofMillis(500));

        // Act & Assert
        assertFalse(limiter.tryAcquire("client-1"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, -10 })
    void constructorRejectsInvalidMaxRequests(int maxRequests) {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new FixedWindowRateLimiter(maxRequests, Duration.ofSeconds(1), time));
    }

    @Test
    void constructorRejectsNullWindow() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> new FixedWindowRateLimiter(3, null, time));

    }

    @Test
    void constructorRejectsNullTimeSource() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> new FixedWindowRateLimiter(3, Duration.ofSeconds(1), null));
    }
}
