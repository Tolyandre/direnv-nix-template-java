package com.example.ratelimiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test-only {@link TimeSource} whose "now" only moves when you tell it to.
 *
 * <p>This lets you test window/expiry behaviour instantly and deterministically
 * instead of calling {@code Thread.sleep}. It is thread-safe so it can also be
 * shared by the concurrency tests.
 *
 * <p>(Provided as given infrastructure — in a real interview, deciding how to
 * make time testable would be part of what you design.)
 */
public final class ManualTimeSource implements TimeSource {

    private final AtomicLong nanos = new AtomicLong(0);

    @Override
    public long nanoTime() {
        return nanos.get();
    }

    /** Advance the clock by the given duration. */
    public void advance(Duration duration) {
        nanos.addAndGet(duration.toNanos());
    }

    /** Advance the clock by an explicit number of nanoseconds. */
    public void advanceNanos(long deltaNanos) {
        nanos.addAndGet(deltaNanos);
    }
}
