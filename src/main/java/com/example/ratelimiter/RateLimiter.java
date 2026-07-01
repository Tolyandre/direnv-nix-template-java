package com.example.ratelimiter;

/**
 * A rate limiter decides whether an incoming request for a given client should
 * be admitted or rejected, based on how many requests that client has already
 * made within a configured time window.
 *
 * <p>Implementations are expected to be safe for concurrent use by multiple
 * threads (see the exercise stages).
 */
public interface RateLimiter {

    /**
     * Attempts to admit a single request for {@code clientId}.
     *
     * @param clientId identifies the caller whose budget is consumed
     * @return {@code true} if the request is allowed (and one unit of budget was
     *         consumed), {@code false} if the request should be rejected because
     *         the client has exceeded its allowance for the current window
     */
    boolean tryAcquire(String clientId);
}
