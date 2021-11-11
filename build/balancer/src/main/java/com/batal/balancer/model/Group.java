package com.batal.balancer.model;

import com.batal.balancer.dto.v1.ActionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {
    private static final Logger log = LoggerFactory.getLogger(Group.class);
    private final Map<String, Instance> instances;

    public Group() {
        this.instances = new HashMap<>(); // TODO Atomic
    }

    public Map<String, Instance> getInstances() {
        return instances;
    }

    public void remoteDeadAndRecalc(LocalDateTime currentTime, Map<String, ActionConfig> map) {
        remoteDead(currentTime);
        recalc(map);
    }

    public void remoteDead(LocalDateTime currentTime) {
        Map<String, Instance> appInstances = getInstances();
//        log.info("check lives");
        List<Instance> dead = new ArrayList<>();
        for (Map.Entry<String, Instance> entry : appInstances.entrySet()) {
            Instance instance = entry.getValue();
            Duration duration = Duration.between(instance.getLastAccess(), currentTime);
            if (duration.getSeconds() > 10) { // TODO magic number
                instance.setActive(false);
                dead.add(instance);
            }
        }
        for (Instance instance : dead) {
//            log.info("> dead " + instance.getHostname());
            appInstances.remove(instance.getHostname());
        }
    }

    public void recalc(Map<String, ActionConfig> map) {
//        log.info("> recalcRate");
        for (Map.Entry<String, Instance> entry : instances.entrySet()) {
            if (entry.getValue().isActive()) {
                return;
            }
        }
        for (Map.Entry<String, Instance> entry : instances.entrySet()) {
            entry.getValue().setActive(true);
            entry.getValue().rebalance(map);
            return;
        }
    }
}
