package org.ulpgc.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastContext {
    private static final HazelcastInstance instance = Hazelcast.newHazelcastInstance();

    public static HazelcastInstance getInstance() {
        return instance;
    }
}
