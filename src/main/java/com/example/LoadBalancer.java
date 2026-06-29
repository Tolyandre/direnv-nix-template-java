package com.example;

public interface LoadBalancer {
    /**
     * Adds a resource to be managed by this load balancer.
     */
    void add(Resource resource);

    /**
     * Retrieves the next available Resource from the load balancer.
     *
     * @return The next available Resource, or null if none are available.
     */
    Resource getNextResource();
}
