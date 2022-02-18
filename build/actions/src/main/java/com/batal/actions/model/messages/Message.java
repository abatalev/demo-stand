package com.batal.actions.model.messages;

public class Message {
    private String id;
    private Object payload;
    private String correlationId;

    public Message(String id, Object payload) {
        this.id = id;
        this.payload = payload;
    }

    public Message(String id, Object payload, String correlationId) {
        this.id = id;
        this.payload = payload;
        this.correlationId = correlationId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
