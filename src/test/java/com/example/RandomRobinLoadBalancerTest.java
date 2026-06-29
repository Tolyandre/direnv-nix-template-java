package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RandomRobinLoadBalancerTest {

    private final LoadBalancer balancer = new RandomRobinLoadBalancer();

    @Test
    void returnsNullWhenNoResourcesAdded() {
        assertNull(balancer.getNextResource());
    }

    @Test
    void alwaysReturnsTheSingleAddedResource() {
        Resource only = new Resource("http://a");
        balancer.add(only);

        for (int i = 0; i < 100; i++) {
            assertEquals(only, balancer.getNextResource());
        }
    }

    @Test
    void neverReturnsAResourceThatWasNotAdded() {
        List<Resource> added = List.of(
                new Resource("http://a"),
                new Resource("http://b"),
                new Resource("http://c"));
        added.forEach(balancer::add);

        for (int i = 0; i < 1000; i++) {
            Resource picked = balancer.getNextResource();
            assertNotNull(picked);
            assertTrue(added.contains(picked), () -> "unexpected resource: " + picked);
        }
    }

    @Test
    void eventuallyReturnsEveryAddedResource() {
        List<Resource> added = List.of(
                new Resource("http://a"),
                new Resource("http://b"),
                new Resource("http://c"));
        added.forEach(balancer::add);

        Set<Resource> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            seen.add(balancer.getNextResource());
        }

        // With random selection over 1000 draws, the chance of missing any of
        // the three resources is negligible (~(2/3)^1000), so this is stable.
        assertEquals(new HashSet<>(added), seen);
    }
}
