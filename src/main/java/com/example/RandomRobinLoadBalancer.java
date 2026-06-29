package com.example;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Selects a managed {@link Resource} at random on each call.
 */
public class RandomRobinLoadBalancer implements LoadBalancer {
    private CopyOnWriteArrayList<Resource> resources = new CopyOnWriteArrayList<>();

    @Override
    public void add(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }
        resources.add(resource);
    }

    @Override
    public Resource getNextResource() {
        if (resources.isEmpty()) {
            return null;
        }

        int index = (int) (ThreadLocalRandom.current().nextDouble() * resources.size());
        return resources.get(index);
    }
}
