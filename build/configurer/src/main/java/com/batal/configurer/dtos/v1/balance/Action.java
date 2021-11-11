package com.batal.configurer.dtos.v1.balance;

public class Action {
    private String id;
    private String method;
    private int rate;

    public Action(String id, String method, int rate) {
        this.id = id;
        this.method = method;
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
