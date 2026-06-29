package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Thread-safety contract for {@link RandomRobinLoadBalancer}.
 *
 * <p>
 * These tests must stay green for any correctly synchronized implementation
 * and have a high probability of going red for one that is not (e.g. a plain
 * {@code ArrayList}). They never assert on a specific interleaving, only on
 * invariants that must hold under <em>any</em> interleaving.
 */
class RandomRobinLoadBalancerConcurrencyTest {

    @Test
    @Timeout(30)
    void concurrentAddsRegisterEveryResourceWithoutCorruption() throws InterruptedException {
        LoadBalancer balancer = new RandomRobinLoadBalancer();

        int threads = 8;
        int perThread = 500;
        Set<Resource> expected = ConcurrentHashMap.newKeySet();

        // Every thread adds its own distinct slice of resources at the same time.
        runConcurrently(threads, threadId -> {
            for (int i = 0; i < perThread; i++) {
                Resource r = new Resource("http://node-" + threadId + "-" + i);
                expected.add(r);
                balancer.add(r);
            }
        });

        // Draw far more than the coupon-collector expectation so full coverage
        // is overwhelmingly likely (~1 - 1.5e-6 for this size). A lost update
        // shows up as a missing resource; a corrupted slot shows up as null.
        Set<Resource> seen = new HashSet<>();
        int draws = expected.size() * 30;
        for (int i = 0; i < draws; i++) {
            Resource picked = balancer.getNextResource();
            assertNotNull(picked, "getNextResource returned null after concurrent adds");
            assertTrue(expected.contains(picked), () -> "unexpected resource: " + picked);
            seen.add(picked);
        }

        assertEquals(expected, seen, "some concurrently-added resources were lost or never reachable");
    }

    @Test
    @Timeout(30)
    void concurrentReadsAlwaysReturnAValidResource() throws InterruptedException {
        LoadBalancer balancer = new RandomRobinLoadBalancer();
        Set<Resource> universe = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            Resource r = new Resource("http://node-" + i);
            universe.add(r);
            balancer.add(r);
        }

        int threads = 16;
        int perThread = 10_000;
        runConcurrently(threads, threadId -> {
            for (int i = 0; i < perThread; i++) {
                Resource picked = balancer.getNextResource();
                assertNotNull(picked, "getNextResource returned null under concurrent reads");
                assertTrue(universe.contains(picked), () -> "unexpected resource: " + picked);
            }
        });
    }

    @Test
    @Timeout(30)
    void readsStayValidWhileResourcesAreAddedConcurrently() throws InterruptedException {
        LoadBalancer balancer = new RandomRobinLoadBalancer();
        Set<Resource> universe = ConcurrentHashMap.newKeySet();

        // Seed so readers always have something to pick from the very start.
        for (int i = 0; i < 10; i++) {
            Resource r = new Resource("http://seed-" + i);
            universe.add(r);
            balancer.add(r);
        }

        int writerThreads = 8;
        int readerThreads = 8;
        int writesPerThread = 2_000;
        int readsPerThread = 5_000;

        List<ThrowingTask> tasks = new ArrayList<>();
        for (int w = 0; w < writerThreads; w++) {
            int writerId = w;
            tasks.add(threadId -> {
                for (int i = 0; i < writesPerThread; i++) {
                    Resource r = new Resource("http://w-" + writerId + "-" + i);
                    // Publish to the universe before the balancer so any reader
                    // that observes it via the balancer also finds it here.
                    universe.add(r);
                    balancer.add(r);
                }
            });
        }
        for (int r = 0; r < readerThreads; r++) {
            tasks.add(threadId -> {
                for (int i = 0; i < readsPerThread; i++) {
                    Resource picked = balancer.getNextResource();
                    assertNotNull(picked, "getNextResource returned null during concurrent add");
                    assertTrue(universe.contains(picked), () -> "unexpected resource: " + picked);
                }
            });
        }

        runConcurrently(tasks);
    }

    // --- concurrency harness ------------------------------------------------

    @FunctionalInterface
    private interface ThrowingTask {
        void run(int threadId) throws Throwable;
    }

    private static void runConcurrently(int threads, ThrowingTask task) throws InterruptedException {
        List<ThrowingTask> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(task);
        }
        runConcurrently(tasks);
    }

    /**
     * Runs every task on its own thread, releasing them all at the same instant.
     */
    private static void runConcurrently(List<ThrowingTask> tasks) throws InterruptedException {
        int n = tasks.size();
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        try {
            for (int i = 0; i < n; i++) {
                int threadId = i;
                ThrowingTask task = tasks.get(i);
                pool.execute(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        task.run(threadId);
                    } catch (Throwable th) {
                        failures.add(th);
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await(); // every thread parked at the gate
            start.countDown(); // fire simultaneously for maximum contention
            done.await(); // @Timeout on the test guards against a hang
        } finally {
            pool.shutdownNow();
        }

        if (!failures.isEmpty()) {
            AssertionError error = new AssertionError(failures.size() + " concurrent task(s) failed");
            failures.forEach(error::addSuppressed);
            throw error;
        }
    }
}
