package com.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Selects a managed {@link Resource} at random on each call.
 */
public class RandomRobinLoadBalancer implements LoadBalancer {
    private List<Resource> resources = new ArrayList<>();

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

        int index = (int) (Math.random() * resources.size());
        return resources.get(index);
    }
}
