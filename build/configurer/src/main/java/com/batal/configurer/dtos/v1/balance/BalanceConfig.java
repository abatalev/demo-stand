package com.batal.configurer.dtos.v1.balance;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class BalanceConfig {
    private List<Action> actions;

    public BalanceConfig(List<Action> actions) {
        this.actions = actions;
    }

    public List<Action> getActions() {
        return actions;
    }
}
