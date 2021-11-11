package com.batal.balancer.dto.v1;

public class ActionConfig {
    private String id;
    private String method;
    private int rate;

    public void setId(String id) {
        this.id = id;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public int getRate() {
        return rate;
    }
}
