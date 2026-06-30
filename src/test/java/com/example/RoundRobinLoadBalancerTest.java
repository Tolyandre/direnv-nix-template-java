package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoundRobinLoadBalancerTest {
    private RoundRobinLoadBalancer loadBalancer;

    @BeforeEach
    void setUp() {
        loadBalancer = new RoundRobinLoadBalancer();
    }

    @Test
    void testGetNextResourceWhenEmpty() {
        assertNull(loadBalancer.getNextResource(), "Should return null when no resources are added");
    }

    @Test
    void testGetNextResourceWithSingleResource() {
        Resource resource = new Resource("http://server1.com");
        loadBalancer.add(resource);

        assertEquals(resource, loadBalancer.getNextResource(), "Should return the single resource");
        assertEquals(resource, loadBalancer.getNextResource(), "Should return the same resource again");
        assertEquals(resource, loadBalancer.getNextResource(), "Should continue returning the same resource");
    }

    @Test
    void testGetNextResourceWithMultipleResources() {
        Resource resource1 = new Resource("http://server1.com");
        Resource resource2 = new Resource("http://server2.com");
        Resource resource3 = new Resource("http://server3.com");

        loadBalancer.add(resource1);
        loadBalancer.add(resource2);
        loadBalancer.add(resource3);

        assertEquals(resource1, loadBalancer.getNextResource(), "First call should return resource1");
        assertEquals(resource2, loadBalancer.getNextResource(), "Second call should return resource2");
        assertEquals(resource3, loadBalancer.getNextResource(), "Third call should return resource3");
    }

    @Test
    void testRoundRobinCycling() {
        Resource resource1 = new Resource("http://server1.com");
        Resource resource2 = new Resource("http://server2.com");

        loadBalancer.add(resource1);
        loadBalancer.add(resource2);

        // First cycle
        assertEquals(resource1, loadBalancer.getNextResource());
        assertEquals(resource2, loadBalancer.getNextResource());

        // Second cycle - should start over
        assertEquals(resource1, loadBalancer.getNextResource(), "Should cycle back to resource1");
        assertEquals(resource2, loadBalancer.getNextResource(), "Should cycle back to resource2");

        // Third cycle
        assertEquals(resource1, loadBalancer.getNextResource(), "Should cycle back to resource1 again");
    }

    @Test
    void testAddNullResource() {
        assertThrows(NullPointerException.class, () -> {
            loadBalancer.add(null);
        }, "Adding null resource should throw NullPointerException");
    }
}
