package com.batal.configurer.dtos.v1;

public class RestConfig {
    private String type;
    private String url;

    public RestConfig(String url) {
        this.type = "rest";
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }
}
