package com.batal.configurer.dtos.v1;

public class MqDbConfig {
    private String tableName;
    private String queueName;

    public MqDbConfig(String tableName, String queueName) {
        this.tableName = tableName;
        this.queueName = queueName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getQueueName() {
        return queueName;
    }
}
