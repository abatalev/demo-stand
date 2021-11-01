package com.batal.configurer.dtos.v1;

public class MqConfig {
    private String type;
    private String queueName;

    public MqConfig(String queueName) {
        this.type = "mq";
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getType() {
        return type;
    }
}
