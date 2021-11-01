package com.batal.actions.model.config;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class ActionConfig {

    private String name;
    private String id;
    private Map<String, Object> config;

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

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
