package com.batal.configurer.model;

public class Action {
    private String id;
    private String type;
    private Object config;
    private String balanceMethod = "none";
    private int rate = -1;

    public Action(String id, String type, Object config) {
        this.id = id;
        this.type = type;
        this.config = config;
    }

    public Action(String id, String type, String method, int rate, Object config) {
        this.id = id;
        this.type = type;
        this.balanceMethod = method;
        this.rate = rate;
        this.config = config;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Object getConfig() {
        return config;
    }

    public String getBalanceMethod() {
        return balanceMethod;
    }

    public int getRate() {
        return rate;
    }
}
