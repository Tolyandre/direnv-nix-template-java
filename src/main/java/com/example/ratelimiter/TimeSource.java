package com.example.ratelimiter;

/**
 * Abstraction over the passage of time so that rate-limiting logic can be tested
 * deterministically without sleeping.
 *
 * <p>The value is expressed in nanoseconds from an arbitrary origin and is
 * <strong>monotonic</strong> (it never goes backwards), exactly like
 * {@link System#nanoTime()}. Do not use it as a wall-clock timestamp.
 */
@FunctionalInterface
public interface TimeSource {

    /**
     * @return the current time in nanoseconds from an arbitrary, monotonic origin
     */
    long nanoTime();
}
