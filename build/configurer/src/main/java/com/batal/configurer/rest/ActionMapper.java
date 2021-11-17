package com.batal.configurer.rest;

import com.batal.configurer.dtos.v1.ActionConfig;
import com.batal.configurer.dtos.v1.ActionsConfig;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toList;

@Service
public class ActionMapper {

    private final ConfigLoader loader;

    public ActionMapper(ConfigLoader loader) {
        this.loader = loader;
    }

    public ActionsConfig getConfig() {
        ActionsConfig config = new ActionsConfig();
        config.setEnabled(true);
        config.setBalancerPollingPeriod(5);
        config.setActions(loader.getActions().stream()
                .map(action -> new ActionConfig(
                        action.getType(),
                        action.getId(),
                        action.getConfig()))
                .collect(toList()));
        return config;
    }
}
