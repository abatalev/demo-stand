package com.batal.balancer.model;

import java.time.LocalDateTime;

public class Notify {
    private String appType;
    private String hostname;
    private LocalDateTime time;

    public Notify(String appType, String hostname) {
        this.appType = appType;
        this.hostname = hostname;
        this.time = LocalDateTime.now();
    }

    public String getAppType() {
        return appType;
    }

    public String getHostname() {
        return hostname;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
