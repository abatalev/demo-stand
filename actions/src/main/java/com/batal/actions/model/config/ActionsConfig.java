package com.batal.actions.model.config;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class ActionsConfig {
    private boolean enabled;
    private List<ActionConfig> actions;
    private int balancerPollingPeriod = -1;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<ActionConfig> getActions() {
        return actions;
    }

    public void setActions(List<ActionConfig> actions) {
        this.actions = actions;
    }

    public int getBalancerPollingPeriod() {
        return balancerPollingPeriod;
    }

    public void setBalancerPollingPeriod(int balancerPollingPeriod) {
        this.balancerPollingPeriod = balancerPollingPeriod;
    }
}
