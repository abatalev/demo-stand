package com.batal.balancer.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupTest {

    @Test
    public void checkRemoteDeadAndRecalc() {
        LocalDateTime currentTime = now();
        Group group = new Group();
        Instance instance1 = addInstance(group, false, "instance1");
        instance1.setLastAccess(currentTime.minusMinutes(20));
        group.remoteDeadAndRecalc(currentTime, new HashMap<>());
        Assertions.assertEquals(0, group.getInstances().size());
    }

    @Test
    public void checkRemoteDead() {
        LocalDateTime currentTime = now();
        Group group = new Group();
        Instance instance1 = addInstance(group, false, "instance1");
        instance1.setLastAccess(currentTime.minusMinutes(20));
        group.remoteDead(currentTime);
        Assertions.assertEquals(0, group.getInstances().size());
    }

    @Test
    public void checkRecalc() {
        Group group = new Group();
        addInstance(group, true, "instance1");
        Instance instance2 = addInstance(group, false, "instance2");
        group.getInstances().remove("instance1");
        group.recalc(new HashMap<>());
        assertTrue(instance2.isActive());
    }

    private Instance addInstance(Group group, boolean active, String hostname) {
        Instance instance = new Instance(hostname, "actions");
        instance.setActive(active);
        instance.rebalance(new HashMap<>());
        group.getInstances().put(hostname, instance);
        return instance;
    }
}
