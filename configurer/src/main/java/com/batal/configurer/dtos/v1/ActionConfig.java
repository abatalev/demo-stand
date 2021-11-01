package com.batal.configurer.dtos.v1;

public class ActionConfig {

    private String name;
    private String id;
    private Object config;

    public ActionConfig(String name, String id, Object config) {
        this.name = name;
        this.id = id;
        this.config = config;
    }

    public ActionConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getConfig() {
        return config;
    }

    public void setConfig(Object config) {
        this.config = config;
    }
}
