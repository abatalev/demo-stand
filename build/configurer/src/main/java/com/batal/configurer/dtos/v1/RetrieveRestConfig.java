package com.batal.configurer.dtos.v1;

public class RetrieveRestConfig {
    private String tableName;
    private String url;

    public RetrieveRestConfig() {
    }

    public RetrieveRestConfig(String url, String tableName) {
        this.url = url;
        this.tableName = tableName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
