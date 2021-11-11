package com.batal.balancer.model;

import com.batal.balancer.dto.v1.ActionConfig;
import com.batal.balancer.dto.v1.BalancerData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;

public class Instance {
    private final String hostname;
    private final String appType; // TODO ???
    private LocalDateTime lastAccess;
    private boolean active;
    private BalancerData data;

    public Instance(String hostname, String appType) {
        this.hostname = hostname;
        this.appType = appType;
        this.lastAccess = now();
    }

    public String getHostname() {
        return hostname;
    }

    public LocalDateTime getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(LocalDateTime lastAccess) {
        this.lastAccess = lastAccess;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BalancerData getData() {
        return data;
    }

    public void setData(BalancerData data) {
        this.data = data;
    }

    public void rebalance(Map<String, ActionConfig> map) {
        int rate = isActive() ? 1 : 0; // TODO calc rate

        BalancerData data = new BalancerData();
        Map<String, Integer> rates = new HashMap<>();
        for (Map.Entry<String, ActionConfig> entry : map.entrySet()) {
            rates.put(entry.getKey(), entry.getValue().getRate() * rate);
        }
        data.setRates(rates);
        setData(data);
    }
}
