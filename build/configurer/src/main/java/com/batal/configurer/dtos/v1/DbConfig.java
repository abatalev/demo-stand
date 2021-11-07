package com.batal.configurer.dtos.v1;

public class DbConfig {
    private String type;
    private String tableName;

    public DbConfig(String tableName) {
        this.type= "db";
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getType() {
        return type;
    }
}
